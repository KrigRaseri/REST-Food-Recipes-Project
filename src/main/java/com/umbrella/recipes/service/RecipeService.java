package com.umbrella.recipes.service;

import com.umbrella.recipes.model.UserModel;
import com.umbrella.recipes.persistence.UserRepository;
import com.umbrella.recipes.web.dto.RecipeDTO;
import com.umbrella.recipes.web.exception.RecipeNotFoundException;
import com.umbrella.recipes.web.exception.UnauthorizedUserException;
import com.umbrella.recipes.web.mapper.RecipeMapper;
import com.umbrella.recipes.model.RecipeModel;
import com.umbrella.recipes.persistence.RecipesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecipeService {

    private final RecipesRepository recipesRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;

    public Optional<RecipeDTO> getRecipe(Long id) {
        log.debug("Searching for recipe with ID: {}", id);
        RecipeModel recipeModel = recipesRepository.findById(id).orElseThrow(() -> {
            log.error("Recipe not found for ID: {}", id);
            return new RecipeNotFoundException("Recipe not found for ID: " + id);
        });

        log.info("Recipe found for ID: {}, now mapping to DTO and returning.", id);
        return Optional.of(recipeMapper.toDTO(recipeModel));
    }


    public Long saveRecipe(String currentUser, RecipeModel recipe) {
        Optional<UserModel> userModel = userRepository.findAppUserByUsername(currentUser);
        if (userModel.isEmpty()) {
            log.error("User not found for username: {}", currentUser);
            throw new RuntimeException("User not found");
        }
        recipe.setUserModel(userModel.get());
        recipesRepository.saveAndFlush(recipe);
        return recipe.getRecipeId();
    }

    public ResponseEntity<String> updateRecipe(String currentUser, Long id, RecipeModel recipeRequest) {
        RecipeModel recipeModel = recipesRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Recipe not found for ID: {}", id);
                    return new RecipeNotFoundException("Recipe not found for ID: " + id);
                });

        String recipeUsername = recipeModel.getUserModel().getUsername();
        if (!recipeUsername.equals(currentUser)) {
            log.error("User {} is not authorized to update recipeRequest with ID: {}", currentUser, recipeRequest.getRecipeId());
            throw new UnauthorizedUserException("User not authorized to update recipeRequest with ID: " + recipeModel.getRecipeId());
        }

        recipeRequest.setRecipeId(recipeModel.getRecipeId());
        recipeMapper.updateRecipeFromDTO(recipeRequest, recipeModel);
        log.info("User {} is updating recipe with ID: {}", currentUser, recipeRequest.getRecipeId());
        recipesRepository.saveAndFlush(recipeModel);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public ResponseEntity<String> deleteRecipe(String currentUser, Long id) {
        RecipeModel recipeModel = recipesRepository.findById(id).orElseThrow(() -> {
            log.error("Recipe not found for ID: {}", id);
            return new RecipeNotFoundException("Recipe not found for ID: " + id);
        });

        String recipeUsername = recipeModel.getUserModel().getUsername();
        if (!recipeUsername.equals(currentUser)) {
            log.error("not authorized to delete recipe with ID: {}", id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        recipesRepository.deleteById(id);
        log.info("Recipe with ID {} deleted.", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    public List<RecipeDTO> searchRecipeByCategory(String category) {
        return searchRecipes(category, () -> recipesRepository.findByCategoryIgnoreCaseOrderByDateDesc(category));
    }

    public List<RecipeDTO> searchRecipeByName(String name) {
        return searchRecipes(name, () -> recipesRepository.findByNameContainingIgnoreCaseOrderByDateDesc(name));
    }

    private List<RecipeDTO> searchRecipes(String searchTerm, Supplier<Optional<List<RecipeModel>>> repositorySupplier) {
        log.debug("Searching for recipes with {}: {}", searchTerm, searchTerm);
        Optional<List<RecipeModel>> recipeModels = repositorySupplier.get();

        if (recipeModels.isEmpty() || recipeModels.get().isEmpty()) {
            log.debug("No recipes found for {}: {}", searchTerm, searchTerm);
            return List.of();
        }

        log.info("Recipes found for {}: {}", searchTerm, searchTerm);
        return recipeModels.get().stream().map(recipeMapper::toDTO).toList();
    }
}
