package com.kh.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchDTO {

    private String userId;          // 아이디
    private String userName;        // 이름
    private String email;           // 이메일
    private String gender;          // 성별 (M/F)
    private String userRole;        // 권한 (ADMIN/USER)
    private String useYn;           // 상태 (Y/N)

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;    // 가입일 시작

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;      // 가입일 종료

    // 검색 조건이 있는지 확인
    public boolean hasSearchCondition() {
        return (userId != null && !userId.isEmpty()) ||
               (userName != null && !userName.isEmpty()) ||
               (email != null && !email.isEmpty()) ||
               (gender != null && !gender.isEmpty()) ||
               (userRole != null && !userRole.isEmpty()) ||
               (useYn != null && !useYn.isEmpty()) ||
               startDate != null ||
               endDate != null;
    }

    // 쿼리 파라미터 생성
    public String getQueryString() {
        StringBuilder sb = new StringBuilder();
        if (userId != null && !userId.isEmpty()) {
            sb.append("&userId=").append(userId);
        }
        if (userName != null && !userName.isEmpty()) {
            sb.append("&userName=").append(userName);
        }
        if (email != null && !email.isEmpty()) {
            sb.append("&email=").append(email);
        }
        if (gender != null && !gender.isEmpty()) {
            sb.append("&gender=").append(gender);
        }
        if (userRole != null && !userRole.isEmpty()) {
            sb.append("&userRole=").append(userRole);
        }
        if (useYn != null && !useYn.isEmpty()) {
            sb.append("&useYn=").append(useYn);
        }
        if (startDate != null) {
            sb.append("&startDate=").append(startDate);
        }
        if (endDate != null) {
            sb.append("&endDate=").append(endDate);
        }
        return sb.toString();
    }
}
