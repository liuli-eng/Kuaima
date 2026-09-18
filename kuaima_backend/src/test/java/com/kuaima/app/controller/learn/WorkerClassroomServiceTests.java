package com.kuaima.app.controller.learn;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;
import com.kuaima.app.domain.course.entity.*;
import com.kuaima.app.domain.course.repository.*;
import com.kuaima.app.domain.academy.entity.AcademyQuiz;
import com.kuaima.app.domain.academy.repository.AcademyQuizRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WorkerClassroomServiceTests {
    @Test
    void overviewAggregatesPublishedContentAndHidesAnswers() {
        CourseRepository courses = mock(CourseRepository.class); CourseVideoRepository videos = mock(CourseVideoRepository.class);
        ExamRepository exams = mock(ExamRepository.class); ExamQuestionRepository questions = mock(ExamQuestionRepository.class); RulesRepository rules = mock(RulesRepository.class);
        Course course = new Course(); course.setId(1L); course.setTitle("模拟接单流程"); course.setCategory("simulate"); course.setStatus("上架");
        CourseVideo video = new CourseVideo(); video.setId(2L); video.setCourseId(1L); video.setVideoUrl("https://video");
        Exam exam = new Exam(); exam.setId(3L); exam.setCourseId(1L); exam.setTitle("接单测试");
        ExamQuestion question = new ExamQuestion(); question.setId(4L); question.setExamId(3L); question.setContent("第一题"); question.setOptions("A|B"); question.setAnswer("A");
        Rules how = new Rules(); how.setId(5L); how.setCategory("如何接单"); how.setTitle("接单规则"); how.setStatus("已发布");
        when(courses.findByStatusOrderBySortOrderAscIdAsc("上架")).thenReturn(List.of(course)); when(videos.findByCourseIdOrderBySortOrderAsc(1L)).thenReturn(List.of(video));
        when(exams.findByCourseIdIn(List.of(1L))).thenReturn(List.of(exam)); when(questions.findByExamIdIn(List.of(3L))).thenReturn(List.of(question));
        when(rules.findByStatusIn(List.of("已发布", "published"))).thenReturn(List.of(how));
        AcademyQuizRepository academyQuizzes = mock(AcademyQuizRepository.class);
        AcademyQuiz academyQuiz = quiz(9L, 10, "[0]", 1);
        when(academyQuizzes.findAllByOrderBySortAscIdAsc()).thenReturn(List.of(academyQuiz));
        var result = new WorkerClassroomService(courses, videos, exams, questions, rules, academyQuizzes).overview();
        assertEquals(1, ((List<?>) result.get("simulateOrder")).size());
        var quiz = (Map<?, ?>) result.get("quiz"); var examView = (Map<?, ?>) ((List<?>) quiz.get("exams")).get(0); var questionView = (Map<?, ?>) ((List<?>) examView.get("questions")).get(0);
        assertEquals(List.of("A", "B"), questionView.get("options")); assertFalse(questionView.containsKey("answer")); assertEquals(1, ((List<?>) result.get("howToOrder")).size());
        assertEquals(60, quiz.get("passScore"));
        assertEquals(1, ((List<?>) quiz.get("questions")).size());
    }

    @Test
    void academyQuizIsReturnedWithoutAnswersAndCanBeSubmittedByLetter() {
        AcademyQuizRepository quizzes = mock(AcademyQuizRepository.class);
        AcademyQuiz first = quiz(2L, 20, "[1]", 2);
        AcademyQuiz second = quiz(1L, 80, "[0]", 1);
        when(quizzes.findAllByOrderBySortAscIdAsc()).thenReturn(List.of(first, second));
        WorkerClassroomService service = new WorkerClassroomService(null, null, null, null, null, quizzes);

        Map<String, Object> view = service.quiz();
        assertEquals(60, view.get("passScore"));
        var questions = (List<?>) view.get("questions");
        assertEquals(2, questions.size());
        assertFalse(((Map<?, ?>) questions.get(0)).containsKey("answer"));
        assertEquals(Map.of("score", 100, "total", 100, "passed", true),
                service.submitQuiz(Map.of("1", "A", "2", "B")));
        assertEquals(Map.of("score", 80, "total", 100, "passed", true),
                service.submitQuiz(Map.of("1", "A", "2", "A")));
    }

    private AcademyQuiz quiz(Long id, int score, String answer, int sort) {
        AcademyQuiz quiz = new AcademyQuiz();
        quiz.setId(id); quiz.setScore(score); quiz.setAnswer(answer); quiz.setSort(sort);
        quiz.setType("single"); quiz.setStem("题目" + id); quiz.setOptions("[\"A\",\"B\"]");
        return quiz;
    }
}
