package com.cmdlee.quizsushi.quiz.repository;

import com.cmdlee.quizsushi.admin.repository.projection.StatRawProjection;
import com.cmdlee.quizsushi.quiz.domain.model.GuestQuizSolveLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GuestQuizSolveLogRepository extends JpaRepository<GuestQuizSolveLog, Long> {
    @Query(value = """
            SELECT '비회원_퀴즈풀이' AS label,
                   DATE_TRUNC(:type, gs.submitted_at) AS time,
                   COUNT(*) AS count
            FROM guest_quiz_solve_log gs
            WHERE gs.submitted_at >= :start AND gs.submitted_at < :end
            GROUP BY time
            ORDER BY time DESC
            """, nativeQuery = true)
    List<StatRawProjection> findSolvedStats(@Param("type") String type,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end
    );
}
