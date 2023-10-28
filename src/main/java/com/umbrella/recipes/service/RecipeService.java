package com.umbrella.recipes.service;

import com.umbrella.recipes.dto.RecipeDTO;
import com.umbrella.recipes.mapper.RecipeMapper;
import com.umbrella.recipes.model.RecipeModel;
import com.umbrella.recipes.persistence.RecipesDB;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecipeService {

    private final RecipesDB recipesDb;
    private final RecipeMapper mapStructMapper;

    public Optional<RecipeDTO> getRecipe(Long id) {
        log.debug("Searching for recipe with ID: {}", id);
        Optional<RecipeModel> recipeModel = recipesDb.findById(id);

        if (recipeModel.isEmpty()) {
            log.debug("Recipe not found for ID: {}", id);
            return Optional.empty();
        }
        log.info("Recipe found for ID: {}, now mapping to DTO and returning.", id);
        return Optional.of(mapStructMapper.toDTO(recipeModel.get()));
    }


    public RecipeModel saveRecipe(RecipeModel recipe) {
        return recipesDb.saveAndFlush(recipe);
    }


    public boolean deleteRecipe(Long id) {
        log.debug("Searching for recipe with ID: {}", id);
        Optional<RecipeModel> recipeModel = recipesDb.findById(id);

        if (recipeModel.isEmpty()) {
            log.debug("Recipe not found for ID: {}", id);
            return false;
        }

        recipesDb.deleteById(id);
        log.info("Recipe with ID {} deleted.", id);
        return true;
    }


    public List<RecipeDTO> searchRecipeByCategory(String category) {
        return searchRecipes(category, () -> recipesDb.findByCategoryIgnoreCaseOrderByDateDesc(category));
    }

    public List<RecipeDTO> searchRecipeByName(String name) {
        return searchRecipes(name, () -> recipesDb.findByNameContainingIgnoreCaseOrderByDateDesc(name));
    }

    private List<RecipeDTO> searchRecipes(String searchTerm, Supplier<Optional<List<RecipeModel>>> repositorySupplier) {
        log.debug("Searching for recipes with {}: {}", searchTerm, searchTerm);
        Optional<List<RecipeModel>> recipeModels = repositorySupplier.get();

        if (recipeModels.isEmpty() || recipeModels.get().isEmpty()) {
            log.debug("No recipes found for {}: {}", searchTerm, searchTerm);
            return List.of();
        }

        log.info("Recipes found for {}: {}", searchTerm, searchTerm);
        return recipeModels.get().stream().map(mapStructMapper::toDTO).toList();
    }
}
