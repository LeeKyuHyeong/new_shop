package com.kh.shop.service;

import com.kh.shop.entity.EmailVerification;
import com.kh.shop.repository.EmailVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private final String testEmail = "test@example.com";

    @Test
    @DisplayName("인증코드 발송 - 6자리 숫자 코드를 생성하고 이메일을 발송한다")
    void sendVerificationCode_shouldGenerateCodeAndSendEmail() {
        // given
        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        emailVerificationService.sendVerificationCode(testEmail);

        // then
        // 기존 인증코드 삭제 확인
        verify(emailVerificationRepository).deleteByEmail(testEmail);

        // 인증코드 저장 확인
        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationRepository).save(captor.capture());

        EmailVerification saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(testEmail);
        assertThat(saved.getVerificationCode()).matches("\\d{6}"); // 6자리 숫자
        assertThat(saved.getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(saved.getVerified()).isFalse();

        // 이메일 발송 확인
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("인증코드 검증 성공 - 유효한 코드와 만료되지 않은 시간")
    void verifyCode_withValidCode_shouldReturnTrue() {
        // given
        EmailVerification verification = EmailVerification.builder()
                .email(testEmail)
                .verificationCode("123456")
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        when(emailVerificationRepository.findByEmailAndVerificationCode(testEmail, "123456"))
                .thenReturn(Optional.of(verification));

        // when
        boolean result = emailVerificationService.verifyCode(testEmail, "123456");

        // then
        assertThat(result).isTrue();
        assertThat(verification.getVerified()).isTrue();
        verify(emailVerificationRepository).save(verification);
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 잘못된 코드")
    void verifyCode_withInvalidCode_shouldReturnFalse() {
        // given
        when(emailVerificationRepository.findByEmailAndVerificationCode(testEmail, "999999"))
                .thenReturn(Optional.empty());

        // when
        boolean result = emailVerificationService.verifyCode(testEmail, "999999");

        // then
        assertThat(result).isFalse();
        verify(emailVerificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("인증코드 검증 실패 - 만료된 코드")
    void verifyCode_withExpiredCode_shouldReturnFalse() {
        // given
        EmailVerification verification = EmailVerification.builder()
                .email(testEmail)
                .verificationCode("123456")
                .expiryDate(LocalDateTime.now().minusMinutes(1)) // 만료됨
                .verified(false)
                .build();

        when(emailVerificationRepository.findByEmailAndVerificationCode(testEmail, "123456"))
                .thenReturn(Optional.of(verification));

        // when
        boolean result = emailVerificationService.verifyCode(testEmail, "123456");

        // then
        assertThat(result).isFalse();
        assertThat(verification.getVerified()).isFalse();
    }

    @Test
    @DisplayName("이메일 인증 완료 여부 확인 - 인증 완료된 경우")
    void isEmailVerified_withVerifiedEmail_shouldReturnTrue() {
        // given
        EmailVerification verification = EmailVerification.builder()
                .email(testEmail)
                .verified(true)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        when(emailVerificationRepository.findByEmailAndVerifiedTrue(testEmail))
                .thenReturn(Optional.of(verification));

        // when
        boolean result = emailVerificationService.isEmailVerified(testEmail);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일 인증 완료 여부 확인 - 인증되지 않은 경우")
    void isEmailVerified_withUnverifiedEmail_shouldReturnFalse() {
        // given
        when(emailVerificationRepository.findByEmailAndVerifiedTrue(testEmail))
                .thenReturn(Optional.empty());

        // when
        boolean result = emailVerificationService.isEmailVerified(testEmail);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("인증코드 발송 시 이메일 제목과 내용이 올바른지 확인")
    void sendVerificationCode_shouldSendEmailWithCorrectContent() {
        // given
        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        emailVerificationService.sendVerificationCode(testEmail);

        // then
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());

        SimpleMailMessage sentMail = mailCaptor.getValue();
        assertThat(sentMail.getTo()).contains(testEmail);
        assertThat(sentMail.getSubject()).contains("KH SHOP");
        assertThat(sentMail.getText()).contains("인증");
    }
}
