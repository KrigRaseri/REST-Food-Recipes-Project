package com.umbrella.recipes.controller;

import com.umbrella.recipes.dto.RecipeDTO;
import com.umbrella.recipes.model.RecipeModel;
import com.umbrella.recipes.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/api/recipe/{id}")
    public ResponseEntity<RecipeDTO> getRecipe(@PathVariable Long id) {
        Optional<RecipeDTO> recipeDTO = recipeService.getRecipe(id);
        return recipeDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping("/api/recipe/search")
    public ResponseEntity<List<RecipeDTO>> searchRecipe(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "name", required = false) String name) {

        if (category != null && name != null) {
            log.error("Both category and name parameters were provided.");
            return ResponseEntity.badRequest().build();
        }

        if (category != null) {
            // Handle the category parameter
            log.debug("Searching for recipe with category: {}, from the controller.", category);
            List<RecipeDTO> recipes = recipeService.searchRecipeByCategory(category);
            return ResponseEntity.ok(recipes);

        } else if (name != null) {
            // Handle the name parameter
            log.debug("Searching for recipe with name: {}, from the controller.", name);
            List<RecipeDTO> recipes = recipeService.searchRecipeByName(name);
            return ResponseEntity.ok(recipes);
        } else {
            log.error("Neither category nor name parameters were provided.");
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/recipe/new")
    public Map<String, Long> postRecipe(@Valid @RequestBody RecipeModel recipe) {
        RecipeModel recipeModel = recipeService.saveRecipe(recipe);
        return Map.of("id", recipeModel.getRecipeId());
    }

    @PutMapping("/api/recipe/{id}")
    public ResponseEntity<String> updateRecipe(@PathVariable Long id, @Valid @RequestBody RecipeModel recipe) {
        Optional<RecipeDTO> recipeDTO = recipeService.getRecipe(id);

        if (recipeDTO.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        recipe.setRecipeId(id);
        recipeService.saveRecipe(recipe);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/api/recipe/{id}")
    public ResponseEntity<String> deleteRecipe(@PathVariable Long id) {
        boolean deleted = recipeService.deleteRecipe(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
