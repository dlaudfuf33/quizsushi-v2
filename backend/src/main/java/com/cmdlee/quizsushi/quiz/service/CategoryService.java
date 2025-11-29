package com.cmdlee.quizsushi.quiz.service;

import com.cmdlee.quizsushi.quiz.dto.response.CategoryResponse;
import com.cmdlee.quizsushi.quiz.dto.response.IntroductionCategoryResponse;
import com.cmdlee.quizsushi.quiz.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<IntroductionCategoryResponse> findIntroductionCategories() {
        return categoryRepository.findAllForIntroduction();
    }
}
