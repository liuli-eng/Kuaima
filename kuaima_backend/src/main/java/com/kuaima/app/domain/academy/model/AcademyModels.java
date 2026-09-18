package com.kuaima.app.domain.academy.model;

import java.util.List;

public final class AcademyModels {
    private AcademyModels() {}

    public record SimulateVideoRequest(Long id, String title, String url, Integer duration, Long size,
                                        String ext, Integer sort, Boolean enabled, Long learners) {}

    public record QuizRequest(Long id, String type, Integer score, String stem, List<String> options,
                              List<Integer> answer, Integer sort) {}

    public record LessonVideoRequest(String title, String url, Integer duration, Long size, String ext) {}

    public record UploadResponse(String url, String filename, long size, String ext, int duration) {}
}
