# JellyStudy Docker 部署 + Nacos 配置中心 完整指南

## 📋 目录

- [第一部分：Docker化部署](#第一部分docker化部署)
  - [1.1 架构设计](#11-架构设计)
  - [1.2 快速开始](#12-快速开始)
  - [1.3 双实例负载均衡验证](#13-双实例负载均衡验证)
- [第二部分：Nacos配置中心](#第二部分nacos配置中心)
  - [2.1 配置参数列表](#21-配置参数列表)
  - [2.2 动态刷新演示](#22-动态刷新演示)
  - [2.3 配置管理最佳实践](#23-配置管理最佳实践)

---

## 第一部分：Docker化部署

### 1.1 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                   Docker Compose 网络拓扑                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────┐                                           │
│   │   Nacos     │ :8848                                    │
│   │ (注册中心)   │                                           │
│   └──────┬──────┘                                           │
│          │                                                  │
│   ┌──────┴──────┐    ┌─────────────────────────────┐        │
│   │  MongoDB    │    │     Redis 缓存              │        │
│   │  (数据库)    │:27017│     (缓存+排行榜)           │:6379  │
│   └─────────────┘    └──────────────┬──────────────┘        │
│                                     │                      │
│         ┌───────────────────────────┼──────────────┐       │
│         │                           │              │       │
│   ┌─────┴──────┐            ┌───────┴──────┐       │       │
│   │ 实例1       │            │ 实例2         │       │       │
│   │ :20882/:8082│            │ :20883/:8083  │       │       │
│   │ question-   │            │ question-     │       │       │
│   │ provider-1  │            │ provider-2    │       │       │
│   └─────────────┘            └──────────────┘       │       │
│                                                     │       │
│                  Dubbo 负载均衡 + Nacos 注册发现      │       │
└─────────────────────────────────────────────────────┘       │
                                                              │
```

**核心特性：**
- ✅ **双实例部署**：问题服务运行2个容器，实现负载均衡
- ✅ **服务注册**：自动注册到Nacos，支持Dubbo RPC调用
- ✅ **Redis共享**：两个实例共享同一个Redis缓存和排行榜
- ✅ **MongoDB共享**：两个实例操作同一数据库
- ✅ **健康检查**：内置Docker健康检查机制
- ✅ **资源限制**：每个实例内存上限512MB

---

### 1.2 快速开始

#### **前置条件**

```bash
# 检查环境
docker --version          # Docker >= 20.10
docker-compose --version  # Docker Compose >= 2.0
java -version             # JDK 11+ (用于本地构建)

# 确保端口未被占用
netstat -an | findstr ":6379 :27017 :8848 :20882 :20883 :8082 :8083"
```

#### **步骤1：构建项目**

```bash
# 进入项目根目录
cd c:\onlywork\jellystudy

# 编译所有模块（跳过测试）
mvn clean package -DskipTests

# 验证JAR包已生成
ls jellystudy-question-provider/target/*.jar
```

**预期输出：**
```
jellystudy-question-provider-1.0.0-SNAPSHOT.jar
```

#### **步骤2：启动所有服务（一键部署）**

```bash
# 使用Docker Compose启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志（问题服务实例1）
docker-compose logs -f question-provider-1
```

**预期输出：**
```
NAME                    STATUS              PORTS
jellystudy-mongodb      Up (healthy)        0.0.0.0:27017->27017/tcp
jellystudy-nacos        Up (healthy)        0.0.0.0:8848->8848/tcp, 9848/tcp
jellystudy-redis        Up (healthy)        0.0.0.0:6379->6379/tcp
question-provider-1     Up (healthy)        0.0.0.0:20882->20882/tcp, 8082/tcp
question-provider-2     Up (healthy)        0.0.0.0:20883->20882/tcp, 8083/tcp
```

#### **步骤3：验证双实例运行**

```bash
# 访问实例1的Nacos配置API
curl http://localhost:8082/nacos-config/all

# 访问实例2的Nacos配置API
curl http://localhost:8083/nacos-config/all
```

**预期输出（两个实例应返回相同配置）：**
```json
{
  "environment": "development",
  "redis": {
    "questionTtl": 3600,
    "enabled": true
  },
  "feature": {
    "aiAnswerEnabled": true,
    "rankingEnabled": true
  }
}
```

#### **步骤4：在Nacos控制台查看服务注册**

打开浏览器访问：http://localhost:8848/nacos

**路径：** 服务管理 → 服务列表 → 查找 `jellystudy-question-provider`

**预期结果：**
```
服务名称                          实例数   健康实例数
jellystudy-question-provider      2       2
```

应该看到**2个实例**已注册：
- `192.168.x.x:20882` (question-provider-1)
- `192.168.x.x:20883` (question-provider-2)

---

### 1.3 双实例负载均衡验证

#### **测试方法1：查看请求分发**

```bash
# 连续发送多次请求，观察不同实例响应
for i in {1..10}; do
  curl http://localhost:8082/nacos-config/features | grep -o '"environment":"[^"]*"'
done
```

#### **测试方法2：通过Dubbo Consumer调用**

启动Web前端服务后访问：
```bash
http://localhost:8080/question/list
```

在问题服务的日志中观察：
```bash
# 实例1日志
docker-compose logs -f question-provider-1 | grep "获取问题列表"

# 实例2日志
docker-compose logs -f question-provider-2 | grep "获取问题列表"
```

**预期现象：** 请求会随机分配到两个实例。

#### **测试方法3：模拟实例故障**

```bash
# 停止实例1
docker-compose stop question-provider-1

# 再次访问服务（应仍然正常）
curl http://localhost:8083/nacos-config/all

# 在Nacos控制台查看：实例数变为1

# 重启实例1
docker-compose start question-provider-1

# Nacos控制台：实例数恢复为2
```

---

## 第二部分：Nacos配置中心

### 2.1 配置参数列表

| 参数路径 | 默认值 | 说明 | 是否支持动态刷新 |
|---------|--------|------|----------------|
| `jellystudy.redis.question-ttl` | 3600 | 问题详情缓存时间(秒) | ✅ 是 |
| `jellystudy.redis.hot-question-ttl` | 1800 | 热点问题缓存时间(秒) | ✅ 是 |
| `jellystudy.redis.ranking-size` | 20 | 排行榜显示数量 | ✅ 是 |
| `jellystudy.redis.hot-question-size` | 50 | 热点问题数量上限 | ✅ 是 |
| `jellystudy.redis.enabled` | true | Redis总开关 | ✅ 是 |
| `jellystudy.feature.ai-answer-enabled` | true | AI回答功能开关 | ✅ 是 |
| `jellystudy.feature.ranking-enabled` | true | 排行榜功能开关 | ✅ 是 |
| `jellystudy.feature.environment` | development | 环境标识 | ✅ 是 |
| `jellystudy.feature.max-concurrent-requests` | 100 | 最大并发请求数 | ✅ 是 |

---

### 2.2 动态刷新演示

#### **场景1：修改缓存TTL时间**

**Step 1:** 查看当前配置
```bash
curl http://localhost:8082/nacos-config/redis
```

**输出：**
```json
{
  "questionTtl": 3600,
  "hotQuestionTtl": 1800
}
```

**Step 2:** 在Nacos控制台修改配置

1. 进入：配置管理 → 配置列表
2. 找到：`jellystudy-question-provider.yaml` (Group: JELLYSTUDY_GROUP)
3. 点击：编辑
4. 修改：
```yaml
jellystudy:
  redis:
    question-ttl: 7200    # 改为2小时
    hot-question-ttl: 3600 # 改为1小时
```
5. 点击：发布

**Step 3:** 验证配置已更新（无需重启！）
```bash
# 等待5秒后再次查询
curl http://localhost:8082/nacos-config/redis
```

**输出：**
```json
{
  "questionTtl": 7200,    // ✅ 已更新！
  "hotQuestionTtl": 3600  // ✅ 已更新！
}
```

**Step 4:** 手动触发刷新（可选）
```bash
curl -X POST http://localhost:8082/nacos-config/refresh
```

**输出：**
```json
{
  "success": true,
  "message": "配置刷新成功！",
  "newQuestionTtl": 7200
}
```

---

#### **场景2：关闭AI回答功能**

**Step 1:** 修改功能开关
```yaml
jellystudy:
  feature:
    ai-answer-enabled: false  # 关闭AI回答
```

**Step 2:** 发布配置后，Web前端的"AI回答"按钮将失效（代码层面需配合判断）

**Step 3:** 重新开启
```yaml
jellystudy:
  feature:
    ai-answer-enabled: true  # 重新开启
```

---

### 2.3 配置管理最佳实践

#### **✅ 推荐做法**

1. **按环境隔离配置**
   ```
   Group命名:
   - JELLYSTUDY_DEV     (开发环境)
   - JELLYSTUDY_TEST    (测试环境)
   - JELLYSTUDY_PROD    (生产环境)
   ```

2. **版本化管理**
   ```
   每次修改填写描述:
   v1.0.0 - 初始配置
   v1.1.0 - 增加热点问题数量到100
   v1.2.0 - 生产环境优化TTL时间
   ```

3. **敏感信息处理**
   ```yaml
   ❌ 错误: 直接写在配置中
   deepseek:
     api-key: sk-xxx
   
   ✅ 正确: 使用环境变量
   deepseek:
     api-key: ${DEEPSEEK_API_KEY}
   ```

4. **灰度发布策略**
   ```yaml
   # 使用namespace区分
   public (默认配置)
   gray-release (灰度配置 - 仅10%流量)
   ```

#### **❌ 常见错误**

1. **忘记添加@RefreshScope注解**
   - 症状：修改配置后值不更新
   - 解决：在Controller和Service类上添加 `@RefreshScope`

2. **使用static字段接收配置**
   - 症状：无法动态刷新
   - 解决：改为实例变量

3. **Data ID或Group不匹配**
   - 症状：配置不生效
   - 解决：严格区分大小写，完全匹配

---

## 🛠️ 运维命令速查

### **Docker管理**

```bash
# 启动所有服务
docker-compose up -d --build

# 停止所有服务
docker-compose down

# 重启某个服务
docker-compose restart question-provider-1

# 查看资源使用情况
docker stats

# 进入容器内部
docker exec -it question-provider-1 bash

# 查看实时日志
docker-compose logs -f --tail=100 question-provider-1
```

### **Nacos配置管理**

```bash
# 导出所有配置
curl "http://localhost:8848/nacos/v1/cs/configs?dataId=&group=&tenant=&pageNo=1&pageSize=100"

# 导入配置（批量）
POST http://localhost:8848/nacos/v1/cs/configs

# 删除配置
DELETE http://localhost:8848/nacos/v1/cs/configs?dataId=xxx&group=JELLYSTUDY_GROUP
```

### **监控与诊断**

```bash
# 查看服务健康状态
curl http://localhost:8082/actuator/health

# 查看Redis连接状态
curl http://localhost:8082/actuator/redis

# 查看Nacos配置信息
curl http://localhost:8082/nacos-config/all

# 查看缓存统计
curl http://localhost:8082/redis/stats
```

---

## 📊 性能优化建议

### **Docker层**

1. **镜像优化**
   ```dockerfile
   # 使用多阶段构建减小镜像体积
   FROM maven:3.8-openjdk-11 AS builder
   WORKDIR /app
   COPY pom.xml .
   COPY src ./src
   RUN mvn package -DskipTests

   FROM openjdk:11-jre-slim
   COPY --from=builder /app/target/*.jar app.jar
   ```

2. **资源调整**
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '1.0'
         memory: 512M
       reservations:
         cpus: '0.5'
         memory: 256M
   ```

### **Nacos配置层**

1. **配置预热**
   - 启动时加载常用配置到本地缓存
   - 减少首次请求延迟

2. **配置版本控制**
   - 使用Git管理配置文件
   - 通过CI/CD自动同步到Nacos

3. **监控告警**
   - 监控配置变更频率
   - 异常变更及时告警

---

## 🔗 相关文档

- [Nacos配置中心详细指南](./NACOS_CONFIG_GUIDE.md)
- [Redis设计文档](./REDIS_DESIGN_DOC.md)
- [项目README](./README.md)

---

## 💬 技术支持

如遇到问题，请检查：

1. **Docker日志**: `docker-compose logs <service-name>`
2. **应用日志**: 容器内 `/app/logs/application.log`
3. **Nacos状态**: http://localhost:8848/nacos
4. **端口占用**: `netstat -an | findstr ":<port>"`

---

**文档版本**: v1.0.0
**最后更新**: 2026-05-19
**适用范围**: JellyStudy 微服务系统