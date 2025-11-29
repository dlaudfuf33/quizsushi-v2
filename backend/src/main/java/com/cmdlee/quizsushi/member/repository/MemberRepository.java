package com.cmdlee.quizsushi.member.repository;

import com.cmdlee.quizsushi.admin.repository.projection.StatRawProjection;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<QuizsushiMember, Long> {
    @Query(value = """
                SELECT '가입자' AS label,
                       DATE_TRUNC(:type, m.created_at) AS time,
                       COUNT(*) AS count
                FROM quizsushi_member m
                WHERE m.created_at >= :start AND m.created_at < :end
                GROUP BY time
                ORDER BY time DESC
            
            """, nativeQuery = true)
    List<StatRawProjection> findJoinStats(
            @Param("type") String type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
                SELECT m FROM QuizsushiMember m
                WHERE
                  (:searchQuery IS NULL OR m.email LIKE :searchQuery
                                      OR m.nickname LIKE :searchQuery)
                AND (
                  :status IS NULL OR
                  (:status = '탈퇴' AND m.isDeleted = true) OR
                  (:status = '정지' AND m.isBan = true ) OR
                  (:status = '활성' AND m.isBan = false AND m.isDeleted = false)
                )
            """)
    Page<QuizsushiMember> searchMembers(
            @Param("searchQuery") String searchQuery,
            @Param("status") String status,
            Pageable pageable
    );
}
