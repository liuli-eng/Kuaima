package com.kuaima.app.common.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * PDF 处理工具：把 PDF 首页渲染成 PNG，供小程序直接当图片预览（避免依赖 downloadFile 域名白名单）。
 */
public final class PdfUtil {

    /** 渲染 DPI：兼顾清晰度与体积，120 左右在手机上已可读。 */
    private static final float RENDER_DPI = 120f;

    private PdfUtil() {
    }

    /**
     * 渲染 PDF 首页为 PNG 字节数组。
     *
     * @return PNG 字节；文件为空、页数为 0 或无法解析时返回 null（调用方降级为不生成预览图）
     */
    public static byte[] renderFirstPageToPng(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            return null;
        }
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.getNumberOfPages() <= 0) {
                return null;
            }
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, RENDER_DPI, ImageType.RGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            // 加密/损坏的 PDF 等一律降级：不生成预览图，不影响原始文件已入库
            return null;
        }
    }

    /** 是否为 PDF（按扩展名判断，忽略大小写）。 */
    public static boolean isPdfExtension(String extension) {
        return ".pdf".equalsIgnoreCase(extension == null ? "" : extension.trim());
    }

    /** 是否为可当图片直接展示的扩展名（忽略大小写）。 */
    public static boolean isImageExtension(String extension) {
        if (extension == null) {
            return false;
        }
        String ext = extension.trim().toLowerCase();
        return ".png".equals(ext) || ".jpg".equals(ext) || ".jpeg".equals(ext)
                || ".gif".equals(ext) || ".webp".equals(ext) || ".bmp".equals(ext);
    }
}