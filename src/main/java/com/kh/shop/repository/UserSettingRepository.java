package com.kh.shop.repository;

import com.kh.shop.entity.User;
import com.kh.shop.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {

    Optional<UserSetting> findByUser(User user);

    Optional<UserSetting> findByUser_UserId(String userId);

    void deleteByUser(User user);
}