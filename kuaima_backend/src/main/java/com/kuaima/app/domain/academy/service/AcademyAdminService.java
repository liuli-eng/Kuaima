package com.kuaima.app.domain.academy.service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSONArray;
import com.kuaima.app.domain.academy.entity.AcademyLesson;
import com.kuaima.app.domain.academy.entity.AcademyQuiz;
import com.kuaima.app.domain.academy.entity.AcademySimulateVideo;
import com.kuaima.app.domain.academy.model.AcademyModels.LessonVideoRequest;
import com.kuaima.app.domain.academy.model.AcademyModels.QuizRequest;
import com.kuaima.app.domain.academy.model.AcademyModels.SimulateVideoRequest;
import com.kuaima.app.domain.academy.model.AcademyModels.UploadResponse;
import com.kuaima.app.domain.academy.repository.AcademyLessonRepository;
import com.kuaima.app.domain.academy.repository.AcademyQuizRepository;
import com.kuaima.app.domain.academy.repository.AcademySimulateVideoRepository;

import jakarta.persistence.EntityNotFoundException;

/** 管理后台学堂内容管理。 */
@Service
public class AcademyAdminService {
    private static final long MAX_VIDEO_SIZE = 200L * 1024 * 1024;
    private static final Set<String> VIDEO_EXTS = Set.of("mp4", "mov", "webm");
    private static final Set<String> QUIZ_TYPES = Set.of("single", "multi", "judge");

    private final AcademySimulateVideoRepository videos;
    private final AcademyQuizRepository quizzes;
    private final AcademyLessonRepository lessons;
    private final Path uploadRoot;

    public AcademyAdminService(AcademySimulateVideoRepository videos,
                               AcademyQuizRepository quizzes,
                               AcademyLessonRepository lessons,
                               @Value("${kuaima.upload.dir:./uploads/}") String uploadDir) {
        this.videos = videos;
        this.quizzes = quizzes;
        this.lessons = lessons;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public List<Map<String, Object>> simulateVideos() {
        return videos.findAllByOrderBySortAscIdAsc().stream().map(this::simulateView).toList();
    }

    @Transactional
    public Map<String, Object> createSimulateVideo(SimulateVideoRequest request) {
        AcademySimulateVideo entity = new AcademySimulateVideo();
        applySimulateVideo(entity, request, true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(entity.getCreatedAt());
        return simulateView(videos.save(entity));
    }

    @Transactional
    public Map<String, Object> updateSimulateVideo(Long id, SimulateVideoRequest request) {
        AcademySimulateVideo entity = requireVideo(id);
        applySimulateVideo(entity, request, false);
        entity.setUpdatedAt(LocalDateTime.now());
        return simulateView(videos.save(entity));
    }

    @Transactional
    public void deleteSimulateVideo(Long id) {
        videos.findById(id).ifPresent(videos::delete);
    }

    @Transactional
    public Map<String, Object> toggleSimulateVideo(Long id, Boolean enabled) {
        AcademySimulateVideo entity = requireVideo(id);
        entity.setEnabled(enabled == null ? !Boolean.TRUE.equals(entity.getEnabled()) : enabled);
        entity.setUpdatedAt(LocalDateTime.now());
        return simulateView(videos.save(entity));
    }

    @Transactional
    public List<Map<String, Object>> quizzes(String type) {
        List<AcademyQuiz> rows = StringUtils.hasText(type)
                ? quizzes.findByTypeOrderBySortAscIdAsc(normalizeQuizType(type))
                : quizzes.findAllByOrderBySortAscIdAsc();
        return rows.stream().map(this::quizView).toList();
    }

    @Transactional
    public Map<String, Object> createQuiz(QuizRequest request) {
        AcademyQuiz entity = new AcademyQuiz();
        applyQuiz(entity, request, true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(entity.getCreatedAt());
        return quizView(quizzes.save(entity));
    }

    @Transactional
    public Map<String, Object> updateQuiz(Long id, QuizRequest request) {
        AcademyQuiz entity = requireQuiz(id);
        applyQuiz(entity, request, false);
        entity.setUpdatedAt(LocalDateTime.now());
        return quizView(quizzes.save(entity));
    }

    @Transactional
    public void deleteQuiz(Long id) {
        quizzes.findById(id).ifPresent(quizzes::delete);
    }

    @Transactional
    public List<Map<String, Object>> lessons() {
        return lessons.findAllByOrderByIdAsc().stream().map(this::lessonView).toList();
    }

    @Transactional
    public Map<String, Object> uploadLessonVideo(String key, LessonVideoRequest request) {
        AcademyLesson entity = requireLesson(key);
        if (!StringUtils.hasText(request.url())) throw new IllegalArgumentException("视频URL不能为空");
        if (StringUtils.hasText(request.title())) entity.setTitle(request.title().trim());
        entity.setVideo(request.url().trim());
        entity.setDuration(nonNegative(request.duration(), 0));
        entity.setSize(nonNegative(request.size(), 0L));
        String ext = normalizeExt(request.ext());
        if (!VIDEO_EXTS.contains(ext)) throw new IllegalArgumentException("仅支持 MP4、MOV、WebM 视频格式");
        entity.setExt(ext);
        entity.setUploadedAt(LocalDateTime.now());
        return lessonView(lessons.save(entity));
    }

    @Transactional
    public void deleteLessonVideo(String key) {
        AcademyLesson entity = requireLesson(key);
        entity.setVideo(null);
        entity.setDuration(null);
        entity.setSize(null);
        entity.setExt(null);
        entity.setUploadedAt(null);
        lessons.save(entity);
    }

    @Transactional
    public Map<String, Object> toggleLesson(String key, Boolean enabled) {
        AcademyLesson entity = requireLesson(key);
        entity.setEnabled(enabled == null ? !Boolean.TRUE.equals(entity.getEnabled()) : enabled);
        return lessonView(lessons.save(entity));
    }

    @Transactional
    public UploadResponse upload(MultipartFile file, String title, String type) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("上传文件不能为空");
        if (!StringUtils.hasText(title)) throw new IllegalArgumentException("视频标题不能为空");
        if (!Set.of("simulate", "lesson").contains(type)) throw new IllegalArgumentException("type 只能是 simulate 或 lesson");
        if (file.getSize() > MAX_VIDEO_SIZE) throw new IllegalArgumentException("视频大小不能超过 200MB");
        String original = Paths.get(file.getOriginalFilename() == null ? "" : file.getOriginalFilename()).getFileName().toString();
        String ext = extension(original);
        if (!VIDEO_EXTS.contains(ext)) throw new IllegalArgumentException("仅支持 MP4、MOV、WebM 视频格式");

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path directory = uploadRoot.resolve("academy").resolve(datePath).normalize();
        if (!directory.startsWith(uploadRoot)) throw new IllegalArgumentException("上传路径无效");
        try {
            Files.createDirectories(directory);
            Path destination = directory.resolve(UUID.randomUUID().toString().replace("-", "") + "." + ext);
            file.transferTo(destination);
            int duration = videoDuration(destination.toFile(), ext);
            return new UploadResponse("/uploads/academy/" + datePath + "/" + destination.getFileName(),
                    original, file.getSize(), ext, duration);
        } catch (IOException e) {
            throw new IllegalStateException("视频上传失败: " + e.getMessage(), e);
        }
    }

    private void applySimulateVideo(AcademySimulateVideo entity, SimulateVideoRequest request, boolean creating) {
        if (request == null) throw new IllegalArgumentException("请求体不能为空");
        if (creating || request.title() != null) entity.setTitle(requiredText(request.title(), "视频标题"));
        if (creating || request.url() != null) entity.setUrl(requiredText(request.url(), "视频URL"));
        if (creating || request.duration() != null) entity.setDuration(nonNegative(request.duration(), 0));
        if (creating || request.size() != null) entity.setSize(nonNegative(request.size(), 0L));
        if (creating || request.ext() != null) {
            String ext = normalizeExt(request.ext());
            if (!VIDEO_EXTS.contains(ext)) throw new IllegalArgumentException("仅支持 MP4、MOV、WebM 视频格式");
            entity.setExt(ext);
        }
        if (creating || request.sort() != null) entity.setSort(nonNegative(request.sort(), 0));
        if (creating || request.enabled() != null) entity.setEnabled(Boolean.TRUE.equals(request.enabled()));
        if (creating || request.learners() != null) entity.setLearners(nonNegative(request.learners(), 0L));
    }

    private void applyQuiz(AcademyQuiz entity, QuizRequest request, boolean creating) {
        if (request == null) throw new IllegalArgumentException("请求体不能为空");
        if (creating || request.type() != null) entity.setType(normalizeQuizType(request.type()));
        if (creating || request.score() != null) entity.setScore(nonNegative(request.score(), 1));
        if (creating || request.stem() != null) entity.setStem(requiredText(request.stem(), "题干"));
        if (creating || request.options() != null) {
            List<String> options = request.options();
            if ("judge".equals(entity.getType()) && options != null && options.size() != 2) {
                throw new IllegalArgumentException("判断题只能有正确、错误两个选项");
            }
            if (options == null || options.size() < 2 || options.stream().anyMatch(option -> !StringUtils.hasText(option))) {
                throw new IllegalArgumentException("选项至少 2 项且不能为空");
            }
            entity.setOptions(JSONArray.toJSONString(options.stream().map(String::trim).toList()));
        }
        if (creating || request.answer() != null) {
            List<Integer> answer = request.answer();
            String type = entity.getType();
            int expectedSize = "multi".equals(type) ? 2 : 1;
            if (answer == null || answer.size() != expectedSize) {
                throw new IllegalArgumentException("正确答案数量无效");
            }
            int optionSize = JSONArray.parseArray(entity.getOptions()).size();
            if (answer.stream().anyMatch(index -> index == null || index < 0 || index >= optionSize)) {
                throw new IllegalArgumentException("正确答案超出选项范围");
            }
            if (answer.stream().distinct().count() != answer.size()) throw new IllegalArgumentException("正确答案不能重复");
            entity.setAnswer(JSONArray.toJSONString(answer.stream().distinct().sorted().toList()));
        }
        if (creating || request.sort() != null) entity.setSort(nonNegative(request.sort(), 0));
    }

    private Map<String, Object> simulateView(AcademySimulateVideo entity) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", entity.getId());
        view.put("title", entity.getTitle());
        view.put("url", entity.getUrl());
        view.put("duration", entity.getDuration());
        view.put("size", entity.getSize());
        view.put("ext", entity.getExt());
        view.put("sort", entity.getSort());
        view.put("enabled", Boolean.TRUE.equals(entity.getEnabled()));
        view.put("learners", entity.getLearners());
        view.put("createdAt", entity.getCreatedAt());
        view.put("updatedAt", entity.getUpdatedAt());
        return view;
    }

    private Map<String, Object> quizView(AcademyQuiz entity) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", entity.getId());
        view.put("type", entity.getType());
        view.put("score", entity.getScore());
        view.put("stem", entity.getStem());
        view.put("options", JSONArray.parseArray(entity.getOptions()));
        view.put("answer", JSONArray.parseArray(entity.getAnswer()));
        view.put("sort", entity.getSort());
        view.put("createdAt", entity.getCreatedAt());
        view.put("updatedAt", entity.getUpdatedAt());
        return view;
    }

    private Map<String, Object> lessonView(AcademyLesson entity) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("key", entity.getLessonKey());
        view.put("title", entity.getTitle());
        view.put("desc", entity.getDescription());
        view.put("video", entity.getVideo());
        view.put("duration", entity.getDuration());
        view.put("size", entity.getSize());
        view.put("ext", entity.getExt());
        view.put("learners", entity.getLearners());
        view.put("enabled", Boolean.TRUE.equals(entity.getEnabled()));
        view.put("uploadedAt", entity.getUploadedAt());
        return view;
    }

    private AcademySimulateVideo requireVideo(Long id) {
        return videos.findById(id).orElseThrow(() -> new EntityNotFoundException("模拟接单视频不存在: " + id));
    }

    private AcademyQuiz requireQuiz(Long id) {
        return quizzes.findById(id).orElseThrow(() -> new EntityNotFoundException("题目不存在: " + id));
    }

    private AcademyLesson requireLesson(String key) {
        return lessons.findByLessonKey(key)
                .orElseThrow(() -> new EntityNotFoundException("新手课程不存在: " + key));
    }

    private String normalizeQuizType(String value) {
        String type = requiredText(value, "题目类型").toLowerCase();
        if (!QUIZ_TYPES.contains(type)) throw new IllegalArgumentException("题型必须是 single、multi 或 judge");
        return type;
    }

    private String requiredText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) throw new IllegalArgumentException(fieldName + "不能为空");
        return value.trim();
    }

    private int nonNegative(Integer value, int fallback) {
        int number = value == null ? fallback : value;
        if (number < 0) throw new IllegalArgumentException("数值不能小于 0");
        return number;
    }

    private long nonNegative(Long value, long fallback) {
        long number = value == null ? fallback : value;
        if (number < 0) throw new IllegalArgumentException("数值不能小于 0");
        return number;
    }

    private String normalizeExt(String value) {
        return requiredText(value, "视频格式").toLowerCase();
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }

    /** 解析 MP4/MOV 的 mvhd 时长；WebM 解析 Segment Info 的 Duration 和 TimestampScale。 */
    private int videoDuration(java.io.File file, String ext) {
        try (RandomAccessFile input = new RandomAccessFile(file, "r")) {
            return switch (ext) {
                case "mp4", "mov" -> mp4Duration(input);
                case "webm" -> webmDuration(input);
                default -> 0;
            };
        } catch (Exception ignored) {
            return 0;
        }
    }

    private int mp4Duration(RandomAccessFile input) throws IOException {
        return (int) Math.ceil(findMp4Duration(input, 0, input.length()));
    }

    private double findMp4Duration(RandomAccessFile input, long start, long end) throws IOException {
        long offset = start;
        while (offset + 8 <= end) {
            input.seek(offset);
            int headerSize = 8;
            long size = readUnsignedInt(input);
            byte[] type = new byte[4];
            input.readFully(type);
            if (size == 1) {
                size = readUnsignedLong(input);
                headerSize = 16;
            } else if (size == 0) {
                size = end - offset;
            }
            if (size < headerSize) break;
            String box = new String(type);
            if ("moov".equals(box) || "trak".equals(box) || "mdia".equals(box)) {
                double duration = findMp4Duration(input, offset + headerSize, offset + size);
                if (duration > 0) return duration;
            } else if ("mvhd".equals(box)) {
                input.seek(offset + headerSize);
                int version = input.read();
                input.skipBytes(3);
                if (version == 1) {
                    input.skipBytes(16);
                    int timescale = (int) readUnsignedInt(input);
                    double duration = readUnsignedLong(input);
                    return timescale == 0 ? 0 : duration / timescale;
                }
                input.skipBytes(8);
                int timescale = (int) readUnsignedInt(input);
                double duration = readUnsignedInt(input);
                return timescale == 0 ? 0 : duration / timescale;
            }
            offset += size;
        }
        return 0;
    }

    private int webmDuration(RandomAccessFile input) throws IOException {
        long scale = 1_000_000L;
        double duration = 0;
        long length = Math.min(input.length(), 2L * 1024 * 1024);
        byte[] buffer = new byte[(int) length];
        input.seek(0);
        input.readFully(buffer);
        for (int i = 0; i < buffer.length - 2; i++) {
            if ((buffer[i] & 0xff) == 0x2a && (buffer[i + 1] & 0xff) == 0xd7 && (buffer[i + 2] & 0xff) == 0xb1) {
                int sizeFieldSize = ebmlVintSize(buffer[i + 3] & 0xff);
                int dataLength = (int) readEbmlUint(buffer, i + 3);
                int dataStart = i + 3 + sizeFieldSize;
                if (dataStart + dataLength <= buffer.length) {
                    scale = readUnsignedBytes(buffer, dataStart, dataLength);
                }
            }
            if ((buffer[i] & 0xff) == 0x44 && (buffer[i + 1] & 0xff) == 0x89) {
                int sizeFieldSize = ebmlVintSize(buffer[i + 2] & 0xff);
                int dataLength = (int) readEbmlUint(buffer, i + 2);
                int dataStart = i + 2 + sizeFieldSize;
                if (dataStart + dataLength <= buffer.length) {
                    duration = dataLength == 4
                            ? Float.intBitsToFloat((int) readUnsignedBytes(buffer, dataStart, 4))
                            : Double.longBitsToDouble(readUnsignedBytes(buffer, dataStart, 8));
                }
            }
        }
        return scale <= 0 ? 0 : (int) Math.ceil(duration * scale / 1_000_000_000d);
    }

    private int ebmlVintSize(int first) {
        int mask = 0x80;
        for (int size = 1; size <= 8; size++, mask >>= 1) {
            if ((first & mask) != 0) return size;
        }
        return 1;
    }

    private long readEbmlUint(byte[] buffer, int offset) {
        int size = ebmlVintSize(buffer[offset] & 0xff);
        long value = buffer[offset] & (0xff >>> size);
        for (int i = 1; i < size; i++) value = (value << 8) | (buffer[offset + i] & 0xff);
        return value;
    }

    private long readUnsignedBytes(byte[] buffer, int offset, int size) {
        long value = 0;
        for (int i = 0; i < size; i++) value = (value << 8) | (buffer[offset + i] & 0xff);
        return value;
    }

    private long readUnsignedInt(RandomAccessFile input) throws IOException {
        byte[] bytes = new byte[4];
        input.readFully(bytes);
        return readUnsignedBytes(bytes, 0, 4);
    }

    private long readUnsignedLong(RandomAccessFile input) throws IOException {
        byte[] bytes = new byte[8];
        input.readFully(bytes);
        return readUnsignedBytes(bytes, 0, 8);
    }
}
