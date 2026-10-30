package com.example.TuVanSinhVien.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Small OOXML workbook, without loading the XMLBeans schema graph on a web request. */
final class ReportWorkbook {
    private static final String SHEET_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String PACKAGE_REL_NS = "http://schemas.openxmlformats.org/package/2006/relationships";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private ReportWorkbook() {}

    static byte[] create(List<ReportRow> rows, Map<String, Long> counts) {
        if (rows.size() > 1_048_566) throw new IllegalArgumentException("Báo cáo vượt số dòng Excel hỗ trợ. Vui lòng chọn khoảng ngày ngắn hơn.");
        int summaryRow = rows.size() + 5;
        try (var output = new ByteArrayOutputStream(); var zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
            entry(zip, "[Content_Types].xml", """
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                  <Override PartName="/xl/drawings/drawing1.xml" ContentType="application/vnd.openxmlformats-officedocument.drawing+xml"/>
                  <Override PartName="/xl/charts/chart1.xml" ContentType="application/vnd.openxmlformats-officedocument.drawingml.chart+xml"/>
                </Types>""");
            entry(zip, "_rels/.rels", relationships("<Relationship Id=\"rId1\" Type=\""+REL_NS+"/officeDocument\" Target=\"xl/workbook.xml\"/>"));
            entry(zip, "xl/workbook.xml", "<workbook xmlns=\""+SHEET_NS+"\" xmlns:r=\""+REL_NS+"\"><sheets><sheet name=\"BaoCao\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            entry(zip, "xl/_rels/workbook.xml.rels", relationships(
                    "<Relationship Id=\"rId1\" Type=\""+REL_NS+"/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    +"<Relationship Id=\"rId2\" Type=\""+REL_NS+"/styles\" Target=\"styles.xml\"/>"));
            entry(zip, "xl/styles.xml", styles());
            entry(zip, "xl/worksheets/sheet1.xml", sheet(rows, counts, summaryRow));
            entry(zip, "xl/worksheets/_rels/sheet1.xml.rels", relationships("<Relationship Id=\"rId1\" Type=\""+REL_NS+"/drawing\" Target=\"../drawings/drawing1.xml\"/>"));
            entry(zip, "xl/drawings/drawing1.xml", drawing(summaryRow));
            entry(zip, "xl/drawings/_rels/drawing1.xml.rels", relationships("<Relationship Id=\"rId1\" Type=\""+REL_NS+"/chart\" Target=\"../charts/chart1.xml\"/>"));
            entry(zip, "xl/charts/chart1.xml", chart(counts, summaryRow));
            zip.finish();
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể tạo báo cáo Excel.", ex);
        }
    }

    private static String sheet(List<ReportRow> rows, Map<String, Long> counts, int summaryRow) {
        var xml = new StringBuilder("<worksheet xmlns=\""+SHEET_NS+"\" xmlns:r=\""+REL_NS+"\">");
        xml.append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
        xml.append("<cols>");
        int[] widths = {60, 24, 28, 36, 42, 20, 28};
        for (int i=0;i<widths.length;i++) xml.append("<col min=\"").append(i+1).append("\" max=\"").append(i+1).append("\" width=\"").append(widths[i]).append("\" customWidth=\"1\"/>");
        xml.append("</cols><sheetData>");
        textRow(xml, 1, 1, List.of("Tiêu đề câu hỏi", "Ngày tạo", "Sinh viên", "Đơn vị", "Lĩnh vực", "Trạng thái", "Cố vấn"));
        int index=2;
        for (ReportRow row : rows) textRow(xml, index++, 2, java.util.Arrays.asList(row.title(), row.createdAt()==null?"":DATE.format(row.createdAt()),
                row.studentName(), row.unitName(), row.fieldName(), row.status(), row.advisorName()));
        textRow(xml, summaryRow, 1, List.of("Trạng thái", "Số lượng"));
        index=summaryRow+1;
        for (var item:counts.entrySet()) {
            xml.append("<row r=\"").append(index).append("\">");
            textCell(xml, "A"+index, item.getKey(), 0);
            xml.append("<c r=\"B").append(index).append("\"><v>").append(item.getValue()).append("</v></c></row>");
            index++;
        }
        xml.append("</sheetData>");
        if (!rows.isEmpty()) xml.append("<autoFilter ref=\"A1:G").append(rows.size()+1).append("\"/>");
        return xml.append("<drawing r:id=\"rId1\"/></worksheet>").toString();
    }

    private static void textRow(StringBuilder xml, int row, int style, List<String> values) {
        xml.append("<row r=\"").append(row).append("\">");
        for (int i=0;i<values.size();i++) textCell(xml, String.valueOf((char)('A'+i))+row, values.get(i), style);
        xml.append("</row>");
    }

    private static void textCell(StringBuilder xml, String address, String value, int style) {
        // Inline strings keep leading =,+,-,@ as text rather than spreadsheet formulas.
        xml.append("<c r=\"").append(address).append("\" s=\"").append(style)
                .append("\" t=\"inlineStr\"><is><t>").append(escape(value))
                .append("</t></is></c>");
    }

    private static String styles() {
        return """
            <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
              <fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font></fonts>
              <fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FF203C73"/><bgColor indexed="64"/></patternFill></fill></fills>
              <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
              <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
              <cellXfs count="3"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyAlignment="1"><alignment wrapText="1" vertical="top"/></xf><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyAlignment="1"><alignment wrapText="1" vertical="top"/></xf></cellXfs>
              <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
            </styleSheet>""";
    }

    private static String drawing(int summaryRow) {
        return """
            <xdr:wsDr xmlns:xdr="http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
              <xdr:twoCellAnchor><xdr:from><xdr:col>8</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>%d</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:from>
              <xdr:to><xdr:col>16</xdr:col><xdr:colOff>0</xdr:colOff><xdr:row>%d</xdr:row><xdr:rowOff>0</xdr:rowOff></xdr:to>
              <xdr:graphicFrame macro=""><xdr:nvGraphicFramePr><xdr:cNvPr id="2" name="Thống kê trạng thái"/><xdr:cNvGraphicFramePr/></xdr:nvGraphicFramePr><xdr:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/></xdr:xfrm>
              <a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/chart"><c:chart xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" r:id="rId1"/></a:graphicData></a:graphic></xdr:graphicFrame><xdr:clientData/></xdr:twoCellAnchor>
            </xdr:wsDr>""".formatted(summaryRow-1, summaryRow+14);
    }

    private static String chart(Map<String, Long> counts, int summaryRow) {
        var xml=new StringBuilder("<c:chartSpace xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"><c:chart>");
        xml.append("<c:title><c:tx><c:rich><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>Số lượng câu hỏi theo trạng thái</a:t></a:r></a:p></c:rich></c:tx></c:title>");
        xml.append("<c:plotArea><c:layout/><c:barChart><c:barDir val=\"col\"/><c:grouping val=\"clustered\"/><c:ser><c:idx val=\"0\"/><c:order val=\"0\"/><c:tx><c:v>Số lượng</c:v></c:tx>");
        int last=summaryRow+counts.size();
        xml.append("<c:cat><c:strRef><c:f>'BaoCao'!$A$").append(summaryRow+1).append(":$A$").append(last).append("</c:f><c:strCache><c:ptCount val=\"").append(counts.size()).append("\"/>");
        int index=0;
        for (String state:counts.keySet()) xml.append("<c:pt idx=\"").append(index++).append("\"><c:v>").append(escape(state)).append("</c:v></c:pt>");
        xml.append("</c:strCache></c:strRef></c:cat><c:val><c:numRef><c:f>'BaoCao'!$B$").append(summaryRow+1).append(":$B$").append(last).append("</c:f><c:numCache><c:formatCode>0</c:formatCode><c:ptCount val=\"").append(counts.size()).append("\"/>");
        index=0;
        for (Long count:counts.values()) xml.append("<c:pt idx=\"").append(index++).append("\"><c:v>").append(count).append("</c:v></c:pt>");
        return xml.append("</c:numCache></c:numRef></c:val></c:ser><c:axId val=\"1\"/><c:axId val=\"2\"/></c:barChart>")
                .append("<c:catAx><c:axId val=\"1\"/><c:scaling><c:orientation val=\"minMax\"/></c:scaling><c:axPos val=\"b\"/><c:crossAx val=\"2\"/><c:crosses val=\"autoZero\"/><c:auto val=\"1\"/><c:lblAlgn val=\"ctr\"/><c:lblOffset val=\"100\"/></c:catAx>")
                .append("<c:valAx><c:axId val=\"2\"/><c:scaling><c:orientation val=\"minMax\"/></c:scaling><c:axPos val=\"l\"/><c:majorGridlines/><c:numFmt formatCode=\"0\" sourceLinked=\"0\"/><c:crossAx val=\"1\"/><c:crosses val=\"autoZero\"/><c:crossBetween val=\"between\"/></c:valAx></c:plotArea><c:plotVisOnly val=\"1\"/></c:chart></c:chartSpace>").toString();
    }

    private static String relationships(String inner) { return "<Relationships xmlns=\""+PACKAGE_REL_NS+"\">"+inner+"</Relationships>"; }
    private static void entry(ZipOutputStream zip, String path, String xml) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+xml).getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
    private static String escape(String value) {
        if (value==null) return "";
        var clean=new StringBuilder();
        value.codePoints().filter(c -> c==9 || c==10 || c==13 || c>=32 && c<=0xD7FF || c>=0xE000 && c<=0xFFFD || c>=0x10000 && c<=0x10FFFF)
                .limit(32767).forEach(clean::appendCodePoint);
        return clean.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}
