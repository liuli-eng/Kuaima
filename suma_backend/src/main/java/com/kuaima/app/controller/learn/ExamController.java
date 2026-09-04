package com.kuaima.app.controller.learn;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kuaima.app.common.Result;
import com.kuaima.app.domain.course.entity.Exam;
import com.kuaima.app.domain.course.entity.ExamQuestion;
import com.kuaima.app.domain.course.entity.ExamResult;
import com.kuaima.app.domain.course.repository.ExamQuestionRepository;
import com.kuaima.app.domain.course.repository.ExamRepository;
import com.kuaima.app.domain.course.repository.ExamResultRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 课程考试。
 */
@RestController
@RequestMapping("/exams")
public class ExamController {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository questionRepository;
    private final ExamResultRepository resultRepository;

    public ExamController(ExamRepository examRepository,
                          ExamQuestionRepository questionRepository,
                          ExamResultRepository resultRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
    }

    /** 获取考试（含题目）：GET /exams/{courseId} */
    @GetMapping("/{courseId}")
    public Result<Map<String, Object>> getExam(@PathVariable Long courseId) {
        Exam exam = examRepository.findByCourseId(courseId)
                .orElseThrow(() -> new EntityNotFoundException("该课程暂无考试"));
        List<ExamQuestion> questions = questionRepository.findByExamId(exam.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("exam", exam);
        data.put("questions", questions);
        return Result.success(data);
    }

    /** 提交考试：POST /exams/{courseId}/submit  body: { "userId": 1, "answers": {"1":"A","2":"B"} } */
    @PostMapping("/{courseId}/submit")
    @Transactional
    public Result<ExamResult> submitExam(@PathVariable Long courseId, @RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        Exam exam = examRepository.findByCourseId(courseId)
                .orElseThrow(() -> new EntityNotFoundException("该课程暂无考试"));
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.get("answers");
        List<ExamQuestion> questions = questionRepository.findByExamId(exam.getId());
        int score = 0;
        for (ExamQuestion q : questions) {
            String userAnswer = answers != null ? answers.get(String.valueOf(q.getId())) : null;
            if (q.getAnswer() != null && q.getAnswer().equalsIgnoreCase(userAnswer)) {
                score += q.getScore() != null ? q.getScore() : 0;
            }
        }
        int passScore = exam.getPassScore() != null ? exam.getPassScore() : 60;
        ExamResult result = new ExamResult();
        result.setUserId(userId);
        result.setExamId(exam.getId());
        result.setScore(score);
        result.setPassed(score >= passScore);
        result.setTakenAt(new Timestamp(System.currentTimeMillis()));
        return Result.success(resultRepository.save(result));
    }

    /** 考试结果：GET /exams/result?userId=1&examId=1 */
    @GetMapping("/result")
    public Result<ExamResult> getResult(@RequestParam Long userId, @RequestParam Long examId) {
        return Result.success(resultRepository.findFirstByUserIdAndExamIdOrderByIdDesc(userId, examId)
                .orElse(null));
    }
}
