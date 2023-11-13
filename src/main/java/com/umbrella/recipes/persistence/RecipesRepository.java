package com.umbrella.recipes.persistence;

import com.umbrella.recipes.model.RecipeModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipesRepository extends JpaRepository<RecipeModel, Long> {

    @EntityGraph(attributePaths = {"ingredients"})
    @Query("SELECT r FROM RecipeModel r LEFT JOIN FETCH r.ingredients WHERE r.recipeId = :id")
    Optional<RecipeModel> findByIdWithIngredients(Long id);

    @EntityGraph(attributePaths = {"directions"})
    @Query("SELECT r FROM RecipeModel r LEFT JOIN FETCH r.directions WHERE r.recipeId = :id")
    Optional<RecipeModel> findByIdWithDirections(Long id);

    default Optional<RecipeModel> findByIdEager(Long id) {
        Optional<RecipeModel> recipeWithIngredients = findByIdWithIngredients(id);
        Optional<RecipeModel> recipeWithDirections = findByIdWithDirections(id);
        recipeWithIngredients.ifPresent(recipeModel -> recipeModel.setDirections(recipeWithDirections.get().getDirections()));
        return recipeWithIngredients;
    }

    Optional<List<RecipeModel>> findByCategoryIgnoreCaseOrderByDateDesc(String category);
    Optional<List<RecipeModel>> findByNameContainingIgnoreCaseOrderByDateDesc(String name);
}
