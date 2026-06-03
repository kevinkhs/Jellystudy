# JellyStudy 微服务系统 - 部署指南

## 📋 系统架构（第二、三阶段）

```
┌─────────────────────────────────────────────────────────────┐
│                    JellyStudy Web (8080)                     │
│              Controller + Thymeleaf 前端                     │
└──────────┬──────────────────┬───────────────────┬───────────┘
           │  @DubboReference │   @DubboReference  │
           ▼                  ▼                   ▼
┌──────────────────┐ ┌─────────────────┐ ┌────────────────────┐
│ Knowledge-Point  │ │ Question         │ │ Evaluation Service │
│ Provider (8081)  │ │ Provider (8082)  │ │ (8083)             │
│ Dubbo: 20881     │ │ Dubbo: 20882     │ │ Dubbo: 20883       │
└────────┬─────────┘ └────────┬─────────┘ └────────┬───────────┘
         │                    │                    │
         └────────────────────┴────────────────────┘
                              │
                     ┌────────▼────────┐
                     │    MongoDB      │
                     │   :27017        │
                     └─────────────────┘

                              │
                     ┌────────▼────────┐
                     │     Nacos       │
                     │   :8848          │
                     └─────────────────┘
```

## 🔧 服务端口分配

| 服务名称 | 端口 | Dubbo 端口 | 说明 |
|---------|------|-----------|------|
| jellystudy-web | 8080 | - | Web 应用（前端+Controller） |
| jellystudy-knowledge-point-provider | 8081 | 20881 | 知识点服务 |
| jellystudy-question-provider | 8082 | 20882 | 问答服务 |
| jellystudy-evaluation-service | 8083 | 20883 | DeepSeek AI评估服务 |
| Nacos | 8848 | - | 注册中心 |
| MongoDB | 27017 | - | 数据库 |
| SkyWalking (可选) | 8080, 11800 | - | 调用链追踪 |

## 🚀 快速启动

### 前置条件
1. ✅ JDK 11+
2. ✅ Maven 3.6+
3. ✅ MongoDB 已启动 (localhost:27017)
4. ✅ Nacos 已启动 (localhost:8848)
5. ✅ 网络：可以访问 DeepSeek API

### 启动步骤

#### 方式一：使用启动脚本（推荐）
```bash
# 双击运行或命令行执行
start-all.bat
```

#### 方式二：手动启动每个服务

**1. 编译项目**
```bash
cd c:\onlywork\jellystudy
mvn clean package -DskipTests
```

**2. 启动 Nacos**（如果未启动）
```bash
cd {nacos目录}/bin
startup.cmd -m standalone
```

**3. 按顺序启动各服务**

```bash
# 终端 1: 知识点服务
cd jellystudy-knowledge-point-provider
mvn spring-boot:run

# 终端 2: 问答服务
cd jellystudy-question-provider
mvn spring-boot:run

# 终端 3: 评估服务
cd jellystudy-evaluation-service
mvn spring-boot:run

# 终端 4: Web 应用
cd jellystudy-web
mvn spring-boot:run
```

## 📊 功能特性

### Phase 2 - Dubbo 微服务化 ✅

- [x] **知识点服务** (KnowledgePointService)
  - CRUD 操作
  - 分类查询
  - 热门知识点排序
  - 标题搜索

- [x] **问答服务** (QuestionService)
  - 问题 CRUD
  - 回答管理（嵌套评论）
  - 点赞功能
  - 统计数据
  - 推荐算法

- [x] **服务拆分**
  - 独立的知识点应用
  - 独立的问答应用
  - 共享的 Web 前端

### Phase 3 - DeepSeek AI 评估 ✅

- [x] **问题评估**
  - 自动提取知识点
  - 难度分级（easy/medium/hard）
  - 评估原因记录

- [x] **回答评分**
  - 0-100 分制打分
  - 详细评分理由
  - 评估结果持久化

- [x] **自动触发**
  - 提交问题时自动评估
  - 提交回答时自动评分
  - 异常不影响主流程

- [x] **独立部署**
  - 评估服务可单独启停
  - 通过 Dubbo 远程调用
  - 结果存储到 MongoDB

## 🔗 SkyWalking 调用链配置（可选）

### 1. 下载安装 SkyWalking
```bash
# 下载地址: https://skywalking.apache.org/downloads/
# 解压后进入 apache-skywalking-apm-bin 目录
```

### 2. 配置 Agent
在每个服务的 `application.yml` 中添加：

```yaml
spring:
  application:
    name: jellystudy-{service-name}
    
# SkyWalking 配置（可选）
skywalking:
  agent:
    service-name: ${spring.application.name}
    collector:
      backend-service: localhost:11800
```

### 3. 启动 SkyWalking
```bash
# Linux/Mac
./bin/startup.sh

# Windows
bin\startup.bat
```

### 4. 使用 Java Agent 启动服务
在启动命令中添加 JVM 参数：
```bash
java -javaagent:/path/to/skywalking-agent/skywalking-agent.jar \
     -Dskywalking.agent.service_name=jellystudy-web \
     -Dskywalking.collector.backend_service=localhost:11800 \
     -jar target/jellystudy-web.jar
```

### 5. 访问 SkyWalking UI
打开浏览器访问：http://localhost:8080

## 📝 API 文档

### Dubbo 服务接口

#### KnowledgePointService (group: knowledge-point)
```java
// 创建知识点
KnowledgePoint createKnowledgePoint(KnowledgePoint kp);

// 更新知识点
KnowledgePoint updateKnowledgePoint(String id, KnowledgePoint kp);

// 删除知识点
void deleteKnowledgePoint(String id);

// 获取知识点详情
KnowledgePoint getKnowledgePointById(String id);

// 获取所有知识点
List<KnowledgePoint> getAllKnowledgePoints();

// 获取热门知识点
List<KnowledgePoint> findHotKnowledgePoints();

// 按分类查询
List<KnowledgePoint> findByCategory(String category);

// 搜索知识点
List<KnowledgePoint> searchByTitle(String keyword);
```

#### QuestionService (group: question)
```java
// 问题操作
Question createQuestion(Question question);
Question updateQuestion(String id, Question question);
void deleteQuestion(String id);
Question getQuestionById(String id);

// 列表和推荐
List<Question> getAllQuestions();
List<Question> getRecommendedQuestions(int page, int size);
List<Question> getHotQuestions();
List<Question> getQuestionsByKnowledgePointId(String kpId);

// 回答操作
Answer addAnswer(String qid, Answer answer);
Answer updateAnswer(String qid, String aid, Answer answer);
void deleteAnswer(String qid, String aid);

// 评论操作
Comment addComment(String qid, String aid, Comment comment);
Comment updateComment(String qid, String aid, String cid, Comment comment);
void deleteComment(String qid, String aid, String cid);

// 点赞操作
Like likeEntity(String userId, String targetType, String targetId);
void unlikeEntity(String userId, String targetType, String targetId);

// 统计数据
int getTotalQuestionCount();
int getTotalAnswerCount();
int getTotalCommentCount();
int getTotalLikeCount();
List<Question> getTopLikedQuestions(int limit);
List<Answer> getTopLikedAnswers(int limit);
```

#### EvaluationService (group: evaluation)
```java
// 评估问题
Evaluation evaluateQuestion(String qid, String title, String content);

// 评估回答
Evaluation evaluateAnswer(String qid, String aid, String content, String author);

// 查询评估结果
Evaluation getEvaluationById(String id);
List<Evaluation> getEvaluationsByTargetId(String targetId);
```

## 🎯 DeepSeek API 配置

配置文件位置：`jellystudy-evaluation-service/src/main/resources/application.yml`

```yaml
deepseek:
  api-key: sk-6f978b89ecf044dca3b753df0284f8ee
  api-url: https://api.deepseek.com/v1/chat/completions
  model: deepseek-chat
```

## 🐛 故障排查

### 常见问题

**1. Nacos 连接失败**
```
检查: netstat -an | findstr ":8848"
解决: 确保 Nacos 已启动，检查防火墙设置
```

**2. Dubbo 服务注册失败**
```
检查: Nacos 控制台 -> 服务列表
解决: 确认 dubbo.registry.address 配置正确
```

**3. DeepSeek API 调用失败**
```
检查: 日志中的错误信息
解决: 
  - 验证 API Key 是否有效
  - 检查网络连接
  - 查看 API 配额是否用完
```

**4. MongoDB 连接失败**
```
检查: netstat -an | findstr ":27017"
解决: 确保 MongoDB 服务已启动
```

**5. 页面 500 错误**
```
检查: 
  1. 所有 Provider 是否已启动
  2. Nacos 中服务是否已注册
  3. 查看日志中的具体错误信息
```

## 📞 技术支持

如有问题，请检查：
1. 各服务终端的错误日志
2. Nacos 控制台的服务状态
3. MongoDB 数据库连接
4. 网络连通性

---

**版本**: v2.0.0 (Phase 2 & Phase 3 完成)
**最后更新**: 2026-05-12
