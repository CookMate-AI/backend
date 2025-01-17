package com.project.cook_mate.recipe.model;

import com.project.cook_mate.category.model.Category;
import com.project.cook_mate.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recipe")
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Integer recipeId;

    @Column(name = "food_name", length = 50, nullable = false)
    private String foodName;

    @Column(name = "content", length = 2000, nullable = false)
    private String content;

    @Column(name = "url", length = 100, nullable = false)
    private String url;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id", nullable = false)
    private Category category;
}
