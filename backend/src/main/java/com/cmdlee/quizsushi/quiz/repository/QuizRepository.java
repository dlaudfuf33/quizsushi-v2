package com.cmdlee.quizsushi.quiz.repository;

import com.cmdlee.quizsushi.admin.repository.projection.StatRawProjection;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = {"category"})
    Page<Quiz> findAllByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Quiz> findAll(Pageable pageable);

    @Query("""
                SELECT q FROM Quiz q
                JOIN FETCH q.category c
                WHERE q.title LIKE :query
                  AND c.id = :categoryId
            """)
    Page<Quiz> searchQuizTitleWithCategory(@Param("query") String query, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("""
            SELECT q FROM Quiz q
            JOIN FETCH q.category c
            WHERE q.title = :query
            """)
    Page<Quiz> searchQuizTitleNoCategory(@Param("query") String query, Pageable pageable);

    @Query("""
                SELECT q FROM Quiz q
                JOIN FETCH q.category c
                WHERE q.author.nickname LIKE :query
                  AND c.id = :categoryId
            """)
    Page<Quiz> searchQuizAuthorNameWithCategory(@Param("query") String query, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("""
            SELECT q FROM Quiz q
            JOIN FETCH q.category c
            WHERE q.author.nickname LIKE :query
            """)
    Page<Quiz> searchQuizAuthorNameNoCategory(@Param("query") String query, Pageable pageable);

    @Query("""
                SELECT q FROM Quiz q
                JOIN FETCH q.category
                WHERE q.id = :id
            """)
    Optional<Quiz> findQuizDetailById(@Param("id") Long id);

    @Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.quizRatings c WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithRating(@Param("quizId") Long quizId);

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.author.id = :memberId")
    int countByAuthorId(@Param("memberId") long memberId);

    @Query("SELECT q FROM Quiz q WHERE q.author.id = :memberId")
    Page<Quiz> findCreatedByAuthor(@Param("memberId") long memberId, Pageable pageable);

    @Query(value = """
                SELECT '출제' AS label,
                       DATE_TRUNC(:type, q.created_at) AS time,
                       COUNT(*) AS count
                FROM quiz q
                WHERE q.created_at >= :start AND q.created_at < :end
                GROUP BY time
                ORDER BY time DESC
            """, nativeQuery = true)
    List<StatRawProjection> findCreatedStats(@Param("type") String type,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end
            );

    List<Quiz> findByAuthor(QuizsushiMember member);
}
