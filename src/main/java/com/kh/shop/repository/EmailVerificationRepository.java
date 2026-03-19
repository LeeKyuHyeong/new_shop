package com.kh.shop.repository;

import com.kh.shop.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndVerificationCode(String email, String verificationCode);

    Optional<EmailVerification> findByEmailAndVerifiedTrue(String email);

    void deleteByEmail(String email);
}
