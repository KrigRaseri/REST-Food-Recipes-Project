package com.umbrella.recipes.web.controller;

import com.umbrella.recipes.web.dto.RecipeDTO;
import com.umbrella.recipes.model.RecipeModel;
import com.umbrella.recipes.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            log.debug("Searching for recipe with category: {}, from the controller.", category);
            List<RecipeDTO> recipes = recipeService.searchRecipeByCategory(category);
            return ResponseEntity.ok(recipes);

        } else if (name != null) {
            log.debug("Searching for recipe with name: {}, from the controller.", name);
            List<RecipeDTO> recipes = recipeService.searchRecipeByName(name);
            return ResponseEntity.ok(recipes);
        } else {
            log.error("Neither category nor name parameters were provided.");
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/api/recipe/new")
    public ResponseEntity<Map<String, Long>> postRecipe(@AuthenticationPrincipal UserDetails details, @Valid @RequestBody RecipeModel recipe) {
        if (details == null) {
            log.error("User not found for username: ");
            throw new RuntimeException("User not found");
        }

        log.info("User {} is creating a new recipe", details.getUsername());
        Long id = recipeService.saveRecipe(details.getUsername(), recipe);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("Recipe created for id", id));
    }

    @PutMapping("/api/recipe/{id}")
    public ResponseEntity<String> updateRecipe(@AuthenticationPrincipal UserDetails details,
                                               @PathVariable Long id,
                                               @RequestBody RecipeModel recipeRequest) {

        return recipeService.updateRecipe(details.getUsername(), id, recipeRequest);
    }

    @DeleteMapping("/api/recipe/{id}")
    public ResponseEntity<String> deleteRecipe(@AuthenticationPrincipal UserDetails details, @PathVariable Long id) {
        return recipeService.deleteRecipe(details.getUsername(), id);
    }
}
