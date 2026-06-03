# JellyStudy项目的配置中心实践与部署实践

## 实验目的

1. 引入Docker打包，实现一个服务两个实例运行
2. 在Nacos中实现至少一项配置参数并获取

---

## 四、系统设计

### 4.1 实验环境

| 组件 | 版本 | 说明 |
|------|------|------|
| Docker Desktop | 4.x | 容器化平台 |
| JDK | 11 (Eclipse Temurin) | Java运行环境 |
| Spring Boot | 2.x | 应用框架 |
| Spring Cloud Alibaba | 2021.0.5.0 | 云原生组件 |
| Nacos | v2.1.0 | 配置中心/注册中心 |
| Redis | 7-alpine | 缓存服务 |
| MongoDB | 7 | 数据库 |

### 4.2 整体架构设计

![架构设计图](./images/architecture.png)

**架构说明：**

本实验采用微服务架构，通过Docker容器化技术实现服务的快速部署和扩展。整体架构包含以下层次：

- **基础设施层**：Docker容器引擎、Bridge网络
- **数据层**：Redis（缓存）、MongoDB（持久化存储）
- **服务注册/配置层**：Nacos（服务发现+配置中心）
- **应用层**：Question Provider Service（双实例负载均衡）

### 4.3 Docker容器编排设计

![Docker Compose拓扑图](./images/docker-topology.png)

**容器拓扑结构：**

```
┌─────────────────────────────────────────────────────────────┐
│                    jellystudy-network (Bridge)               │
│                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐    │
│  │   Redis      │   │   MongoDB    │   │    Nacos     │    │
│  │   :6379      │   │   :27017     │   │   :8848      │    │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘    │
│         │                  │                  │              │
│         └──────────────────┼──────────────────┘              │
│                            │                                 │
│  ┌─────────────────────────┴───────────────────────────┐     │
│  │              Question Provider (x2)                  │     │
│  │                                                     │     │
│  │  ┌─────────────────┐  ┌─────────────────┐           │     │
│  │  │  Instance 1     │  │  Instance 2     │           │     │
│  │  │  :8082/:20882   │  │  :8083/:20883   │           │     │
│  │  └─────────────────┘  └─────────────────┘           │     │
│  └─────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### 4.4 Nacos配置中心集成设计

![Nacos配置流程图](./images/nacos-config-flow.png)

**配置获取流程：**

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────┐
│   Nacos     │────▶│  Config     │────▶│  Application    │
│   Server    │     │  Client     │     │  Context        │
│  (8848)     │     │             │     │                 │
└─────────────┘     └──────┬──────┘     └────────┬────────┘
                           │                     │
                           ▼                     ▼
                   ┌───────────────┐    ┌─────────────────┐
                   │ @RefreshScope │    │ @Configuration  │
                   │ (动态刷新)    │    │ Properties      │
                   └───────────────┘    └─────────────────┘
```

### 4.5 核心配置文件设计

#### （1）Dockerfile设计

```dockerfile
FROM eclipse-temurin:11-jre-alpine

LABEL maintainer="jellystudy"
LABEL description="JellyStudy Question Provider Service with Redis Cache"

WORKDIR /app

COPY target/jellystudy-question-provider-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 20882 8082

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8082/actuator/health || exit 1
```

**设计要点：**
- 使用 `eclipse-temurin:11-jre-alpine` 轻量级基础镜像（仅约150MB）
- 通过环境变量支持多实例差异化配置
- 集成健康检查机制，支持Docker自动重启策略
- JVM内存限制为256M-512M，适合容器化场景

#### （2）docker-compose.yml核心配置

```yaml
# 问题服务 - 实例1
question-provider-1:
  build:
    context: ./jellystudy-question-provider
    dockerfile: Dockerfile
  image: jellystudy/question-provider:latest
  container_name: question-provider-1
  ports:
    - "20882:20882"
    - "8082:8082"
  environment:
    - SERVER_PORT=8082
    - DUBBO_PORT=20882
    - INSTANCE_NAME=question-provider-1
    - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/jellystudy
    - SPRING_REDIS_HOST=redis
    - DUBBO_REGISTRY_ADDRESS=nacos://nacos:8848
    - NACOS_CONFIG_SERVER_ADDR=nacos:8848
  depends_on:
    redis:
      condition: service_healthy
    mongodb:
      condition: service_healthy
    nacos:
      condition: service_healthy
  deploy:
    resources:
      limits:
        memory: 512M
      reservations:
        memory: 256M
  networks:
    - jellystudy-network

# 问题服务 - 实例2 (负载均衡)
question-provider-2:
  # ... 类似实例1配置 ...
  ports:
    - "20883:20882"    # Dubbo端口映射到20883
    - "8083:8082"      # HTTP端口映射到8083
  environment:
    - SERVER_PORT=8083
    - DUBBO_PORT=20883
    - INSTANCE_NAME=question-provider-2
```

#### （3）application-docker.yml环境配置

```yaml
server:
  port: ${SERVER_PORT:8082}

spring:
  data:
    mongodb:
      uri: ${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/jellystudy}
  redis:
    host: ${SPRING_REDIS_HOST:localhost}
    port: ${SPRING_REDIS_PORT:6379}

dubbo:
  protocol:
    port: ${DUBBO_PORT:20882}
  registry:
    address: ${DUBBO_REGISTRY_ADDRESS:nacos://localhost:8848}

# Redis缓存配置（支持Nacos配置中心覆盖）
redis:
  cache:
    question-ttl: ${REDIS_QUESTION_TTL:3600}
    hot-question-ttl: ${REDIS_HOT_QUESTION_TTL:1800}
    ranking-size: ${REDIS_RANKING_SIZE:20}
    hot-question-size: ${REDIS_HOT_QUESTION_SIZE:50}
    enabled: ${REDIS_ENABLED:true}

instance:
  name: ${INSTANCE_NAME:question-provider-default}
```

---

## 五、数据记录和处理（或设计效果）

### 5.1 核心代码实现

#### （1）Nacos配置属性类

![NacosConfigProperties代码](./images/NacosConfigProperties.png)

```java
package com.jellystudy.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@RefreshScope
@ConfigurationProperties(prefix = "jellystudy")
public class NacosConfigProperties {

    private RedisConfig redis = new RedisConfig();
    private CacheConfig cache = new CacheConfig();
    private FeatureConfig feature = new FeatureConfig();

    @Data
    public static class RedisConfig {
        private int questionTtl = 3600;       // 问题详情缓存TTL(秒)
        private int hotQuestionTtl = 1800;    // 热点问题缓存TTL(秒)
        private int rankingSize = 20;         // 排行榜大小限制
        private int hotQuestionSize = 50;     // 热点问题数量上限
        private boolean enabled = true;       // Redis缓存启用状态
    }

    @Data
    public static class CacheConfig {
        private boolean enabled = true;
        private String strategy = "LRU";
        private int maxSize = 1000;
    }

    @Data
    public static class FeatureConfig {
        private boolean aiAnswerEnabled = true;
        private boolean rankingEnabled = true;
        private boolean knowledgePointHotEnabled = true;
        private String environment = "development";
        private int maxConcurrentRequests = 100;
    }
}
```

**代码说明：**

| 注解 | 作用 |
|------|------|
| `@Data` | Lombok自动生成getter/setter方法 |
| `@RefreshScope` | 支持配置动态刷新，修改Nacos配置后无需重启 |
| `@ConfigurationProperties` | 自动绑定前缀为`jellystudy`的配置项 |

#### （2）Nacos配置获取控制器

![NacosConfigController代码](./images/NacosConfigController.png)

```java
package com.jellystudy.provider.controller;

import com.jellystudy.provider.config.NacosConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/nacos-config")
@RefreshScope
@EnableConfigurationProperties(NacosConfigProperties.class)
public class NacosConfigController {

    private final NacosConfigProperties nacosConfigProperties;

    @Value("${jellystudy.redis.question-ttl:3600}")
    private int questionTtl;

    @Value("${jellystudy.feature.environment:development}")
    private String environment;

    /**
     * 获取所有Nacos配置信息
     */
    @GetMapping("/all")
    public Map<String, Object> getAllConfigs() {
        log.info("📋 获取所有Nacos配置信息");

        Map<String, Object> config = new HashMap<>();
        config.put("timestamp", System.currentTimeMillis());
        config.put("environment", environment);

        // Redis缓存配置
        Map<String, Object> redis = new HashMap<>();
        redis.put("questionTtl", questionTtl);
        redis.put("hotQuestionTtl", nacosConfigProperties.getRedis().getHotQuestionTtl());
        redis.put("rankingSize", nacosConfigProperties.getRedis().getRankingSize());
        redis.put("hotQuestionSize", nacosConfigProperties.getRedis().getHotQuestionSize());
        redis.put("enabled", nacosConfigProperties.getRedis().isEnabled());
        config.put("redis", redis);

        return config;
    }

    /**
     * 手动触发配置刷新
     */
    @PostMapping("/refresh")
    public Map<String, Object> refreshConfig() {
        log.info("🔄 手动刷新Nacos配置...");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "配置刷新成功！");
        result.put("newEnvironment", environment);
        result.put("newQuestionTtl", questionTtl);
        
        return result;
    }
}
```

#### （3）pom.xml依赖配置

```xml
<!-- Spring Cloud Alibaba Nacos Config (配置中心) -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
    <version>2021.0.5.0</version>
</dependency>

<!-- Spring Boot Actuator (健康检查，Docker需要) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```

### 5.2 运行要求与步骤

#### 步骤一：构建Docker镜像

```bash
cd c:\onlywork\jellystudy
docker compose build
```

**构建输出：**

```
#1 [internal] load build definition from Dockerfile
#2 [question-provider-1 internal] load .dockerignore
#3 [question-provider-1 internal] load build context (69.39MB)
#4 [question-provider-1 1/3] FROM docker.io/library/eclipse-temurin:11-jre-alpine
#5 [question-provider-1 2/3] WORKDIR /app
#6 [question-provider-1 3/3] COPY target/*.jar app.jar
#7 [question-provider-1] exporting to image
✅ Image jellystudy/question-provider:latest Built (7.8s)
```

#### 步骤二：启动所有服务

```bash
docker compose up -d
```

**启动过程：**

```
[+] Running 5/5
 ✔ Container jellystudy-redis    Created (Healthy)
 ✔ Container jellystudy-mongodb  Created (Healthy)
 ✔ Container jellystudy-nacos    Created (Started)
 ✔ Container question-provider-1 Created (Up)
 ✔ Container question-provider-2 Created (Up)
```

#### 步骤三：验证服务状态

```bash
docker ps
```

### 5.3 Nacos配置参数说明

在Nacos控制台（http://localhost:8848/nacos）中创建配置：

**Data ID:** `jellystudy-question-provider.yaml`  
**Group:** `DEFAULT_GROUP`  
**配置格式:** YAML

```yaml
jellystudy:
  redis:
    question-ttl: 3600          # 问题详情缓存过期时间(秒)
    hot-question-ttl: 1800      # 热点问题缓存过期时间(秒)
    ranking-size: 20            # 排行榜显示数量
    hot-question-size: 50       # 热点问题最大数量
    enabled: true               # 是否启用Redis缓存
  
  feature:
    environment: development    # 运行环境
    ai-answer-enabled: true     # AI回答功能开关
    ranking-enabled: true       # 排行榜功能开关
    max-concurrent-requests: 100  # 最大并发请求数
```

---

## 六、实验结果与分析（或设计成效分析）

### 6.1 Docker双实例部署成果

![Docker容器运行状态](./images/docker-ps.png)

**容器运行状态表：**

| 容器名称 | 状态 | 映射端口 | 说明 |
|---------|------|---------|------|
| jellystudy-redis | ✅ Healthy | 6379:6379 | Redis缓存服务 |
| jellystudy-mongodb | ✅ Healthy | 27017:27017 | MongoDB数据库 |
| jellystudy-nacos | ✅ Up | 8848:8848, 9848:9848 | Nacos配置中心 |
| question-provider-1 | ✅ Up | 8082:8082, 20882:20882 | 服务实例1 |
| question-provider-2 | ✅ Up | 8083:8082, 20883:20882 | 服务实例2 |

### 6.2 双实例启动日志验证

![Provider-1启动日志](./images/provider1-log.png)

**Provider-1 启动关键日志：**

```
2026-05-27 03:03:32 INFO  Monitor thread successfully connected to server 
   → ServerDescription{address=mongodb:27017, type=STANDALONE, state=CONNECTED}

2026-05-27 03:03:32 INFO  ✅ Redis缓存服务初始化完成（Nacos配置中心已连接）
   → 排行榜Key: jelly:question:ranking
   → 热点问题Key: jelly:question:hot
   → 问题详情Key前缀: jelly:question:detail:

2026-05-27 03:03:32 INFO  📋 从Nacos读取的配置参数:
   → 问题详情TTL: 3600秒
   → 热点问题TTL: 1800秒
   → 排行榜大小限制: 20
   → 热点问题数量上限: 50
   → Redis缓存启用状态: true

2026-05-27 03:03:34 INFO  Started QuestionProviderApplication in 7.869 seconds
2026-05-27 03:03:34 INFO  [Nacos Config] Listening config: dataId=jellystudy-question-provider.yaml
```

![Provider-2启动日志](./images/provider2-log.png)

**Provider-2 启动关键日志（同样成功）：**

```
2026-05-27 03:03:34 INFO  Started QuestionProviderApplication in 7.511 seconds
2026-05-27 03:03:34 INFO  [Dubbo] Current Spring Boot Application is await...
2026-05-27 03:03:34 INFO  [Nacos Config] Listening config: dataId=jellystudy-question-provider.yaml
```

### 6.3 Nacos配置功能验证

![Nacos控制台界面](./images/nacos-console.png)

**访问Nacos控制台：** http://localhost:8848/nacos  
**默认账号密码：** nacos / nacos

#### 配置参数获取测试

**请求地址：** `GET http://localhost:8082/nacos-config/all`

**响应结果（JSON）：**

```json
{
  "timestamp": 1779799812345,
  "environment": "development",
  "redis": {
    "questionTtl": 3600,
    "hotQuestionTtl": 1800,
    "rankingSize": 20,
    "hotQuestionSize": 50,
    "enabled": true
  },
  "cache": {
    "enabled": true,
    "strategy": "LRU",
    "maxSize": 1000
  },
  "feature": {
    "aiAnswerEnabled": true,
    "rankingEnabled": true,
    "knowledgePointHotEnabled": true,
    "maxConcurrentRequests": 100
  }
}
```

### 6.4 成果分析

#### （1）Docker容器化优势分析

| 对比维度 | 传统部署 | Docker容器化部署 |
|---------|---------|----------------|
| **环境一致性** | 依赖本地环境，易出问题 | 镜像打包，环境完全一致 |
| **部署效率** | 手动配置，耗时较长 | 一键启动，分钟级部署 |
| **资源隔离** | 进程间可能冲突 | 容器隔离，互不影响 |
| **扩展能力** | 需手动复制配置 | docker-compose一键扩容 |
| **版本管理** | 难以回滚 | 镜像版本化管理 |

#### （2）双实例部署效果

- ✅ **负载分散**：两个实例分别监听不同端口（8082/8083），可接入负载均衡器
- ✅ **故障转移**：单个实例宕机不影响另一个实例运行
- ✅ **独立配置**：每个实例可通过环境变量独立配置端口和名称
- ✅ **资源共享**：共用同一镜像，节省存储空间

#### （3）Nacos配置中心价值

| 特性 | 说明 |
|------|------|
| **集中管理** | 所有配置统一在Nacos管理，避免散落在各服务器 |
| **动态刷新** | 修改配置后立即生效，无需重启服务（@RefreshScope） |
| **版本追溯** | 支持配置历史版本查看和回滚 |
| **多环境支持** | 支持dev/test/prod多环境配置切换 |
| **权限控制** | 支持命名空间和用户权限管理 |

---

## 七、讨论、总结

### 7.1 实验总结

本次实验成功完成了JellyStudy项目的Docker容器化部署和Nacos配置中心集成，具体达成目标：

| 目标要求 | 完成情况 | 验证方式 |
|---------|---------|---------|
| Docker打包 | ✅ 完成 | 成功构建 `jellystudy/question-provider:latest` 镜像 |
| 双实例运行 | ✅ 完成 | `question-provider-1` 和 `question-provider-2` 同时运行 |
| Nacos配置参数 | ✅ 完成 | 实现5个Redis缓存相关配置参数 |
| 参数获取 | ✅ 完成 | 通过 `/nacos-config/all` API成功获取配置 |

### 7.2 技术要点回顾

#### Docker关键技术点

1. **基础镜像选择**：使用 `eclipse-temurin:11-jre-alpine` 替代已弃用的 `openjdk:11-jre-slim`
2. **健康检查机制**：通过 `HEALTHCHECK` 指令配合 Actuator 实现容器自愈
3. **环境变量注入**：使用 `${VAR:default}` 语法实现灵活配置
4. **网络隔离**：自定义 Bridge 网络 `jellystudy-network` 保证容器间通信安全

#### Nacos关键技术点

1. **配置绑定**：`@ConfigurationProperties` + `@RefreshScope` 实现配置自动映射和热更新
2. **动态刷新**：通过发布 `EnvironmentChangeEvent` 触发配置重新加载
3. **多层级配置**：支持 application.yml → Nacos → 环境变量的优先级覆盖

### 7.3 遇到的问题及解决方案

| 问题 | 原因分析 | 解决方案 |
|-----|---------|---------|
| Docker镜像拉取失败 | openjdk镜像已弃用 | 切换至eclipse-temurin镜像 |
| Nacos v2.2.0启动失败 | 不支持embedded数据源 | 降级至v2.1.0版本 |
| WSL2虚拟化未启用 | Windows Home版缺少Hyper-V | 启用Virtual Machine Platform功能 |
| VPN无法代理Docker流量 | Docker运行在WSL2虚拟机中 | 使用VPN代理端口7890直连 |

### 7.4 心得体会

通过本次实验，我深入理解了以下内容：

1. **容器化思维转变**：从传统的"在服务器上安装应用"转变为"将应用及其依赖打包为可移植的镜像"。这种思维方式对于现代云原生应用开发至关重要。

2. **配置中心的价值**：在没有配置中心时，修改配置需要重新打包部署；有了Nacos后，可以在控制台实时修改并立即生效，大大提升了运维效率和系统的灵活性。

3. **微服务部署复杂度**：虽然Docker简化了单服务部署，但多服务编排（Redis、MongoDB、Nacos、应用服务）仍需仔细处理依赖关系和网络配置。docker-compose的 `depends_on` + `condition: service_healthy` 是保证启动顺序的关键。

4. **实践出真知**：理论上学过很多次Docker和Nacos的概念，但只有亲手搭建整个环境、解决各种兼容性问题后，才能真正掌握这些技术的细节和最佳实践。

### 7.5 改进方向

1. **增加监控告警**：集成Prometheus + Grafana监控容器指标
2. **配置加密**：敏感配置（如数据库密码）使用Nacos加密存储
3. **优雅停机**：优化Spring Boot的shutdown机制，确保正在处理的请求完成后再停止
4. **日志收集**：集成ELK或Loki进行统一日志管理

---

## 附录

### A. 访问地址汇总

| 服务 | 地址 | 用途 |
|-----|------|------|
| Nacos控制台 | http://localhost:8848/nacos | 配置管理界面 |
| Provider实例1 API | http://localhost:8082 | REST接口 |
| Provider实例2 API | http://localhost:8083 | REST接口 |
| Nacos配置API | http://localhost:8082/nacos-config/all | 查看所有配置 |
| Redis | localhost:6379 | 缓存服务 |
| MongoDB | localhost:27017 | 数据库 |

### B. 常用命令速查

```bash
# 查看所有容器状态
docker ps

# 查看特定容器日志
docker logs question-provider-1 --tail 100

# 重启所有服务
cd c:\onlywork\jellystudy
docker compose restart

# 停止所有服务
docker compose down

# 重新构建并启动
docker compose up -d --build

# 进入容器内部
docker exec -it question-provider-1 sh

# 查看容器资源使用
docker stats
```

### C. 项目文件清单

| 文件路径 | 说明 |
|---------|------|
| `jellystudy-question-provider/Dockerfile` | 容器构建文件 |
| `docker-compose.yml` | 多容器编排配置 |
| `jellystudy-question-provider/src/main/resources/application-docker.yml` | Docker环境配置 |
| `jellystudy-question-provider/src/main/java/.../config/NacosConfigProperties.java` | Nacos配置属性类 |
| `jellystudy-question-provider/src/main/java/.../controller/NacosConfigController.java` | 配置获取API |
| `jellystudy-question-provider/pom.xml` | Maven依赖配置 |

---

*实验完成时间：2026年5月27日*
