package com.example.TuVanSinhVien.service;

/** Shared server-side validation for self-service and administrator account creation. */
public final class AccountInput {
    private AccountInput() {}
    public static String required(String value,String label,int max) {
        String clean=value==null?"":value.trim();
        if(clean.isEmpty() || clean.length()>max)
            throw new IllegalArgumentException(label+" không được để trống và tối đa "+max+" ký tự.");
        return clean;
    }
    public static void identity(String username,String name,String email,String phone) {
        String user=required(username,"Tên đăng nhập",50);
        if(!user.matches("[A-Za-z0-9_.-]{3,50}")) throw new IllegalArgumentException("Tên đăng nhập dài 3–50 ký tự, chỉ gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
        if(required(name,"Họ tên",100).length()<2) throw new IllegalArgumentException("Họ tên phải có ít nhất 2 ký tự.");
        email(email);
        if(phone!=null && !phone.isBlank() && !phone.trim().matches("^(?:\\+84|0)[0-9]{9,10}$"))
            throw new IllegalArgumentException("Số điện thoại không đúng định dạng Việt Nam.");
    }
    public static String email(String value) {
        String email=required(value,"Email",254).toLowerCase(java.util.Locale.ROOT);
        if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Email không hợp lệ.");
        return email;
    }
    public static void student(String code,String faculty,String major,String className,String course) {
        if(!required(code,"MSSV",20).matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException("MSSV chỉ được chứa chữ, số, gạch dưới hoặc gạch ngang.");
        required(faculty,"Khoa",100); required(major,"Ngành",100);
        required(className,"Lớp",20); required(course,"Khóa học",20);
    }
    public static void password(String value) {
        if(value==null || !value.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")
                || value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Mật khẩu dài 8–72 ký tự, gồm chữ và số, tối đa 72 byte UTF-8.");
    }
}
