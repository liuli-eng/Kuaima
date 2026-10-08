package com.kuaima.app.common.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 简历文件正文提取：PDF/Word 直接抽文本，图片走服务器已安装的 tesseract 命令行 OCR。
 *
 * <p>约定：任何解析异常都只返回 null，绝不向外抛出——解析失败不能影响简历原件入库。
 */
@Component
public class ResumeTextExtractor {

    /** PDF 只解析前若干页，控制耗时（简历正文通常就在前几页）。 */
    private static final int PDF_MAX_PAGES = 5;
    /** OCR 单张图片超时时间（秒），超时强制销毁进程。 */
    private static final long OCR_TIMEOUT_SECONDS = 30;

    private final boolean enabled;
    private final String ocrCommand;
    private final String ocrLanguages;
    private final String ocrDatapath;

    public ResumeTextExtractor(
            @Value("${kuaima.resume.parse.enabled:true}") boolean enabled,
            @Value("${kuaima.resume.parse.ocr-command:tesseract}") String ocrCommand,
            @Value("${kuaima.resume.parse.ocr-languages:chi_sim+eng}") String ocrLanguages,
            @Value("${kuaima.resume.parse.ocr-datapath:}") String ocrDatapath) {
        this.enabled = enabled;
        this.ocrCommand = StringUtils.hasText(ocrCommand) ? ocrCommand.trim() : "tesseract";
        this.ocrLanguages = StringUtils.hasText(ocrLanguages) ? ocrLanguages.trim() : "chi_sim+eng";
        this.ocrDatapath = ocrDatapath;
    }

    /** 解析开关：关闭时 extract 一律返回 null，调用方据此跳过整条解析链路。 */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 把简历文件内容转成纯文本。
     *
     * @param content   文件字节
     * @param extension 扩展名（如 .pdf/.docx/.jpg）
     * @return 纯文本；开关关闭、类型不支持或解析失败时返回 null
     */
    public String extract(byte[] content, String extension) {
        if (!enabled || content == null || content.length == 0) {
            return null;
        }
        String ext = extension == null ? "" : extension.trim().toLowerCase();
        try {
            return switch (ext) {
                case ".pdf" -> extractPdf(content);
                case ".docx" -> extractDocx(content);
                case ".doc" -> extractDoc(content);
                case ".jpg", ".jpeg", ".png", ".bmp", ".webp", ".gif" -> extractImageOcr(content, ext);
                default -> null;
            };
        } catch (Exception e) {
            // 统一兜底：任何异常都不向外抛，降级为「不解析」
            return null;
        }
    }

    /** PDF：PDFBox 抽取文本，只取前 5 页。 */
    private String extractPdf(byte[] content) throws IOException {
        try (PDDocument document = Loader.loadPDF(content)) {
            int pages = document.getNumberOfPages();
            if (pages <= 0) {
                return null;
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(Math.min(PDF_MAX_PAGES, pages));
            return stripper.getText(document);
        }
    }

    /** DOCX：拼接所有段落 + 所有表格单元格（表格里常有「姓名|手机号」这类关键信息）。 */
    private String extractDocx(byte[] content) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (StringUtils.hasText(text)) {
                    sb.append(text.trim()).append('\n');
                }
            }
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    StringBuilder line = new StringBuilder();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String cellText = cell.getText();
                        if (StringUtils.hasText(cellText)) {
                            if (line.length() > 0) {
                                line.append(' ');
                            }
                            line.append(cellText.trim());
                        }
                    }
                    if (line.length() > 0) {
                        sb.append(line).append('\n');
                    }
                }
            }
            return sb.toString();
        }
    }

    /** 老版 .doc：HWPF + WordExtractor。 */
    private String extractDoc(byte[] content) throws IOException {
        try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(content))) {
            return new WordExtractor(document).getText();
        }
    }

    /**
     * 图片：写临时文件后调用 tesseract 命令行 OCR。
     *
     * <p>命令形如 {@code tesseract <tmp> stdout -l chi_sim+eng [--tessdata-dir <dir>]}；
     * 设 30 秒超时，超时销毁进程；临时文件在 finally 中删除。
     */
    private String extractImageOcr(byte[] content, String ext) throws IOException {
        Path image = Files.createTempFile("kuaima-resume-ocr-", ext);
        Path output = Files.createTempFile("kuaima-resume-ocr-", ".txt");
        try {
            Files.write(image, content);

            List<String> command = new ArrayList<>();
            command.add(ocrCommand);
            command.add(image.toString());
            command.add("stdout");
            command.add("-l");
            command.add(ocrLanguages);
            if (StringUtils.hasText(ocrDatapath)) {
                command.add("--tessdata-dir");
                command.add(ocrDatapath.trim());
            }

            ProcessBuilder builder = new ProcessBuilder(command);
            // stderr（tesseract 的进度信息）丢弃；结果写入临时文件，避免管道堆积死锁
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            builder.redirectOutput(output.toFile());
            Process process = builder.start();

            boolean finished;
            try {
                finished = process.waitFor(OCR_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
                return null;
            }
            if (!finished) {
                process.destroyForcibly();
                return null;
            }
            return Files.readString(output, StandardCharsets.UTF_8).trim();
        } finally {
            Files.deleteIfExists(image);
            Files.deleteIfExists(output);
        }
    }
}