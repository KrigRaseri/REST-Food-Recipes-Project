package com.umbrella.recipes.persistence;

import com.umbrella.recipes.model.RecipeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipesDB extends JpaRepository<RecipeModel, Long> {
    Optional<List<RecipeModel>> findByCategoryIgnoreCaseOrderByDateDesc(String category);
    Optional<List<RecipeModel>> findByNameContainingIgnoreCaseOrderByDateDesc(String name);
}
