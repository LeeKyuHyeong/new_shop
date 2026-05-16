package com.kh.shop.repository;

import com.kh.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // 휴면 대상 조회 (마지막 로그인이 N일 이전인 활성 사용자)
    @Query("SELECT u FROM User u WHERE u.userStatus = 'ACTIVE' AND u.lastLoginAt < :cutoffDate AND u.useYn = 'Y' AND u.userRole = 'USER'")
    List<User> findDormantCandidates(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 상태별 사용자 수
    long countByUserStatusAndUseYn(String userStatus, String useYn);

    // BCrypt 마이그레이션 진단: 아직 평문 비밀번호로 남아있는 사용자 수.
    // 0 이 되면 UserService.loginUser 의 평문 분기 + DUMMY_BCRYPT_HASH 분기를 정리해도 된다.
    @Query("SELECT COUNT(u) FROM User u WHERE u.userPassword NOT LIKE '$2a$%' AND u.userPassword NOT LIKE '$2b$%'")
    long countPlainPasswordUsers();

    // sunset 권고 메일 발송 대상 (평문 비밀번호 사용자 목록).
    @Query("SELECT u FROM User u WHERE u.userPassword NOT LIKE '$2a$%' AND u.userPassword NOT LIKE '$2b$%' AND u.useYn = 'Y'")
    List<User> findPlainPasswordUsers();

    // 검색 조건으로 사용자 조회
    @Query("SELECT u FROM User u WHERE " +
           "(:userId IS NULL OR :userId = '' OR u.userId LIKE CONCAT('%', :userId, '%')) AND " +
           "(:userName IS NULL OR :userName = '' OR u.userName LIKE CONCAT('%', :userName, '%')) AND " +
           "(:email IS NULL OR :email = '' OR u.email LIKE CONCAT('%', :email, '%')) AND " +
           "(:gender IS NULL OR :gender = '' OR u.gender = :gender) AND " +
           "(:userRole IS NULL OR :userRole = '' OR u.userRole = :userRole) AND " +
           "(:useYn IS NULL OR :useYn = '' OR u.useYn = :useYn) AND " +
           "(:startDate IS NULL OR CAST(u.createdDate AS LocalDate) >= :startDate) AND " +
           "(:endDate IS NULL OR CAST(u.createdDate AS LocalDate) <= :endDate) " +
           "ORDER BY u.createdDate DESC")
    List<User> searchUsers(
            @Param("userId") String userId,
            @Param("userName") String userName,
            @Param("email") String email,
            @Param("gender") String gender,
            @Param("userRole") String userRole,
            @Param("useYn") String useYn,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}