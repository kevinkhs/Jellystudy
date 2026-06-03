# Nacos 配置中心 - JellyStudy 问题服务

## 📋 概述

本文档说明如何在 Nacos 配置中心中为 `jellystudy-question-provider` 服务添加和管理配置参数。

---

## 🔧 步骤1：访问 Nacos 控制台

打开浏览器访问：http://localhost:8848/nacos

默认账号密码：`nacos` / `nacos`

---

## 📝 步骤2：创建配置文件（共3个）

### **配置1: 主配置文件**

- **Data ID**: `jellystudy-question-provider.yaml`
- **Group**: `JELLYSTUDY_GROUP`
- **配置格式**: `YAML`
- **配置内容**:

```yaml
jellystudy:
  redis:
    question-ttl: 3600
    hot-question-ttl: 1800
    ranking-size: 20
    hot-question-size: 50
    enabled: true
  cache:
    enabled: true
    strategy: LRU
    max-size: 1000
  feature:
    ai-answer-enabled: true
    ranking-enabled: true
    knowledge-point-hot-enabled: true
    environment: development
    max-concurrent-requests: 100
```

---

### **配置2: 公共Redis配置**

- **Data ID**: `common-redis.yaml`
- **Group**: `JELLYSTUDY_GROUP`
- **配置格式**: `YAML`
- **配置内容**:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

---

### **配置3: 自定义扩展配置**

- **Data ID**: `question-provider-custom.yaml`
- **Group**: `JELLYSTUDY_GROUP`
- **配置格式**: `YAML`
- **配置内容**:

```yaml
# 自定义业务配置
question:
  default-page-size: 10
  max-page-size: 100
  enable-pagination: true

monitoring:
  enable-metrics: true
  metrics-interval: 60
```

---

## ✅ 步骤3：验证配置是否生效

启动问题服务后，访问以下API验证：

```bash
# 获取所有Nacos配置
curl http://localhost:8082/nacos-config/all

# 获取Redis缓存配置
curl http://localhost:8082/nacos-config/redis

# 获取功能开关配置
curl http://localhost:8082/nacos-config/features
```

**预期响应示例：**
```json
{
  "timestamp": 1705660800000,
  "environment": "development",
  "redis": {
    "questionTtl": 3600,
    "hotQuestionTtl": 1800,
    "rankingSize": 20,
    "enabled": true
  },
  "feature": {
    "aiAnswerEnabled": true,
    "rankingEnabled": true,
    "maxConcurrentRequests": 100
  }
}
```

---

## 🔄 步骤4：测试动态刷新功能

### **方法1: 通过API刷新**

```bash
curl -X POST http://localhost:8082/nacos-config/refresh
```

### **方法2: 在Nacos控制台修改配置**

1. 进入 **配置管理** → **配置列表**
2. 找到 `jellystudy-question-provider.yaml`
3. 点击 **编辑**，修改任意值（例如将 `environment` 改为 `production`）
4. 点击 **发布**
5. 等待几秒后重新调用 `/nacos-config/all` API，查看值是否更新

**示例：修改环境标识**

修改前：
```yaml
feature:
  environment: development
```

修改后：
```yaml
feature:
  environment: production
```

验证结果：
```json
{
  "environment": "production"  // ✅ 已自动更新！
}
```

---

## 🎯 关键特性说明

### **1. @RefreshScope 动态刷新**

所有标注了 `@RefreshScope` 的Bean会在Nacos配置变更时自动刷新：

- `NacosConfigProperties.java` - 配置属性类
- `NacosConfigController.java` - REST控制器

### **2. @Value 注入支持**

使用 `@Value` 注解的属性也支持动态刷新：

```java
@Value("${jellystudy.feature.environment:development}")
private String environment;  // 可动态更新
```

### **3. 共享配置 (shared-configs)**

多个服务可共享的公共配置：
- `common-redis.yaml` - Redis连接配置
- `common-cache.yaml` - 缓存策略配置

### **4. 扩展配置 (extension-configs)**

特定服务的自定义配置：
- `question-provider-custom.yaml` - 问题服务专属配置

---

## 🐳 Docker环境中的Nacos配置

当使用Docker Compose部署时，通过环境变量指定Nacos地址：

```yaml
environment:
  - NACOS_CONFIG_SERVER_ADDR=nacos:8848
```

Docker容器会自动连接到同一网络中的Nacos服务。

---

## 💡 最佳实践建议

### **1. 敏感信息管理**

❌ 不要在Nacos中存储敏感信息（如数据库密码、API Key）

✅ 建议使用环境变量或密钥管理系统

### **2. 配置分组策略**

按环境或模块进行分组：

| Group名称 | 用途 |
|-----------|------|
| `JELLYSTUDY_DEV` | 开发环境配置 |
| `JELLYSTUDY_TEST` | 测试环境配置 |
| `JELLYSTUDY_PROD` | 生产环境配置 |

### **3. 版本管理**

每次修改配置时填写描述：

```
版本: v1.2.0
描述: 增加排行榜大小到30，优化缓存TTL时间
作者: admin
```

### **4. 灰度发布**

利用命名空间实现配置灰度：

- `public` - 默认配置
- `gray-release` - 灰度配置（仅部分实例加载）

---

## 🔍 故障排查

### **问题1: 配置未生效**

**检查项：**
1. Data ID 是否完全匹配（区分大小写）
2. Group 是否正确（默认是 `DEFAULT_GROUP`）
3. 配置格式是否选择 `YAML`
4. 服务是否重启过（首次需要重启）

**解决方案：**
```bash
# 重启服务
mvn -f jellystudy-question-provider/pom.xml spring-boot:run
```

### **问题2: 无法连接Nacos**

**检查项：**
1. Nacos服务是否运行（http://localhost:8848/nacos）
2. bootstrap.yml中的server-addr是否正确
3. 防火墙是否阻止了8848端口

**日志查看：**
```bash
grep "nacos" logs/application.log | tail -50
```

### **问题3: 刷新不生效**

**可能原因：**
- Bean没有标注 `@RefreshScope`
- 属性类没有 `@ConfigurationProperties`
- 使用了 `static` 字段（不支持动态刷新）

---

## 📚 相关资源

- [Spring Cloud Alibaba Nacos Config 文档](https://github.com/alibaba/spring-cloud-alibaba/wiki/Nacos-config)
- [Nacos 官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [JellyStudy 项目README](../README.md)

---

**最后更新**: 2026-05-19
**维护者**: JellyStudy Team