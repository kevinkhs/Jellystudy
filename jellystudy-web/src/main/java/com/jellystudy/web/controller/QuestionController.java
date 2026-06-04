package com.jellystudy.web.controller;

import com.jellystudy.api.entity.*;
import com.jellystudy.api.service.EvaluationService;
import com.jellystudy.api.service.KnowledgePointService;
import com.jellystudy.api.service.QuestionService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/question")
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @DubboReference(version = "1.0.0", group = "question", check = false)
    private QuestionService questionService;

    @DubboReference(version = "1.0.0", group = "knowledge-point", check = false)
    private KnowledgePointService knowledgePointService;

    @DubboReference(version = "1.0.0", group = "evaluation", timeout = 60000, check = false)
    private EvaluationService evaluationService;

    @GetMapping("/list")
    public String list(Model model) {
        try {
            List<Question> questions = questionService.getAllQuestions();
            model.addAttribute("questions", questions);
        } catch (Exception e) {
            logger.error("获取问题列表失败: {}", e.getMessage());
            model.addAttribute("questions", java.util.Collections.emptyList());
        }
        return "question/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        List<KnowledgePoint> knowledgePoints = knowledgePointService.getAllKnowledgePoints();
        model.addAttribute("knowledgePoints", knowledgePoints);
        return "question/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Question question) {
        KnowledgePoint kp = knowledgePointService.getKnowledgePointById(question.getKnowledgePointId());
        if (kp != null) {
            question.setKnowledgePointTitle(kp.getTitle());
        }

        Question created = questionService.createQuestion(question);

        try {
            evaluationService.evaluateQuestion(
                created.getId(),
                created.getTitle(),
                created.getContent()
            );
            logger.info("问题已创建并触发AI评估: questionId={}", created.getId());
        } catch (Exception e) {
            logger.warn("AI评估调用失败（不影响问题创建）: {}", e.getMessage());
        }

        return "redirect:/question/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable String id, Model model) {
        try {
            Question question = questionService.getQuestionById(id);
            model.addAttribute("question", question);

            try {
                List<Evaluation> evaluations = evaluationService.getEvaluationsByTargetId(id);
                Evaluation latestEval = null;
                if (evaluations != null && !evaluations.isEmpty()) {
                    latestEval = evaluations.stream()
                        .max(Comparator.comparing(Evaluation::getCreateTime))
                        .orElse(null);
                    if (latestEval == null) {
                        latestEval = evaluations.get(evaluations.size() - 1);
                    }
                }
                model.addAttribute("latestEval", latestEval);
            } catch (Exception e) {
                logger.warn("获取问题评估失败: {}", e.getMessage());
            }

            try {
                if (question != null && question.getAnswers() != null) {
                    java.util.Map<String, Evaluation> answerEvals = new java.util.HashMap<>();
                    for (Answer answer : question.getAnswers()) {
                        try {
                            List<Evaluation> ansEvals = evaluationService.getEvaluationsByTargetId(answer.getId());
                            if (ansEvals != null && !ansEvals.isEmpty()) {
                                Evaluation latestAnsEval = ansEvals.stream()
                                    .max(Comparator.comparing(Evaluation::getCreateTime))
                                    .orElse(null);
                                if (latestAnsEval != null) {
                                    answerEvals.put(answer.getId(), latestAnsEval);
                                }
                            }
                        } catch (Exception ex) {
                            logger.warn("获取回答评估失败: answerId={}, error={}", answer.getId(), ex.getMessage());
                        }
                    }
                    model.addAttribute("answerEvals", answerEvals);
                }
            } catch (Exception e) {
                logger.warn("获取回答评估列表失败: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("获取问题详情失败: {}", e.getMessage());
            return "redirect:/question/list";
        }

        return "question/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        Question question = questionService.getQuestionById(id);
        model.addAttribute("question", question);
        return "question/edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable String id, @ModelAttribute Question question) {
        questionService.updateQuestion(id, question);
        return "redirect:/question/" + id + "#answers-section";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return "redirect:/question/list";
    }

    @PostMapping("/{id}/answer")
    public String addAnswer(@PathVariable String id, @ModelAttribute Answer answer) {
        Answer created = questionService.addAnswer(id, answer);

        try {
            evaluationService.evaluateAnswer(
                id,
                created.getId(),
                created.getContent(),
                created.getAuthor()
            );
            logger.info("回答已创建并触发AI评估: answerId={}", created.getId());
        } catch (Exception e) {
            logger.warn("AI评估调用失败（不影响回答创建）: {}", e.getMessage());
        }

        return "redirect:/question/" + id + "#answers-section";
    }

    @PostMapping("/{qid}/answer/{aid}/update")
    public String updateAnswer(@PathVariable String qid, @PathVariable String aid,
                              @ModelAttribute Answer answer) {
        questionService.updateAnswer(qid, aid, answer);
        return "redirect:/question/" + qid + "#answer-" + aid;
    }

    @GetMapping("/{qid}/answer/{aid}/edit")
    public String editAnswerForm(@PathVariable String qid, @PathVariable String aid, Model model) {
        Question question = questionService.getQuestionById(qid);
        if (question == null) {
            return "redirect:/question/list";
        }

        Answer targetAnswer = null;
        if (question.getAnswers() != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(aid)) {
                    targetAnswer = answer;
                    break;
                }
            }
        }

        if (targetAnswer == null) {
            return "redirect:/question/" + qid;
        }

        model.addAttribute("questionId", qid);
        model.addAttribute("answer", targetAnswer);
        return "question/edit-answer";
    }

    @PostMapping("/{qid}/answer/{aid}/delete")
    public String deleteAnswer(@PathVariable String qid, @PathVariable String aid) {
        questionService.deleteAnswer(qid, aid);
        return "redirect:/question/" + qid + "#answers-section";
    }

    @PostMapping("/{qid}/answer/{aid}/like")
    public String likeAnswer(@PathVariable String qid, @PathVariable String aid,
                           @RequestParam(defaultValue = "user_001") String userId) {
        try {
            questionService.likeEntity(userId, "answer", aid);
        } catch (Exception e) {
            logger.info("取消点赞: answerId={}, userId={}", aid, userId);
            questionService.unlikeEntity(userId, "answer", aid);
        }
        return "redirect:/question/" + qid + "#answer-" + aid;
    }

    @PostMapping("/{qid}/answer/{aid}/comment/add")
    public String addComment(@PathVariable String qid, @PathVariable String aid,
                            @ModelAttribute Comment comment) {
        questionService.addComment(qid, aid, comment);
        return "redirect:/question/" + qid + "#answer-" + aid;
    }

    @PostMapping("/{qid}/answer/{aid}/comment/{cid}/update")
    public String updateComment(@PathVariable String qid, @PathVariable String aid,
                               @PathVariable String cid, @ModelAttribute Comment comment) {
        questionService.updateComment(qid, aid, cid, comment);
        return "redirect:/question/" + qid + "#answer-" + aid;
    }

    @PostMapping("/{qid}/answer/{aid}/comment/{cid}/delete")
    public String deleteComment(@PathVariable String qid, @PathVariable String aid,
                               @PathVariable String cid) {
        questionService.deleteComment(qid, aid, cid);
        return "redirect:/question/" + qid + "#answer-" + aid;
    }

    @PostMapping("/{qid}/answer/{aid}/comment/{cid}/like")
    @ResponseBody
    public Map<String, Object> likeComment(@PathVariable String qid, @PathVariable String aid,
                                          @PathVariable String cid,
                                          @RequestParam(defaultValue = "user_001") String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            questionService.likeEntity(userId, "comment", cid);
            result.put("success", true);
            result.put("message", "点赞成功");
            logger.info("评论点赞成功: commentId={}, userId={}", cid, userId);
        } catch (Exception e) {
            questionService.unlikeEntity(userId, "comment", cid);
            result.put("success", true);
            result.put("message", "取消点赞");
            logger.info("取消评论点赞: commentId={}, userId={}", cid, userId);
        }
        return result;
    }

    @PostMapping("/{id}/like")
    public String likeQuestion(@PathVariable String id,
                              @RequestParam(defaultValue = "user_001") String userId) {
        try {
            questionService.likeEntity(userId, "question", id);
        } catch (Exception e) {
            logger.info("取消点赞: questionId={}, userId={}", id, userId);
            questionService.unlikeEntity(userId, "question", id);
        }
        return "redirect:/question/" + id;
    }

    @PostMapping("/{id}/evaluate")
    public String evaluateQuestion(@PathVariable String id, Model model) {
        Question question = questionService.getQuestionById(id);
        
        try {
            Evaluation evaluation = evaluationService.evaluateQuestion(
                id,
                question.getTitle(),
                question.getContent()
            );
            
            if (evaluation != null) {
                model.addAttribute("evalMsg", "✅ 问题评估完成！知识点: " + 
                    (evaluation.getExtractedKnowledgePoint() != null ? evaluation.getExtractedKnowledgePoint() : "已提取") + 
                    ", 难度: " + (evaluation.getDifficultyLevel() != null ? evaluation.getDifficultyLevel() : "已评估"));
                logger.info("手动触发问题评估成功: questionId={}", id);
            } else {
                model.addAttribute("evalError", "❌ 评估失败，请检查评估服务是否正常运行");
            }
        } catch (Exception e) {
            logger.error("手动触发问题评估异常: questionId={}", id, e);
            model.addAttribute("evalError", "❌ 评估服务调用失败: " + e.getMessage());
        }

        return "redirect:/question/" + id;
    }

    @PostMapping("/{qid}/answer/{aid}/evaluate")
    public String evaluateAnswer(@PathVariable String qid, @PathVariable String aid, Model model) {
        Question question = questionService.getQuestionById(qid);
        Answer targetAnswer = null;
        
        if (question != null && question.getAnswers() != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(aid)) {
                    targetAnswer = answer;
                    break;
                }
            }
        }
        
        if (targetAnswer == null) {
            model.addAttribute("evalError", "❌ 未找到回答");
            return "redirect:/question/" + qid + "#answer-" + aid;
        }
        
        try {
            Evaluation evaluation = evaluationService.evaluateAnswer(
                qid,
                aid,
                targetAnswer.getContent(),
                targetAnswer.getAuthor()
            );
            
            if (evaluation != null) {
                model.addAttribute("evalMsg", "✅ 回答评分完成！得分: " + 
                    (evaluation.getScore() != null ? evaluation.getScore() : "已评分") + "分");
                logger.info("手动触发回答评估成功: answerId={}", aid);
            } else {
                model.addAttribute("evalError", "❌ 评分失败，请检查评估服务是否正常运行");
            }
        } catch (Exception e) {
            logger.error("手动触发回答评估异常: answerId={}", aid, e);
            model.addAttribute("evalError", "❌ 评分服务调用失败: " + e.getMessage());
        }

        return "redirect:/question/" + qid + "#answer-" + aid;
    }

    @PostMapping("/{id}/ai-answer")
    public String aiAnswer(@PathVariable String id, RedirectAttributes redirectAttributes) {
        logger.info("开始生成AI回答: questionId={}", id);
        
        Question question = questionService.getQuestionById(id);
        
        if (question == null) {
            logger.error("问题不存在: questionId={}", id);
            redirectAttributes.addFlashAttribute("error", "❌ 问题不存在");
            return "redirect:/question/" + id + "#answers-section";
        }
        
        logger.info("问题信息: title={}, contentLength={}", 
            question.getTitle(), 
            question.getContent() != null ? question.getContent().length() : 0);
        
        try {
            logger.info("调用评估服务generateAnswer方法...");
            String aiResponse = evaluationService.generateAnswer(
                question.getTitle(),
                question.getContent()
            );
            
            logger.info("评估服务返回: aiResponse={}, length={}", 
                aiResponse != null ? "非null" : "null",
                aiResponse != null ? aiResponse.length() : 0);
            
            if (aiResponse != null && !aiResponse.isEmpty()) {
                Answer aiAnswer = new Answer();
                aiAnswer.setContent(aiResponse);
                aiAnswer.setAuthor("博识尊");
                
                logger.info("保存AI回答到数据库...");
                questionService.addAnswer(id, aiAnswer);
                
                redirectAttributes.addFlashAttribute("evalMsg", "✅ AI回答生成成功！");
                logger.info("AI回答生成成功: questionId={}", id);
            } else {
                logger.warn("AI回答为空: questionId={}", id);
                redirectAttributes.addFlashAttribute("evalError", "❌ AI回答生成失败：返回内容为空，请检查DeepSeek API配置");
            }
        } catch (Exception e) {
            logger.error("AI回答生成异常: questionId={}", id, e);
            redirectAttributes.addFlashAttribute("evalError", "❌ AI回答服务调用失败: " + e.getMessage());
        }

        return "redirect:/question/" + id + "#answers-section";
    }

    @GetMapping("/{id}/debug-ai")
    @ResponseBody
    public String debugAi(@PathVariable String id) {
        StringBuilder result = new StringBuilder();
        result.append("=== AI回答调试信息 ===\n\n");

        Question question = questionService.getQuestionById(id);
        if (question == null) {
            return "问题不存在: " + id;
        }

        result.append("1. 问题信息:\n");
        result.append("   - ID: ").append(question.getId()).append("\n");
        result.append("   - 标题: ").append(question.getTitle()).append("\n");
        result.append("   - 内容长度: ").append(question.getContent() != null ? question.getContent().length() : 0).append("\n\n");

        result.append("2. 调用评估服务...\n");
        try {
            String aiResponse = evaluationService.generateAnswer(
                question.getTitle(),
                question.getContent()
            );

            result.append("3. 评估服务返回:\n");
            result.append("   - 值: ").append(aiResponse != null ? "非null" : "null").append("\n");
            if (aiResponse != null) {
                result.append("   - 长度: ").append(aiResponse.length()).append("\n");
                result.append("   - 是否为空: ").append(aiResponse.isEmpty()).append("\n");
                result.append("   - 内容预览(前200字): ").append(aiResponse.substring(0, Math.min(200, aiResponse.length()))).append("\n\n");

                if (!aiResponse.isEmpty()) {
                    result.append("4. 尝试保存AI回答...\n");
                    Answer aiAnswer = new Answer();
                    aiAnswer.setContent(aiResponse);
                    aiAnswer.setAuthor("博识尊");

                    questionService.addAnswer(id, aiAnswer);
                    result.append("   ✅ 保存成功!\n");
                }
            } else {
                result.append("   ❌ 返回值为null!\n");
            }
        } catch (Exception e) {
            result.append("❌ 调用失败:\n");
            result.append("   - 异常类型: ").append(e.getClass().getName()).append("\n");
            result.append("   - 异常消息: ").append(e.getMessage()).append("\n");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            result.append("   - 堆栈跟踪:\n").append(sw.toString());
        }

        return result.toString();
    }

    @GetMapping("/recommended")
    public String recommended(Model model) {
        List<Question> questions = questionService.getRecommendedQuestions(0, 20);
        model.addAttribute("questions", questions);
        return "question/recommended";
    }

    @GetMapping("/knowledge-point/{kpId}")
    public String byKnowledgePoint(@PathVariable String kpId, Model model) {
        var questions = questionService.getQuestionsByKnowledgePointId(kpId);
        var kp = knowledgePointService.getKnowledgePointById(kpId);
        model.addAttribute("questions", questions);
        model.addAttribute("knowledgePoint", kp);
        return "question/by-knowledge-point";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("totalQuestions", questionService.getTotalQuestionCount());
        model.addAttribute("totalAnswers", questionService.getTotalAnswerCount());
        model.addAttribute("totalComments", questionService.getTotalCommentCount());
        model.addAttribute("totalLikes", questionService.getTotalLikeCount());
        model.addAttribute("hotQuestions", questionService.getTopLikedQuestions(5));
        model.addAttribute("topAnswers", questionService.getTopLikedAnswers(5));
        return "question/statistics";
    }
}