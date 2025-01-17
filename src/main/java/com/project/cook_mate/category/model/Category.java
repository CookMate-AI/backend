package com.project.cook_mate.category.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
@Builder
public class Category {

    @Id
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name", length = 20, nullable = false)
    private String categoryName;
}
