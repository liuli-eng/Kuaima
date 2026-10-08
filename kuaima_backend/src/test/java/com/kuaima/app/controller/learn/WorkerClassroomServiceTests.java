package com.kuaima.app.controller.learn;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;
import com.kuaima.app.domain.course.entity.*;
import com.kuaima.app.domain.course.repository.*;
import com.kuaima.app.domain.academy.entity.AcademyQuiz;
import com.kuaima.app.domain.academy.entity.AcademySimulateVideo;
import com.kuaima.app.domain.academy.entity.AcademyLesson;
import com.kuaima.app.domain.academy.repository.AcademyQuizRepository;
import com.kuaima.app.domain.academy.repository.AcademySimulateVideoRepository;
import com.kuaima.app.domain.academy.repository.AcademyLessonRepository;
import com.kuaima.app.common.service.OssStorageService;
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

    @Test
    void overviewReturnsOnlyEnabledSimulateVideosWithPlayableUrls() {
        CourseRepository courses = mock(CourseRepository.class);
        CourseVideoRepository videos = mock(CourseVideoRepository.class);
        ExamRepository exams = mock(ExamRepository.class);
        ExamQuestionRepository questions = mock(ExamQuestionRepository.class);
        RulesRepository rules = mock(RulesRepository.class);
        AcademyQuizRepository quizzes = mock(AcademyQuizRepository.class);
        AcademySimulateVideoRepository academyVideos = mock(AcademySimulateVideoRepository.class);
        OssStorageService storage = mock(OssStorageService.class);
        when(courses.findByStatusOrderBySortOrderAscIdAsc("上架")).thenReturn(List.of());
        when(courses.findByStatusOrderBySortOrderAscIdAsc("已发布")).thenReturn(List.of());
        when(rules.findByStatusIn(any())).thenReturn(List.of());
        when(quizzes.findAllByOrderBySortAscIdAsc()).thenReturn(List.of());
        AcademySimulateVideo later = video(2L, "第二步", "oss://second", 2, true);
        AcademySimulateVideo disabled = video(3L, "已禁用", "oss://disabled", 0, false);
        AcademySimulateVideo first = video(1L, "第一步", "oss://first", 1, true);
        when(academyVideos.findByEnabledTrueOrderBySortAscIdAsc())
                .thenReturn(List.of(later, disabled, first));
        when(storage.playableUrl("oss://first")).thenReturn("https://play/first");
        when(storage.playableUrl("oss://second")).thenReturn("https://play/second");
        WorkerClassroomService service = new WorkerClassroomService(courses, videos, exams, questions,
                rules, quizzes, academyVideos, storage);

        var result = service.overview();
        @SuppressWarnings("unchecked")
        var rows = (List<Map<String, Object>>) result.get("simulateVideos");

        assertEquals(List.of(1L, 2L), rows.stream().map(row -> row.get("id")).toList());
        assertEquals("https://play/first", rows.get(0).get("url"));
        assertEquals(List.of("id", "title", "url", "duration", "learners"),
                rows.get(0).keySet().stream().toList());
        verify(academyVideos).findByEnabledTrueOrderBySortAscIdAsc();
        verify(storage, never()).playableUrl("oss://disabled");
    }

    @Test
    void overviewReturnsEnabledAcademyLessonsWithPlayableVideoUrls() {
        CourseRepository courses = mock(CourseRepository.class);
        CourseVideoRepository videos = mock(CourseVideoRepository.class);
        ExamRepository exams = mock(ExamRepository.class);
        ExamQuestionRepository questions = mock(ExamQuestionRepository.class);
        RulesRepository rules = mock(RulesRepository.class);
        AcademyQuizRepository quizzes = mock(AcademyQuizRepository.class);
        AcademySimulateVideoRepository academyVideos = mock(AcademySimulateVideoRepository.class);
        AcademyLessonRepository lessons = mock(AcademyLessonRepository.class);
        OssStorageService storage = mock(OssStorageService.class);
        when(courses.findByStatusOrderBySortOrderAscIdAsc("上架")).thenReturn(List.of());
        when(courses.findByStatusOrderBySortOrderAscIdAsc("已发布")).thenReturn(List.of());
        when(rules.findByStatusIn(any())).thenReturn(List.of());
        when(quizzes.findAllByOrderBySortAscIdAsc()).thenReturn(List.of());
        when(academyVideos.findByEnabledTrueOrderBySortAscIdAsc()).thenReturn(List.of());
        AcademyLesson enabled = lesson(1L, "find", "如何浏览和报名岗位", "oss://lesson", true);
        AcademyLesson disabled = lesson(2L, "work", "接单后如何工作", "oss://disabled", false);
        when(lessons.findAllByOrderByIdAsc()).thenReturn(List.of(enabled, disabled));
        when(storage.playableUrl("oss://lesson")).thenReturn("https://play/lesson");
        WorkerClassroomService service = new WorkerClassroomService(courses, videos, exams, questions,
                rules, quizzes, academyVideos, lessons, storage);

        var result = service.overview();
        @SuppressWarnings("unchecked")
        var rows = (List<Map<String, Object>>) result.get("howToOrder");

        assertEquals(1, rows.size());
        assertEquals("find", rows.get(0).get("key"));
        assertEquals("https://play/lesson", rows.get(0).get("video"));
        assertEquals("https://play/lesson", rows.get(0).get("url"));
        verify(storage, never()).playableUrl("oss://disabled");
    }

    private AcademyQuiz quiz(Long id, int score, String answer, int sort) {
        AcademyQuiz quiz = new AcademyQuiz();
        quiz.setId(id); quiz.setScore(score); quiz.setAnswer(answer); quiz.setSort(sort);
        quiz.setType("single"); quiz.setStem("题目" + id); quiz.setOptions("[\"A\",\"B\"]");
        return quiz;
    }

    private AcademySimulateVideo video(Long id, String title, String url, int sort, boolean enabled) {
        AcademySimulateVideo video = new AcademySimulateVideo();
        video.setId(id); video.setTitle(title); video.setUrl(url); video.setSort(sort);
        video.setEnabled(enabled); video.setDuration(30); video.setLearners(5L);
        return video;
    }

    private AcademyLesson lesson(Long id, String key, String title, String video, boolean enabled) {
        AcademyLesson lesson = new AcademyLesson();
        lesson.setId(id); lesson.setLessonKey(key); lesson.setTitle(title);
        lesson.setDescription("课程说明"); lesson.setVideo(video); lesson.setEnabled(enabled);
        lesson.setDuration(60); lesson.setLearners(10L);
        return lesson;
    }
}
