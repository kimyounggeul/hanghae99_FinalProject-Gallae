package com.sparta.team2project.email;


import com.sparta.team2project.commons.Util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final MailSender mailSender;
    private final RedisUtil redisUtil;


    //이메일 인증
    public void sendNumber(int number, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email); //수신자 설정
        message.setSubject("갈래! 이메일 인증 번호입니다."); //메일 제목
        message.setText("인증번호: " + number); //메일 내용 설정
        message.setFrom("hanghaestudy@gmail.com"); //발신자 설정
        redisUtil.setDataExpire(email, String.valueOf(number), 180000); //3분
        mailSender.send(message);
    }
}
