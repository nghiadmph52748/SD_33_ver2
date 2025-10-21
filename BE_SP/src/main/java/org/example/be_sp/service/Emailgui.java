package org.example.be_sp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class Emailgui {
    @Autowired
    private JavaMailSender mailSender;

    public void sendAccountInfo(String toEmail, String tenNhanVien, String matKhau) {
        String subject = "Thông tin tài khoản nhân viên mới";
        String body = String.format(
                "Xin chào %s,\n\nTài khoản nhân viên của bạn đã được tạo thành công.\n\n" +
                        "Email đăng nhập: %s\nMật khẩu: %s\n\nVui lòng đăng nhập và đổi mật khẩu sớm nhất.\n\nTrân trọng,\nĐội ngũ hỗ trợ.",
                tenNhanVien, toEmail, matKhau
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}