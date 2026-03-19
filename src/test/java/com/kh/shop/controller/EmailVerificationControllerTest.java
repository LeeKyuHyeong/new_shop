package com.kh.shop.controller;

import com.kh.shop.controller.common.ApiController;
import com.kh.shop.service.EmailVerificationService;
import com.kh.shop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private ApiController apiController;

    @Test
    @DisplayName("인증코드 발송 API - 성공")
    void sendVerificationCode_success() {
        // given
        doNothing().when(emailVerificationService).sendVerificationCode("test@example.com");
        when(userService.isDuplicateEmail("test@example.com")).thenReturn(false);

        // when
        Map<String, Object> response = apiController.sendVerificationCode("test@example.com");

        // then
        assertThat(response.get("success")).isEqualTo(true);
        assertThat(response.get("message")).isNotNull();
        verify(emailVerificationService).sendVerificationCode("test@example.com");
    }

    @Test
    @DisplayName("인증코드 발송 API - 이미 사용 중인 이메일")
    void sendVerificationCode_duplicateEmail() {
        // given
        when(userService.isDuplicateEmail("existing@example.com")).thenReturn(true);

        // when
        Map<String, Object> response = apiController.sendVerificationCode("existing@example.com");

        // then
        assertThat(response.get("success")).isEqualTo(false);
        assertThat(response.get("message")).isEqualTo("이미 사용 중인 이메일입니다");
        verify(emailVerificationService, never()).sendVerificationCode(any());
    }

    @Test
    @DisplayName("인증코드 검증 API - 성공")
    void verifyEmailCode_success() {
        // given
        when(emailVerificationService.verifyCode("test@example.com", "123456")).thenReturn(true);

        // when
        Map<String, Object> response = apiController.verifyEmailCode("test@example.com", "123456");

        // then
        assertThat(response.get("success")).isEqualTo(true);
    }

    @Test
    @DisplayName("인증코드 검증 API - 실패")
    void verifyEmailCode_failure() {
        // given
        when(emailVerificationService.verifyCode("test@example.com", "999999")).thenReturn(false);

        // when
        Map<String, Object> response = apiController.verifyEmailCode("test@example.com", "999999");

        // then
        assertThat(response.get("success")).isEqualTo(false);
    }
}
