package com.example.TuVanSinhVien.service;

import com.lowagie.text.pdf.BaseFont;

final class PdfFonts {
    private PdfFonts() {}
    static BaseFont vietnamese() throws Exception {
        try (var input=PdfFonts.class.getResourceAsStream("/fonts/NotoSans-Regular.ttf")) {
            if(input==null) throw new IllegalStateException("Thiếu font Unicode đóng gói trong ứng dụng.");
            return BaseFont.createFont("NotoSans-Regular.ttf",BaseFont.IDENTITY_H,BaseFont.EMBEDDED,false,input.readAllBytes(),null);
        }
    }
}
