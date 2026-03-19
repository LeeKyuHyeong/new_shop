package com.kh.shop.service;

import com.kh.shop.entity.EmailVerification;
import com.kh.shop.repository.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 5;

    @Transactional
    public void sendVerificationCode(String email) {
        // 기존 인증코드 삭제
        emailVerificationRepository.deleteByEmail(email);

        // 6자리 인증코드 생성
        String code = generateCode();

        // DB 저장
        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .verificationCode(code)
                .expiryDate(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .verified(false)
                .build();
        emailVerificationRepository.save(verification);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[KH SHOP] 이메일 인증코드");
        message.setText("안녕하세요, KH SHOP입니다.\n\n"
                + "이메일 인증코드: " + code + "\n\n"
                + "인증코드는 " + EXPIRY_MINUTES + "분간 유효합니다.\n"
                + "본인이 요청하지 않은 경우 이 메일을 무시해주세요.");
        mailSender.send(message);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        Optional<EmailVerification> verificationOpt =
                emailVerificationRepository.findByEmailAndVerificationCode(email, code);

        if (verificationOpt.isEmpty()) {
            return false;
        }

        EmailVerification verification = verificationOpt.get();

        // 만료 확인
        if (verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 인증 완료 처리
        verification.setVerified(true);
        emailVerificationRepository.save(verification);
        return true;
    }

    public boolean isEmailVerified(String email) {
        return emailVerificationRepository.findByEmailAndVerifiedTrue(email).isPresent();
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }
}
