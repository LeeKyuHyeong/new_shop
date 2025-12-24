package com.kh.shop.repository;

import com.kh.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);

    // 전체 사용자 조회 (최신순)
    List<User> findAllByOrderByCreatedDateDesc();

    // 상태별 사용자 조회
    List<User> findByUseYnOrderByCreatedDateDesc(String useYn);

    // 권한별 사용자 조회
    List<User> findByUserRoleOrderByCreatedDateDesc(String userRole);
}