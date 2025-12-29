package com.kh.shop.repository;

import com.kh.shop.entity.SocialAccount;
import com.kh.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    // 제공자와 제공자ID로 찾기
    Optional<SocialAccount> findByProviderAndProviderId(String provider, String providerId);

    // 사용자의 소셜 계정 목록
    List<SocialAccount> findByUser(User user);

    // 사용자 ID로 소셜 계정 찾기
    List<SocialAccount> findByUser_UserId(String userId);

    // 특정 제공자로 연결된 계정 확인
    Optional<SocialAccount> findByUser_UserIdAndProvider(String userId, String provider);

    // 소셜 계정 삭제
    void deleteByUserAndProvider(User user, String provider);
}