package com.kuaima.app.domain.resume.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kuaima.app.domain.resume.entity.ResumeExperience;

import lombok.Getter;
import lombok.Setter;

/**
 * 简历纯文本字段解析器。
 *
 * <p>核心原则：<b>宁可留空，不写错</b>。拿不准的字段一律不写，绝不用「第一行当姓名」之类的猜测。
 */
@Service
public class ResumeFieldParser {

    /** 手机号。 */
    private static final Pattern PHONE = Pattern.compile("((?<!\\d)1[3-9]\\d{9}(?!\\d))");
    /** 邮箱。 */
    private static final Pattern EMAIL = Pattern.compile("([A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,})");
    /** 身份证号。 */
    private static final Pattern ID_CARD = Pattern.compile("((?<!\\d)\\d{17}[\\dXx](?!\\d))");
    /** 性别标签（全角冒号已在预处理里转半角，这里仍兼容两者）。 */
    private static final Pattern GENDER = Pattern.compile("性别\\s*[:：]?\\s*(男|女)");
    /** 年龄标签。 */
    private static final Pattern AGE = Pattern.compile("年龄\\s*[:：]?\\s*(\\d{1,3})");
    /** 姓名标签：OCR 可能在「姓」「名」之间插空格，需容忍。 */
    private static final Pattern NAME = Pattern.compile("姓\\s*名\\s*[:：]?\\s*([\\u4e00-\\u9fa5]{2,4})");
    /** 现居住地。 */
    private static final Pattern CITY = Pattern.compile("(?:现居住地|现居|居住地|所在地)\\s*[:：]?\\s*([\\u4e00-\\u9fa5]{2,15})");
    /** 期望工作地。 */
    private static final Pattern WORK_LOCATION = Pattern.compile("(?:期望工作地|工作地点|意向城市)\\s*[:：]?\\s*([\\u4e00-\\u9fa5]{2,15})");
    /** 意向岗位。 */
    private static final Pattern POSITION = Pattern.compile("(?:意向岗位|求职意向|期望职位|期望岗位|应聘岗位|求职岗位)\\s*[:：]?\\s*([^\\s，,。；;:：]{2,20})");
    /** 期望薪资。 */
    private static final Pattern SALARY = Pattern.compile("(?:期望薪资|期望工资|期望月薪|薪资要求)\\s*[:：]?\\s*([^\\s，,。；;:：]{1,20})");
    /** 工作经验标签值。 */
    private static final Pattern EXPERIENCE_LABEL = Pattern.compile("(?:工作经验|工作年限)\\s*[:：]?\\s*([^\\s，,。；;:：]{1,20})");
    /** 经验年限兜底：「N年…经验/工作」。 */
    private static final Pattern EXPERIENCE_YEARS = Pattern.compile("(\\d{1,2})\\s*年.{0,4}(?:经验|工作)");
    /** 日期区间：如 2019.03-2021.06 / 2019年3月-2021年6月 / 2021.07-至今。 */
    private static final Pattern DATE_RANGE = Pattern.compile(
            "(\\d{4})\\s*(?:[./年\\-]\\s*(\\d{1,2})\\s*月?)?\\s*[-~—－–至到]+\\s*"
                    + "(?:(\\d{4})\\s*(?:[./年\\-]\\s*(\\d{1,2})\\s*月?)?|(至今|现在|present|Present|PRESENT))");

    /** 小标题（必须独占一行，避免把「工作经验: 5年经验」这种字段误判成段落标题）。 */
    private static final Pattern EDU_HEAD = Pattern.compile("(教育经历|教育背景|学历经历|学习经历)\\s*:?");
    private static final Pattern WORK_HEAD = Pattern.compile("(工作经历|工作经验|工作履历|职业经历)\\s*:?");
    private static final Pattern PROJECT_HEAD = Pattern.compile("(项目经历|项目经验|项目介绍)\\s*:?");

    /** 每段经历最多产出条数。 */
    private static final int MAX_ENTRIES_PER_SECTION = 10;

    /** 解析结果：简历字段 + 经历草稿列表。 */
    @Getter
    @Setter
    public static class ParsedResume {
        private String name;
        private String gender;
        private Integer age;
        private String phone;
        private String email;
        private String city;
        private String idCard;
        private String position;
        private String jobCategory;
        private String education;
        private String experience;
        private String expectedSalary;
        private String workLocation;
        private List<ResumeExperience> experiences = new ArrayList<>();

        /** 是否解析出了任何有效内容（用于判断导入记录该记成功还是「解析失败」）。 */
        public boolean hasAnyContent() {
            return StringUtils.hasText(name) || StringUtils.hasText(gender) || age != null
                    || StringUtils.hasText(phone) || StringUtils.hasText(email)
                    || StringUtils.hasText(city) || StringUtils.hasText(idCard)
                    || StringUtils.hasText(position) || StringUtils.hasText(jobCategory)
                    || StringUtils.hasText(education) || StringUtils.hasText(experience)
                    || StringUtils.hasText(expectedSalary) || StringUtils.hasText(workLocation)
                    || (experiences != null && !experiences.isEmpty());
        }
    }

    /**
     * 解析简历纯文本。
     *
     * @return 解析结果；文本为空时返回 null（调用方按「不解析」处理）
     */
    public ParsedResume parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String t = normalize(text);
        ParsedResume result = new ParsedResume();

        result.setPhone(firstGroup(PHONE, t));
        result.setEmail(firstGroup(EMAIL, t));
        result.setIdCard(firstGroup(ID_CARD, t));

        // 性别：优先标签，其次用身份证第 17 位推算（奇男偶女）
        String gender = firstGroup(GENDER, t);
        if (!StringUtils.hasText(gender) && StringUtils.hasText(result.getIdCard())) {
            gender = genderFromIdCard(result.getIdCard());
        }
        result.setGender(gender);

        // 年龄：优先标签（16~70 之外丢弃），其次用身份证出生日期算周岁
        Integer age = null;
        Matcher ageMatcher = AGE.matcher(t);
        if (ageMatcher.find()) {
            try {
                int value = Integer.parseInt(ageMatcher.group(1));
                if (value >= 16 && value <= 70) {
                    age = value;
                }
            } catch (NumberFormatException ignore) {
                // 忽略，视为未识别
            }
        }
        if (age == null && StringUtils.hasText(result.getIdCard())) {
            age = ageFromIdCard(result.getIdCard());
        }
        result.setAge(age);

        result.setName(matchName(t));
        result.setCity(firstGroup(CITY, t));
        result.setWorkLocation(firstGroup(WORK_LOCATION, t));
        String position = firstGroup(POSITION, t);
        result.setPosition(position);
        result.setExpectedSalary(firstGroup(SALARY, t));
        result.setExperience(parseExperience(t));
        result.setEducation(parseEducation(t));
        result.setJobCategory(parseJobCategory(position));
        result.setExperiences(parseExperiences(t));
        return result;
    }

    // ---------------- 文本预处理 ----------------

    /** 归一化换行；全角关键符号转半角；压缩连续空白（OCR 空格可能不规则）。 */
    private String normalize(String text) {
        String t = text.replace("\r\n", "\n").replace('\r', '\n');
        t = t.replace('：', ':').replace('（', '(').replace('）', ')').replace('／', '/');
        return t.replaceAll("[ \t\u00A0\u3000]+", " ");
    }

    /** 取第一个匹配并返回第 1 组（去空白）。 */
    private String firstGroup(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1);
            return StringUtils.hasText(value) ? value.trim() : null;
        }
        return null;
    }

    // ---------------- 单字段 ----------------

    /** 姓名：只认显式标签，值须为 2~4 个汉字。 */
    private String matchName(String text) {
        Matcher matcher = NAME.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String name = trimTrailingLabel(matcher.group(1));
        if (name.length() < 2 || name.length() > 4) {
            return null;
        }
        return name;
    }

    /** 去掉紧跟姓名后粘连的其它标签词（如 OCR 无空格时的「张伟性别」）。 */
    private String trimTrailingLabel(String name) {
        String[] labels = {"性别", "年龄", "电话", "手机", "邮箱", "学历", "经验", "籍贯"};
        for (String label : labels) {
            if (name.length() > 2 && name.endsWith(label)) {
                return name.substring(0, name.length() - label.length());
            }
        }
        return name;
    }

    private String genderFromIdCard(String idCard) {
        if (idCard == null || idCard.length() < 17) {
            return null;
        }
        char c = idCard.charAt(16);
        if (!Character.isDigit(c)) {
            return null;
        }
        return ((c - '0') % 2 == 1) ? "男" : "女";
    }

    private Integer ageFromIdCard(String idCard) {
        if (idCard == null || idCard.length() < 14) {
            return null;
        }
        try {
            int year = Integer.parseInt(idCard.substring(6, 10));
            int month = Integer.parseInt(idCard.substring(10, 12));
            int day = Integer.parseInt(idCard.substring(12, 14));
            if (year < 1900 || month < 1 || month > 12 || day < 1 || day > 31) {
                return null;
            }
            int age = Period.between(LocalDate.of(year, month, day), LocalDate.now()).getYears();
            return (age >= 16 && age <= 100) ? age : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** 经验：优先标签值，其次「应届」，最后「N年…经验/工作」兜底归一为「N年经验」。 */
    private String parseExperience(String text) {
        Matcher matcher = EXPERIENCE_LABEL.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        if (text.contains("应届")) {
            return "应届";
        }
        Matcher years = EXPERIENCE_YEARS.matcher(text);
        if (years.find()) {
            return years.group(1) + "年经验";
        }
        return null;
    }

    /** 学历：全文找学历关键词，取最高一档。 */
    private String parseEducation(String text) {
        if (containsAny(text, "博士", "硕士", "研究生", "本科", "大专", "专科")) {
            return "大专及以上";
        }
        if (containsAny(text, "中专", "高中", "技校", "职高")) {
            // 注意：取值必须与前端筛选器选项一致（resume-list.vue 的 educationOpts 用的是「高中/中专」）
            return "高中/中专";
        }
        if (text.contains("初中")) {
            return "初中及以上";
        }
        return null;
    }

    /** 职位类型：仅当意向岗位里明确含关键词时才归类。 */
    private String parseJobCategory(String position) {
        if (!StringUtils.hasText(position)) {
            return null;
        }
        if (position.contains("分拣")) {
            return "分拣打包";
        }
        if (position.contains("装卸") || position.contains("搬运")) {
            return "搬运装卸";
        }
        if (position.contains("餐饮") || position.contains("服务")) {
            return "餐饮服务";
        }
        return null;
    }

    // ---------------- 经历解析 ----------------

    /** 按小标题切段，段内按日期区间切条。 */
    private List<ResumeExperience> parseExperiences(String text) {
        List<ResumeExperience> result = new ArrayList<>();
        String currentType = null;
        List<String> buffer = new ArrayList<>();
        for (String line : text.split("\n", -1)) {
            String type = sectionHeaderType(line);
            if (type != null) {
                addSection(result, currentType, buffer);
                buffer = new ArrayList<>();
                currentType = type;
                continue;
            }
            if (currentType != null) {
                buffer.add(line);
            }
        }
        addSection(result, currentType, buffer);
        return result;
    }

    /** 判断是否为段落小标题（须独占一行）。 */
    private String sectionHeaderType(String line) {
        String s = line.trim();
        if (s.isEmpty()) {
            return null;
        }
        if (EDU_HEAD.matcher(s).matches()) {
            return "EDU";
        }
        if (WORK_HEAD.matcher(s).matches()) {
            return "WORK";
        }
        if (PROJECT_HEAD.matcher(s).matches()) {
            return "PROJECT";
        }
        return null;
    }

    /** 把一段文本按日期区间切成若干条经历，最多 10 条；识别不出日期区间则不产出。 */
    private void addSection(List<ResumeExperience> out, String type, List<String> lines) {
        if (type == null || lines.isEmpty()) {
            return;
        }
        String section = String.join("\n", lines);
        Matcher matcher = DATE_RANGE.matcher(section);
        List<MatchSpan> spans = new ArrayList<>();
        while (matcher.find()) {
            spans.add(new MatchSpan(matcher.start(), matcher.end() - matcher.start(),
                    matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5)));
        }
        int limit = Math.min(spans.size(), MAX_ENTRIES_PER_SECTION);
        for (int i = 0; i < limit; i++) {
            MatchSpan current = spans.get(i);
            int chunkEnd = (i + 1 < spans.size()) ? spans.get(i + 1).start : section.length();
            String chunk = section.substring(current.start, chunkEnd).trim();
            ResumeExperience entry = buildEntry(type, chunk, current);
            if (entry != null) {
                out.add(entry);
            }
        }
    }

    /** 由一条文本构造经历：去掉开头日期区间，第一行作 title，其余作 description。 */
    private ResumeExperience buildEntry(String type, String chunk, MatchSpan range) {
        String withoutDate = (chunk.length() >= range.length ? chunk.substring(range.length) : "").trim();
        if (withoutDate.isEmpty()) {
            return null;
        }
        String[] parts = withoutDate.split("\n", -1);
        String title = parts[0].trim();
        if (title.isEmpty()) {
            return null;
        }
        if (title.length() > 100) {
            title = title.substring(0, 100);
        }

        StringBuilder rest = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            String line = parts[i].trim();
            if (!line.isEmpty()) {
                if (rest.length() > 0) {
                    rest.append('\n');
                }
                rest.append(line);
            }
        }
        String description = rest.length() == 0
                ? null
                : (rest.length() > 2000 ? rest.substring(0, 2000) : rest.toString());

        ResumeExperience entry = new ResumeExperience();
        entry.setType(type);
        entry.setTitle(title);
        entry.setStartDate(formatDate(range.startYear, range.startMonth));
        entry.setEndDate(formatEnd(range));
        entry.setDescription(description);
        return entry;
    }

    /** 年月统一成 yyyy-MM；只有年份则存 yyyy。 */
    private String formatDate(String year, String month) {
        if (!StringUtils.hasText(year)) {
            return null;
        }
        if (!StringUtils.hasText(month)) {
            return year;
        }
        try {
            int m = Integer.parseInt(month.trim());
            if (m < 1 || m > 12) {
                return year;
            }
            return year + "-" + String.format("%02d", m);
        } catch (NumberFormatException e) {
            return year;
        }
    }

    private String formatEnd(MatchSpan range) {
        if (StringUtils.hasText(range.endKeyword)) {
            return "至今";
        }
        return formatDate(range.endYear, range.endMonth);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /** 一个日期区间匹配片段。 */
    private static final class MatchSpan {
        private final int start;
        private final int length;
        private final String startYear;
        private final String startMonth;
        private final String endYear;
        private final String endMonth;
        private final String endKeyword;

        private MatchSpan(int start, int length, String startYear, String startMonth,
                          String endYear, String endMonth, String endKeyword) {
            this.start = start;
            this.length = length;
            this.startYear = startYear;
            this.startMonth = startMonth;
            this.endYear = endYear;
            this.endMonth = endMonth;
            this.endKeyword = endKeyword;
        }
    }
}