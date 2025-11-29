package com.cmdlee.quizsushi.quiz.repository;

import com.cmdlee.quizsushi.quiz.domain.model.Category;
import com.cmdlee.quizsushi.quiz.dto.response.IntroductionCategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    @Query("""
                SELECT new com.cmdlee.quizsushi.quiz.dto.response.IntroductionCategoryResponse(
                    c.id,
                    c.title,
                    c.description,
                    COUNT(q.id),
                    c.icon
                )
                FROM Category c
                LEFT JOIN c.quizzes q
                GROUP BY c.id, c.title, c.description, c.icon
            """)
    List<IntroductionCategoryResponse> findAllForIntroduction();
}
