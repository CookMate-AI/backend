package com.project.cook_mate.category.repository;

import com.project.cook_mate.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
