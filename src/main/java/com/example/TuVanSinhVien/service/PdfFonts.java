package com.example.TuVanSinhVien.service;

import com.lowagie.text.pdf.BaseFont;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class PdfFonts {
    private PdfFonts() {}

    static BaseFont vietnamese() throws Exception {
        for (String candidate : List.of(
                "C:/Windows/Fonts/arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf")) {
            if (Files.isRegularFile(Path.of(candidate))) {
                return BaseFont.createFont(candidate, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
        }
        throw new IllegalStateException("Máy chủ chưa có font Unicode Arial hoặc DejaVu Sans để xuất PDF tiếng Việt.");
    }
}
