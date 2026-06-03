package com.jellystudy.config;

import com.jellystudy.entity.Answer;
import com.jellystudy.entity.Comment;
import com.jellystudy.entity.KnowledgePoint;
import com.jellystudy.entity.Question;
import com.jellystudy.repository.KnowledgePointRepository;
import com.jellystudy.repository.LikeRepository;
import com.jellystudy.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private KnowledgePointRepository knowledgePointRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (knowledgePointRepository.count() == 0) {
            initKnowledgePoints();
        }
        if (questionRepository.count() == 0) {
            initQuestions();
        }
    }

    private void initKnowledgePoints() {
        KnowledgePoint kp1 = new KnowledgePoint();
        kp1.setTitle("Spring Boot基础");
        kp1.setDescription("Spring Boot框架的核心概念、自动配置、起步依赖等基础知识");
        kp1.setCategory("Spring");
        kp1.setQuestionCount(3);
        kp1.setCreateTime(java.time.LocalDateTime.now());
        kp1.setUpdateTime(java.time.LocalDateTime.now());
        knowledgePointRepository.save(kp1);

        KnowledgePoint kp2 = new KnowledgePoint();
        kp2.setTitle("MongoDB数据库");
        kp2.setDescription("MongoDB文档型数据库的使用、索引、聚合管道等");
        kp2.setCategory("数据库");
        kp2.setQuestionCount(2);
        kp2.setCreateTime(java.time.LocalDateTime.now());
        kp2.setUpdateTime(java.time.LocalDateTime.now());
        knowledgePointRepository.save(kp2);

        KnowledgePoint kp3 = new KnowledgePoint();
        kp3.setTitle("Java集合框架");
        kp3.setDescription("Java中List、Set、Map等集合接口及其实现类的使用");
        kp3.setCategory("Java");
        kp3.setQuestionCount(2);
        kp3.setCreateTime(java.time.LocalDateTime.now());
        kp3.setUpdateTime(java.time.LocalDateTime.now());
        knowledgePointRepository.save(kp3);

        KnowledgePoint kp4 = new KnowledgePoint();
        kp4.setTitle("微服务架构");
        kp4.setDescription("微服务设计原则、服务拆分、服务间通信等概念");
        kp4.setCategory("微服务");
        kp4.setQuestionCount(1);
        kp4.setCreateTime(java.time.LocalDateTime.now());
        kp4.setUpdateTime(java.time.LocalDateTime.now());
        knowledgePointRepository.save(kp4);

        KnowledgePoint kp5 = new KnowledgePoint();
        kp5.setTitle("Thymeleaf模板引擎");
        kp5.setDescription("Thymeleaf模板语法、表达式、布局等前端技术");
        kp5.setCategory("前端");
        kp5.setQuestionCount(1);
        kp5.setCreateTime(java.time.LocalDateTime.now());
        kp5.setUpdateTime(java.time.LocalDateTime.now());
        knowledgePointRepository.save(kp5);

        System.out.println("✅ 知识点数据初始化完成！");
    }

    private void initQuestions() {
        java.util.List<KnowledgePoint> kps = knowledgePointRepository.findAll();

        Question q1 = new Question();
        q1.setTitle("如何理解Spring Boot的自动配置原理？");
        q1.setContent("我正在学习Spring Boot，想深入了解它的自动配置机制。请问Spring Boot是如何实现自动配置的？@Conditional注解的作用是什么？能否详细解释一下整个流程？");
        q1.setKnowledgePointId(kps.get(0).getId());
        q1.setKnowledgePointTitle(kps.get(0).getTitle());
        q1.setAuthor("学习者小明");
        q1.setViewCount(156);
        q1.setLikeCount(23);
        q1.setDifficulty("中等");

        Answer a1 = new Answer();
        a1.setId("ans_001");
        a1.setContent("Spring Boot的自动配置原理主要基于以下几个关键点：\n\n1. **@EnableAutoConfiguration**：通过这个注解启用自动配置\n2. **spring.factories文件**：定义了所有的自动配置类\n3. **@Conditional**系列注解：根据条件决定是否加载配置\n   - @ConditionalOnClass：当类路径存在指定类时生效\n   - @ConditionalOnProperty：当配置属性满足条件时生效\n   - @ConditionalOnMissingBean：当容器中没有指定Bean时生效\n\n工作流程：\n- Spring Boot启动时扫描classpath\n- 加载META-INF/spring.factories中的配置类\n- 根据@Conditional条件判断是否创建Bean\n- 将符合条件的Bean注册到容器中\n\n建议阅读SpringBootApplication的源码和AutoConfigurationImportSelector类来深入理解。");
        a1.setAuthor("资深架构师");
        a1.setLikeCount(45);
        a1.setIsAccepted(true);
        a1.setCreateTime(java.time.LocalDateTime.now().minusDays(2));
        a1.setUpdateTime(java.time.LocalDateTime.now().minusDays(2));

        Comment c1 = new Comment();
        c1.setId("cmt_001");
        c1.setContent("解释得很清晰！特别是@Conditional那部分，之前一直不太明白");
        c1.setAuthor("初学者");
        c1.setParentId(null);
        c1.setPath("ans_001");
        c1.setLikeCount(5);
        c1.setCreateTime(java.time.LocalDateTime.now().minusDays(1));
        a1.getComments().add(c1);

        Comment c2 = new Comment();
        c2.setId("cmt_002");
        c2.setContent("补充一点：从Spring Boot 2.x开始，推荐使用AutoConfiguration.imports文件代替spring.factories");
        c2.setAuthor("技术达人");
        c2.setParentId("cmt_001");
        c2.setPath("ans_001.cmt_001");
        c2.setLikeCount(8);
        c2.setCreateTime(java.time.LocalDateTime.now().minusHours(12));
        a1.getComments().add(c2);

        q1.getAnswers().add(a1);
        q1.setAnswerCount(1);
        q1.setCreateTime(java.time.LocalDateTime.now().minusDays(3));
        q1.setUpdateTime(java.time.LocalDateTime.now().minusHours(6));

        Question q2 = new Question();
        q2.setTitle("MongoDB中如何优化查询性能？");
        q2.setContent("我在使用MongoDB进行开发时发现查询速度比较慢，请问有哪些方法可以优化MongoDB的查询性能？包括索引的使用、查询语句优化等方面。");
        q2.setKnowledgePointId(kps.get(1).getId());
        q2.setKnowledgePointTitle(kps.get(1).getTitle());
        q2.setAuthor("后端开发者");
        q2.setViewCount(98);
        q2.setLikeCount(15);
        q2.setDifficulty("困难");

        Answer a2 = new Answer();
        a2.setId("ans_002");
        a2.setContent("MongoDB查询性能优化可以从以下几个方面入手：\n\n**1. 索引优化**\n- 为常用查询字段创建索引\n- 使用复合索引支持多字段查询\n- 使用explain()分析查询执行计划\n- 避免过多的索引影响写入性能\n\n**2. 查询语句优化**\n- 只查询需要的字段（projection）\n- 使用$limit限制返回数量\n- 避免使用$where和JavaScript表达式\n- 合理使用覆盖查询（Covered Query）\n\n**3. 数据模型设计**\n- 合理使用内嵌文档vs引用\n- 考虑读写比例选择合适的设计模式\n- 避免过深的嵌套\n\n**4. 其他建议**\n- 使用分片处理大数据量\n- 定期执行compact整理碎片\n- 监控慢查询日志");
        a2.setAuthor("DBA专家");
        a2.setLikeCount(32);
        a2.setIsAccepted(true);
        a2.setCreateTime(java.time.LocalDateTime.now().minusDays(1));
        a2.setUpdateTime(java.time.LocalDateTime.now().minusHours(10));
        q2.getAnswers().add(a2);
        q2.setAnswerCount(1);
        q2.setCreateTime(java.time.LocalDateTime.now().minusDays(2));
        q2.setUpdateTime(java.time.LocalDateTime.now().minusHours(5));

        Question q3 = new Question();
        q3.setTitle("ArrayList和LinkedList的区别是什么？");
        q3.setContent("面试经常问到这个问题，我想全面了解ArrayList和LinkedList的区别，包括底层实现、时间复杂度、适用场景等方面。");
        q3.setKnowledgePointId(kps.get(2).getId());
        q3.setKnowledgePointTitle(kps.get(2).getTitle());
        q3.setAuthor("求职者");
        q3.setViewCount(234);
        q3.setLikeCount(38);
        q3.setDifficulty("简单");

        Answer a3 = new Answer();
        a3.setId("ans_003");
        a3.setContent("**ArrayList vs LinkedList 对比：**\n\n| 特性 | ArrayList | LinkedList |\n|------|-----------|------------|\n| 底层结构 | 动态数组 | 双向链表 |\n| 随机访问 | O(1) | O(n) |\n| 头部插入/删除 | O(n) | O(1) |\n| 尾部插入/删除 | 均摊O(1) | O(1) |\n| 内存占用 | 较少 | 较多（存储前后指针）|\n\n**适用场景：**\n- ArrayList：频繁随机访问、尾部操作多的场景\n- LinkedList：频繁头部插入删除的场景\n\n**注意：** 实际开发中90%的情况都用ArrayList，因为CPU缓存对数组更友好。");
        a3.setAuthor("Java导师");
        a3.setLikeCount(56);
        a3.setIsAccepted(true);
        a3.setCreateTime(java.time.LocalDateTime.now().minusDays(5));
        a3.setUpdateTime(java.time.LocalDateTime.now().minusDays(1));
        q3.getAnswers().add(a3);
        q3.setAnswerCount(1);
        q3.setCreateTime(java.time.LocalDateTime.now().minusWeeks(1));
        q3.setUpdateTime(java.time.LocalDateTime.now().minusDays(2));

        questionRepository.save(q1);
        questionRepository.save(q2);
        questionRepository.save(q3);

        System.out.println("✅ 问题数据初始化完成！");
    }
}
