package com.cmdlee.quizsushi.report.repository;

import com.cmdlee.quizsushi.report.model.Report;
import com.cmdlee.quizsushi.admin.repository.projection.StatRawProjection;
import com.cmdlee.quizsushi.report.model.enums.ReportStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @EntityGraph(attributePaths = {"member"})
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = """
                SELECT '신고' AS label,
                       DATE_TRUNC(:type, r.created_at) AS time,
                       COUNT(*) AS count
                FROM report r
                WHERE r.created_at >= :start AND r.created_at < :end
                GROUP BY time
                ORDER BY time DESC
            """, nativeQuery = true)
    List<StatRawProjection> findReportStats(@Param("type") String type,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end
    );

    @Query("""
              SELECT r FROM Report r
              WHERE
                (:searchQuery IS NULL OR
                  r.title LIKE :searchQuery OR
                  r.message LIKE :searchQuery OR
                  r.reporter.email LIKE :searchQuery OR
                  r.reporter.nickname  LIKE :searchQuery
                )
              AND (:status IS NULL OR r.status = :status)
            """)
    Page<Report> searchReports(
            @Param("searchQuery") String searchQuery,
            @Param("status") ReportStatusType status,
            Pageable pageable
    );
}
