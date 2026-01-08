package com.kh.shop.scheduler;

import com.kh.shop.entity.User;
import com.kh.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DormantUserBatchScheduler {

    private final UserRepository userRepository;

    // 휴면 기준 일수 (365일 = 1년)
    private static final int DORMANT_DAYS = 365;

    /**
     * 매일 02:00에 1년 이상 미접속 계정 휴면 전환
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processDormantUsers() {
        log.info("========== [배치] 휴면 계정 처리 시작 ==========");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DORMANT_DAYS);
            List<User> dormantCandidates = userRepository.findDormantCandidates(cutoffDate);

            if (dormantCandidates.isEmpty()) {
                log.info("[배치] 휴면 전환 대상 계정이 없습니다.");
            } else {
                int count = 0;
                for (User user : dormantCandidates) {
                    user.setUserStatus("DORMANT");
                    userRepository.save(user);
                    count++;

                    log.debug("[배치] 휴면 전환: {} (마지막 로그인: {})", user.getUserId(), user.getLastLoginAt());
                }

                log.info("[배치] {}개의 계정이 휴면 전환되었습니다. (기준: {}일 이상 미접속)", count, DORMANT_DAYS);
            }

        } catch (Exception e) {
            log.error("[배치] 휴면 계정 처리 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 휴면 계정 처리 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     * @return 휴면 전환된 계정 개수
     */
    @Transactional
    public int processDormantUsersManual() {
        log.info("[배치] 휴면 계정 처리 수동 실행");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DORMANT_DAYS);
        List<User> dormantCandidates = userRepository.findDormantCandidates(cutoffDate);

        if (dormantCandidates.isEmpty()) {
            log.info("[배치] 휴면 전환 대상 계정이 없습니다.");
            return 0;
        }

        int count = 0;
        for (User user : dormantCandidates) {
            user.setUserStatus("DORMANT");
            userRepository.save(user);
            count++;
        }

        log.info("[배치] {}개의 계정이 휴면 전환되었습니다.", count);
        return count;
    }

    /**
     * 휴면 전환 대상 계정 개수 조회 (미리보기용)
     */
    @Transactional(readOnly = true)
    public int countDormantCandidates() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DORMANT_DAYS);
        return userRepository.findDormantCandidates(cutoffDate).size();
    }

    /**
     * 현재 휴면 계정 수 조회
     */
    @Transactional(readOnly = true)
    public long countDormantUsers() {
        return userRepository.countByUserStatusAndUseYn("DORMANT", "Y");
    }
}