package org.example.be_sp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class Emailgui {
    @Autowired
    private JavaMailSender mailSender;


    public void sendAccountInfo(String toEmail, String tenNhanVien, String matKhau) throws MessagingException {
        String tenTaiKhoan = toEmail.split("@")[0];
        String subject = "Thông tin tài khoản nhân viên mới";

        String body = """
        <div style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f8; padding: 30px;">
            <div style="max-width: 600px; margin: auto; background: white; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden;">
                <div style="background-color: #007BFF; color: white; padding: 16px 24px;">
                    <h2 style="margin: 0;">Thông tin tài khoản nhân viên mới</h2>
                </div>
                <div style="padding: 24px; color: #333;">
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chúng tôi rất vui được thông báo rằng tài khoản nhân viên của bạn đã được tạo thành công với thông tin sau:</p>

                    <div style="background-color: #f1f5ff; padding: 16px; border-radius: 8px; margin: 16px 0;">
                        <p style="margin: 8px 0;"><strong>Tên đăng nhập:</strong> %s</p>
                        <p style="margin: 8px 0;"><strong>Mật khẩu:</strong> %s</p>
                    </div>

                    <p style="margin-top: 16px;">Vui lòng đăng nhập và <strong>đổi mật khẩu ngay</strong> để đảm bảo an toàn cho tài khoản của bạn.</p>

                    <p style="margin-top: 24px;">Trân trọng,<br/>
                    <em>Đội ngũ hỗ trợ hệ thống</em></p>
                </div>
                <div style="background-color: #f0f0f0; text-align: center; padding: 12px; font-size: 13px; color: #666;">
                    © 2025 Công ty TNHH ABC. Mọi quyền được bảo lưu.
                </div>
            </div>
        </div>
        """.formatted(tenNhanVien, tenTaiKhoan, matKhau);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

}
