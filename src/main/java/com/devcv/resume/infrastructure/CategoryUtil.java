package com.devcv.resume.infrastructure;


import com.devcv.resume.domain.Category;
import com.devcv.resume.domain.dto.CategoryDto;
import com.devcv.resume.repository.CategoryRepository;

import java.util.List;

public class CategoryUtil {

    // Category 저장 또는 가져오기
    public static Category getOrCreateCategory(CategoryRepository categoryRepository, CategoryDto categoryDto) {
        List<Category> categories = categoryRepository.findByCompanyTypeAndStackType(
                categoryDto.getCompanyType(),
                categoryDto.getStackType()
        );

        if (categories.isEmpty()) {
            return categoryRepository.save(new Category(categoryDto.getCompanyType(), categoryDto.getStackType()));
        } else {
            return categories.get(0);
        }
    }
}