package com.kuaima.app.domain.academy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.kuaima.app.domain.academy.entity.AcademyLesson;
import com.kuaima.app.domain.academy.entity.AcademySimulateVideo;
import com.kuaima.app.domain.academy.entity.AcademyQuiz;
import com.kuaima.app.domain.academy.model.AcademyModels.LessonVideoRequest;
import com.kuaima.app.domain.academy.model.AcademyModels.QuizRequest;
import com.kuaima.app.domain.academy.model.AcademyModels.SimulateVideoRequest;
import com.kuaima.app.domain.academy.repository.AcademyLessonRepository;
import com.kuaima.app.domain.academy.repository.AcademyQuizRepository;
import com.kuaima.app.domain.academy.repository.AcademySimulateVideoRepository;

class AcademyAdminServiceTests {
    @TempDir
    Path uploadDir;

    @Test
    void createQuizShouldNormalizeOptionsAndAnswerIndexes() {
        var videos = mock(AcademySimulateVideoRepository.class);
        var quizzes = mock(AcademyQuizRepository.class);
        var lessons = mock(AcademyLessonRepository.class);
        when(quizzes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new AcademyAdminService(videos, quizzes, lessons, uploadDir.toString());

        var result = service.createQuiz(new QuizRequest(null, "MULTI", 15, "题干",
                List.of("A", "B", "C"), List.of(2, 0), 3));

        assertEquals("multi", result.get("type"));
        assertEquals(List.of("A", "B", "C"), result.get("options"));
        assertEquals(List.of(0, 2), result.get("answer"));
    }

    @Test
    void createQuizShouldRejectInvalidAnswer() {
        var quizzes = mock(AcademyQuizRepository.class);
        when(quizzes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new AcademyAdminService(mock(AcademySimulateVideoRepository.class),
                quizzes, mock(AcademyLessonRepository.class), uploadDir.toString());

        assertThrows(IllegalArgumentException.class, () -> service.createQuiz(
                new QuizRequest(null, "single", 10, "题干", List.of("A", "B"), List.of(0, 1), 0)));
    }

    @Test
    void lessonVideoShouldRequireKnownLessonKey() {
        var lessons = mock(AcademyLessonRepository.class);
        when(lessons.findByLessonKey("unknown")).thenReturn(Optional.empty());
        var service = new AcademyAdminService(mock(AcademySimulateVideoRepository.class),
                mock(AcademyQuizRepository.class), lessons, uploadDir.toString());

        assertThrows(jakarta.persistence.EntityNotFoundException.class, () ->
                service.uploadLessonVideo("unknown", new LessonVideoRequest("标题", "/uploads/a.mp4", 10, 100L, "mp4")));
    }

    @Test
    void uploadMp4ShouldSaveAndParseDuration() {
        var videos = mock(AcademySimulateVideoRepository.class);
        var lessons = mock(AcademyLessonRepository.class);
        AcademyLesson lesson = new AcademyLesson();
        lesson.setLessonKey("find");
        lesson.setTitle("如何浏览和报名岗位");
        lesson.setDescription("筛选岗位");
        when(lessons.findByLessonKey("find")).thenReturn(Optional.of(lesson));
        when(lessons.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new AcademyAdminService(videos, mock(AcademyQuizRepository.class), lessons, uploadDir.toString());
        byte[] mp4 = mp4WithDuration(100, 10);

        var upload = service.upload(new MockMultipartFile("file", "demo.mp4", "video/mp4", mp4), "测试视频", "lesson");
        var saved = service.uploadLessonVideo("find", new LessonVideoRequest("测试视频",
                upload.url(), upload.duration(), upload.size(), upload.ext()));

        assertEquals("mp4", upload.ext());
        assertEquals(10, upload.duration());
        assertEquals(upload.url(), saved.get("video"));
        assertEquals(10, saved.get("duration"));
    }

    @Test
    void uploadWebmShouldParseDurationFromSegmentInfo() {
        var videos = mock(AcademySimulateVideoRepository.class);
        var lessons = mock(AcademyLessonRepository.class);
        when(lessons.findAllByOrderByIdAsc()).thenReturn(List.of());
        var service = new AcademyAdminService(videos, mock(AcademyQuizRepository.class),
                lessons, uploadDir.toString());
        byte[] webm = new byte[]{
                0x1f, 0x43, (byte) 0xb6, 0x75, 0x01, 0x00, 0x00,
                0x2a, (byte) 0xd7, (byte) 0xb1, (byte) 0x84, 0x00, 0x0f, 0x42, 0x40,
                0x44, (byte) 0x89, (byte) 0x84, 0x46, 0x1c, 0x40, 0x00
        };

        var upload = service.upload(new MockMultipartFile("file", "demo.webm", "video/webm", webm),
                "WebM视频", "lesson");

        assertEquals("webm", upload.ext());
        assertEquals(10, upload.duration());
    }

    @Test
    void simulateVideoShouldRejectUnsupportedExt() {
        var service = new AcademyAdminService(mock(AcademySimulateVideoRepository.class),
                mock(AcademyQuizRepository.class), mock(AcademyLessonRepository.class), uploadDir.toString());

        assertThrows(IllegalArgumentException.class, () -> service.createSimulateVideo(
                new SimulateVideoRequest(null, "标题", "/uploads/a.avi", 1, 1L, "avi", 1, true, 0L)));
    }

    @Test
    void toggleShouldAcceptExplicitTargetStateForIdempotentOperation() {
        var videos = mock(AcademySimulateVideoRepository.class);
        AcademySimulateVideo video = new AcademySimulateVideo();
        video.setId(1L);
        video.setEnabled(true);
        when(videos.findById(1L)).thenReturn(Optional.of(video));
        when(videos.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new AcademyAdminService(videos, mock(AcademyQuizRepository.class),
                mock(AcademyLessonRepository.class), uploadDir.toString());

        var result = service.toggleSimulateVideo(1L, false);

        assertEquals(false, result.get("enabled"));
    }

    private byte[] mp4WithDuration(long duration, int timescale) {
        byte[] ftyp = atom("ftyp", new byte[]{'i', 's', 'o', 'm'});
        byte[] mvhdBody = new byte[20];
        putUnsignedInt(mvhdBody, 12, timescale);
        putUnsignedInt(mvhdBody, 16, duration);
        byte[] mvhd = atom("mvhd", mvhdBody);
        byte[] moov = atom("moov", mvhd);
        byte[] result = new byte[ftyp.length + moov.length];
        System.arraycopy(ftyp, 0, result, 0, ftyp.length);
        System.arraycopy(moov, 0, result, ftyp.length, moov.length);
        return result;
    }

    private byte[] atom(String type, byte[] body) {
        byte[] result = new byte[8 + body.length];
        putUnsignedInt(result, 0, result.length);
        System.arraycopy(type.getBytes(), 0, result, 4, 4);
        System.arraycopy(body, 0, result, 8, body.length);
        return result;
    }

    private void putUnsignedInt(byte[] target, int offset, long value) {
        for (int i = 0; i < 4; i++) target[offset + i] = (byte) ((value >>> (24 - i * 8)) & 0xff);
    }
}
