package com.kuaima.app.controller.learn;

import com.kuaima.app.admin.entity.Rules;
import com.kuaima.app.admin.repository.RulesRepository;
import com.kuaima.app.domain.course.entity.Course;
import com.kuaima.app.domain.course.entity.CourseVideo;
import com.kuaima.app.domain.course.entity.Exam;
import com.kuaima.app.domain.course.entity.ExamQuestion;
import com.kuaima.app.domain.course.repository.CourseRepository;
import com.kuaima.app.domain.course.repository.CourseVideoRepository;
import com.kuaima.app.domain.course.repository.ExamQuestionRepository;
import com.kuaima.app.domain.course.repository.ExamRepository;
import com.kuaima.app.domain.academy.entity.AcademyQuiz;
import com.kuaima.app.domain.academy.repository.AcademyQuizRepository;
import com.alibaba.fastjson2.JSONArray;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WorkerClassroomService {
    public static final int DEFAULT_PASS_SCORE = 60;
    private static final List<String> PUBLISHED = List.of("已发布", "published");
    private final CourseRepository courses;
    private final CourseVideoRepository videos;
    private final ExamRepository exams;
    private final ExamQuestionRepository questions;
    private final RulesRepository rules;
    private final AcademyQuizRepository academyQuizzes;

    @Autowired
    public WorkerClassroomService(CourseRepository courses, CourseVideoRepository videos,
                                  ExamRepository exams, ExamQuestionRepository questions,
                                  RulesRepository rules, AcademyQuizRepository academyQuizzes) {
        this.courses = courses; this.videos = videos; this.exams = exams;
        this.questions = questions; this.rules = rules; this.academyQuizzes = academyQuizzes;
    }

    /** 保留旧测试/调用方构造方式；生产 Bean 使用包含题库仓库的构造器。 */
    public WorkerClassroomService(CourseRepository courses, CourseVideoRepository videos,
                                  ExamRepository exams, ExamQuestionRepository questions,
                                  RulesRepository rules) {
        this(courses, videos, exams, questions, rules, null);
    }

    public Map<String, Object> overview() {
        List<Course> publishedCourses = courses.findByStatusOrderBySortOrderAscIdAsc("上架");
        if (publishedCourses.isEmpty()) publishedCourses = courses.findByStatusOrderBySortOrderAscIdAsc("已发布");
        Map<Long, List<CourseVideo>> videoMap = new HashMap<>();
        for (Course course : publishedCourses) videoMap.put(course.getId(), videos.findByCourseIdOrderBySortOrderAsc(course.getId()));
        List<Long> courseIds = publishedCourses.stream().map(Course::getId).filter(Objects::nonNull).toList();
        List<Exam> examRows = courseIds.isEmpty() ? List.of() : exams.findByCourseIdIn(courseIds);
        List<Long> examIds = examRows.stream().map(Exam::getId).filter(Objects::nonNull).toList();
        List<ExamQuestion> questionRows = examIds.isEmpty() ? List.of() : questions.findByExamIdIn(examIds);
        Map<Long, List<ExamQuestion>> questionMap = questionRows.stream().filter(q -> q.getExamId() != null)
                .collect(java.util.stream.Collectors.groupingBy(ExamQuestion::getExamId));
        List<Map<String, Object>> courseRows = publishedCourses.stream().map(c -> courseView(c, videoMap.getOrDefault(c.getId(), List.of()))).toList();
        List<Map<String, Object>> examViews = examRows.stream().map(e -> examView(e, questionMap.getOrDefault(e.getId(), List.of()))).toList();
        List<Rules> publishedRules = rules.findByStatusIn(PUBLISHED);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("simulateOrder", selectCourses(courseRows, "simulate", "模拟", "体验"));
        data.put("learningRules", selectRules(publishedRules, "学习规则", "平台规则"));
        List<AcademyQuiz> quizRows = academyQuizRows();
        Map<String, Object> quiz = new LinkedHashMap<>();
        quiz.put("questions", quizRows.stream().map(this::quizQuestionView).toList());
        quiz.put("passScore", DEFAULT_PASS_SCORE);
        quiz.put("exams", examViews);
        data.put("quiz", quiz);
        data.put("howToOrder", selectRules(publishedRules, "如何接单", "接单"));
        data.put("platformRules", selectRules(publishedRules, "平台规则", "交易规则"));
        data.put("courses", courseRows);
        return data;
    }

    public Map<String, Object> quiz() {
        List<AcademyQuiz> rows = academyQuizRows();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("questions", rows.stream().map(this::quizQuestionView).toList());
        data.put("passScore", DEFAULT_PASS_SCORE);
        return data;
    }

    public Map<String, Object> submitQuiz(Map<String, ?> answers) {
        List<AcademyQuiz> rows = academyQuizRows();
        int score = 0;
        int total = 0;
        for (AcademyQuiz row : rows) {
            int points = row.getScore() == null ? 0 : row.getScore();
            total += points;
            Object submitted = answers == null ? null : answers.get(String.valueOf(row.getId()));
            if (matchesAnswer(row, submitted)) score += points;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", score);
        result.put("total", total);
        result.put("passed", score >= DEFAULT_PASS_SCORE);
        return result;
    }

    private Map<String, Object> quizQuestionView(AcademyQuiz q) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", q.getId());
        view.put("type", q.getType());
        view.put("score", q.getScore());
        view.put("stem", q.getStem());
        view.put("options", JSONArray.parseArray(q.getOptions()));
        view.put("sort", q.getSort());
        return view;
    }

    private List<AcademyQuiz> academyQuizRows() {
        return academyQuizzes == null ? List.of() : academyQuizzes.findAllByOrderBySortAscIdAsc();
    }

    private boolean matchesAnswer(AcademyQuiz question, Object submitted) {
        if (submitted == null || !StringUtils.hasText(question.getAnswer())) return false;
        List<Integer> expected = JSONArray.parseArray(question.getAnswer(), Integer.class);
        List<Integer> actual = answerIndexes(submitted);
        return expected != null && actual != null && expected.equals(actual);
    }

    private List<Integer> answerIndexes(Object answer) {
        List<String> values = new ArrayList<>();
        if (answer instanceof Collection<?> collection) {
            collection.forEach(value -> values.add(String.valueOf(value)));
        } else {
            String raw = String.valueOf(answer).trim();
            if (raw.startsWith("[") && raw.endsWith("]")) {
                JSONArray.parseArray(raw).forEach(value -> values.add(String.valueOf(value)));
            } else {
                values.addAll(Arrays.stream(raw.split("[,，\\s]+"))
                        .filter(StringUtils::hasText).toList());
            }
        }
        return values.stream().map(String::trim).filter(StringUtils::hasText)
                .map(value -> value.matches("\\d+") ? Integer.valueOf(value) : letterIndex(value))
                .filter(Objects::nonNull).distinct().sorted().toList();
    }

    private Integer letterIndex(String value) {
        String upper = value.toUpperCase(Locale.ROOT);
        if (upper.length() != 1 || upper.charAt(0) < 'A' || upper.charAt(0) > 'Z') return null;
        return upper.charAt(0) - 'A';
    }

    private Map<String, Object> courseView(Course c, List<CourseVideo> rows) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", c.getId()); view.put("title", c.getTitle()); view.put("category", c.getCategory());
        view.put("coverUrl", c.getCoverUrl()); view.put("intro", c.getIntro()); view.put("sortOrder", c.getSortOrder()); view.put("status", c.getStatus());
        view.put("videos", rows.stream().map(v -> { Map<String,Object> x = new LinkedHashMap<>();
            x.put("id", v.getId()); x.put("title", v.getTitle()); x.put("videoUrl", v.getVideoUrl()); x.put("duration", v.getDuration()); x.put("sortOrder", v.getSortOrder()); return x; }).toList());
        return view;
    }

    private Map<String, Object> examView(Exam e, List<ExamQuestion> rows) {
        Map<String, Object> view = new LinkedHashMap<>(); view.put("id", e.getId()); view.put("courseId", e.getCourseId());
        view.put("title", e.getTitle()); view.put("passScore", e.getPassScore());
        view.put("questions", rows.stream().map(q -> { Map<String,Object> x = new LinkedHashMap<>();
            x.put("id", q.getId()); x.put("content", q.getContent()); x.put("options", splitOptions(q.getOptions())); x.put("score", q.getScore()); return x; }).toList());
        return view;
    }

    private List<Map<String, Object>> selectCourses(List<Map<String, Object>> values, String... terms) {
        return values.stream().filter(v -> containsAny(v.get("category"), v.get("title"), terms)).toList();
    }
    private List<Rules> selectRules(List<Rules> values, String... terms) {
        return values.stream().filter(v -> containsAny(v.getCategory(), v.getTitle(), terms)).toList();
    }
    private boolean containsAny(Object a, Object b, String... terms) {
        String text = (String.valueOf(a == null ? "" : a) + " " + String.valueOf(b == null ? "" : b)).toLowerCase();
        return Arrays.stream(terms).anyMatch(t -> text.contains(t.toLowerCase()));
    }
    private List<String> splitOptions(String value) {
        if (!StringUtils.hasText(value)) return List.of();
        return Arrays.stream(value.replace('|', '\n').split("\\r?\\n|[,，]"))
                .map(String::trim).filter(StringUtils::hasText).toList();
    }
}
