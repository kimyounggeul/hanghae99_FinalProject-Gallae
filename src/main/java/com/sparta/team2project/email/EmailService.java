package com.sparta.team2project.email;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final MailSender mailSender;

    //이메일 인증
    public void sendNumber(int number, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email); //수신자 설정
        message.setSubject("갈래! 이메일 인증 번호입니다."); //메일 제목
        message.setText("인증번호: " + number); //메일 내용 설정
        message.setFrom("hanghaestudy@gmail.com"); //발신자 설정
        mailSender.send(message);
    }
}
