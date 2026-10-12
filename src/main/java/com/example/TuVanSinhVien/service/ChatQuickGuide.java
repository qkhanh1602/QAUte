package com.example.TuVanSinhVien.service;

import java.util.List;
import java.util.Locale;

/** Shared source for the suggestions shown in chat and their automatic guidance. */
public final class ChatQuickGuide {
    private ChatQuickGuide() {}
    public record Suggestion(String label, String question, String answer) {
        public String getLabel() { return label; }
        public String getQuestion() { return question; }
    }
    private static Suggestion topic(String label, String details) {
        return new Suggestion(label, "Em muốn hỏi về " + label.toLowerCase(Locale.ROOT) + ".",
                details + " Em có thể gửi thêm thông tin để cố vấn hướng dẫn cụ thể.");
    }
    public static List<Suggestion> forUnit(String code) {
        if (code == null) code = "";
        return switch (code.toUpperCase(Locale.ROOT)) {
            case "DAOTAO", "CTSV", "HOTRO" -> List.of(
                new Suggestion("Vượt tín chỉ", "Làm thế nào để đăng ký học phần vượt quá số tín chỉ quy định?", "Em gửi học kỳ, số tín chỉ dự kiến và tình trạng học tập để được kiểm tra điều kiện đăng ký vượt tín chỉ."),
                new Suggestion("Giấy xác nhận NVQS", "Quy trình xin cấp giấy xác nhận sinh viên để hoãn nghĩa vụ quân sự?", "Em gửi nhu cầu cấp giấy xác nhận và thời hạn cần nhận. Cố vấn sẽ hướng dẫn thủ tục theo quy định hiện hành."),
                new Suggestion("Học bổng", "Hướng dẫn điều kiện xét học bổng khuyến khích học tập?", "Em cho biết học kỳ và loại học bổng quan tâm để được hướng dẫn điều kiện, hồ sơ và thời hạn xét."),
                new Suggestion("Rút học phần", "Em muốn hỏi về điều kiện rút học phần trong học kỳ?", "Em gửi mã học phần, học kỳ và lý do muốn rút. Cố vấn sẽ kiểm tra thời hạn và hướng dẫn thủ tục."));
            case "CNTT" -> List.of(topic("Định hướng chuyên ngành", "Em cho biết hướng quan tâm như phần mềm, dữ liệu, AI hoặc mạng máy tính."), topic("Thực tập CNTT", "Em gửi vị trí thực tập mong muốn, kỹ năng hiện có và thời gian dự kiến."), topic("Đồ án lập trình", "Em mô tả đề tài, công nghệ sử dụng và phần đang gặp khó khăn."), topic("Chứng chỉ CNTT", "Em cho biết mục tiêu nghề nghiệp và chứng chỉ đang cân nhắc."));
            case "CKCTM" -> List.of(topic("Thiết kế cơ khí", "Em cho biết học phần hoặc bài toán thiết kế cần hỗ trợ."), topic("CAD/CAM/CNC", "Em nêu phần mềm, máy hoặc công đoạn đang cần hướng dẫn."), topic("Thực tập cơ khí", "Em cho biết lĩnh vực sản xuất và vị trí thực tập mong muốn."), topic("Đồ án chế tạo máy", "Em gửi tên đề tài và vấn đề đang gặp khi thực hiện."));
            case "GTNL" -> List.of(topic("Kỹ thuật ô tô", "Em mô tả hệ thống hoặc chuyên đề ô tô quan tâm."), topic("Năng lượng", "Em cho biết hướng nghiên cứu hoặc học phần về năng lượng."), topic("Thực tập kỹ thuật", "Em gửi vị trí và thời gian thực tập dự kiến."), topic("Đồ án chuyên ngành", "Em mô tả đề tài, yêu cầu và phần cần hỗ trợ."));
            case "CNHHTP" -> List.of(topic("Công nghệ thực phẩm", "Em nêu quy trình hoặc chủ đề thực phẩm cần tư vấn."), topic("Kỹ thuật hóa học", "Em cho biết học phần và vấn đề kỹ thuật cần hỗ trợ."), topic("An toàn phòng thí nghiệm", "Em mô tả nhu cầu; cần được người phụ trách hướng dẫn trước khi thao tác."), topic("Thực tập ngành hóa", "Em gửi hướng chuyên môn và thời gian thực tập dự kiến."));
            case "DDT" -> List.of(topic("Thiết kế mạch", "Em gửi loại mạch và yêu cầu của bài toán."), topic("Tự động hóa", "Em cho biết hệ thống điều khiển hoặc thiết bị đang học."), topic("Hệ thống nhúng", "Em nêu vi điều khiển, công cụ và khó khăn đang gặp."), topic("Thực tập điện tử", "Em gửi hướng chuyên môn và vị trí thực tập mong muốn."));
            case "INTT" -> List.of(topic("Công nghệ in", "Em nêu công đoạn hoặc công nghệ in cần tư vấn."), topic("Thiết kế đồ họa", "Em gửi mục tiêu thiết kế và công cụ đang sử dụng."), topic("Truyền thông số", "Em cho biết hướng nội dung hoặc dự án đang quan tâm."), topic("Portfolio ngành in", "Em mô tả vị trí ứng tuyển để được gợi ý chuẩn bị portfolio."));
            case "KHUD" -> List.of(topic("Toán ứng dụng", "Em nêu học phần và nội dung toán cần hỗ trợ."), topic("Vật lý ứng dụng", "Em mô tả chuyên đề hoặc bài thực hành cần tư vấn."), topic("Nghiên cứu khoa học", "Em gửi ý tưởng và hướng nghiên cứu quan tâm."), topic("Định hướng nghề nghiệp", "Em cho biết chuyên ngành và mục tiêu nghề nghiệp."));
            case "KINHTE" -> List.of(topic("Quản trị doanh nghiệp", "Em nêu chuyên đề hoặc hướng quản trị quan tâm."), topic("Kế toán tài chính", "Em cho biết học phần hoặc mục tiêu nghề nghiệp."), topic("Logistics", "Em nêu hướng chuyên môn hoặc vị trí mong muốn."), topic("Thực tập kinh tế", "Em gửi vị trí ứng tuyển và thời gian thực tập dự kiến."));
            case "NGOAINGU" -> List.of(topic("Chuẩn ngoại ngữ", "Em cho biết khóa học và chứng chỉ muốn sử dụng để được đối chiếu quy định."), topic("Biên phiên dịch", "Em nêu ngôn ngữ và lĩnh vực muốn theo đuổi."), topic("Kỹ năng giao tiếp", "Em cho biết kỹ năng cần cải thiện và mục tiêu học tập."), topic("Thực tập ngoại ngữ", "Em gửi vị trí và môi trường thực tập mong muốn."));
            case "TTDL" -> List.of(topic("Thiết kế thời trang", "Em nêu hướng thiết kế và kỹ năng muốn phát triển."), topic("Quản trị du lịch", "Em cho biết chuyên đề hoặc công việc du lịch quan tâm."), topic("Portfolio thời trang", "Em mô tả mục tiêu portfolio và các sản phẩm hiện có."), topic("Thực tập du lịch", "Em gửi vị trí và thời gian thực tập dự kiến."));
            case "XAYDUNG" -> List.of(topic("Thiết kế kết cấu", "Em mô tả loại công trình và bài toán cần tư vấn."), topic("BIM và dự toán", "Em nêu công cụ và nội dung đang cần hỗ trợ."), topic("Thực tập công trường", "Em cho biết vị trí và thời gian thực tập dự kiến."), topic("Đồ án xây dựng", "Em gửi tên đề tài và phần đang vướng mắc."));
            case "CTLUAT" -> List.of(topic("Học phần chính trị", "Em nêu học phần và nội dung muốn trao đổi."), topic("Pháp luật đại cương", "Em mô tả tình huống hoặc chủ đề học tập cần tư vấn."), topic("Phương pháp ôn tập", "Em cho biết môn học và phần cần hệ thống kiến thức."), topic("Nghiên cứu pháp luật", "Em nêu đề tài và phạm vi tài liệu muốn tìm hiểu."));
            case "VSPKT" -> List.of(topic("Nghiệp vụ sư phạm", "Em cho biết kỹ năng giảng dạy hoặc học phần quan tâm."), topic("Thực tập sư phạm", "Em gửi đơn vị và thời gian thực tập dự kiến."), topic("Thiết kế bài giảng", "Em nêu đối tượng học, nội dung và mục tiêu bài giảng."), topic("Nghiên cứu giáo dục", "Em gửi ý tưởng và vấn đề nghiên cứu quan tâm."));
            case "DTTT" -> List.of(topic("Chương trình tiên tiến", "Em cho biết ngành học và nội dung chương trình muốn tìm hiểu."), topic("Học phần tiếng Anh", "Em nêu học phần và khó khăn khi học bằng tiếng Anh."), topic("Trao đổi quốc tế", "Em cho biết chương trình, quốc gia và thời gian quan tâm."), topic("Thực tập quốc tế", "Em gửi hướng chuyên môn và vị trí thực tập mong muốn."));
            default -> List.of(topic("Học tập chuyên ngành", "Em cho biết ngành học và vấn đề cần hỗ trợ."), topic("Thực tập", "Em gửi mục tiêu và thời gian thực tập dự kiến."), topic("Đồ án tốt nghiệp", "Em gửi tên đề tài và khó khăn đang gặp."), topic("Định hướng nghề nghiệp", "Em cho biết kỹ năng hiện có và công việc mong muốn."));
        };
    }
}
