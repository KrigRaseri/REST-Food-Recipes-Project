package com.umbrella.recipes.web.controller;

import com.umbrella.recipes.web.dto.RecipeDTO;
import com.umbrella.recipes.model.RecipeModel;
import com.umbrella.recipes.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller class for managing recipes. Provides RESTful endpoints for retrieving, creating, updating, and deleting recipes.
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
public class RecipeController {

    private final RecipeService recipeService;

    /**
     * Retrieves a recipe by its unique identifier.
     *
     * @param id The ID of the recipe to retrieve.
     * @return A ResponseEntity containing the RecipeDTO if found, or a 404 Not Found response if not found.
     */
    @GetMapping("/api/recipe/{id}")
    public ResponseEntity<RecipeDTO> getRecipe(@PathVariable Long id) {
        Optional<RecipeDTO> recipeDTO = recipeService.getRecipe(id);
        return recipeDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Searches for recipes based on category or name parameters.
     *
     * @param category The category parameter for filtering recipes.
     * @param name     The name parameter for filtering recipes.
     * @return A ResponseEntity containing a list of RecipeDTOs that match the search criteria or a 400 Bad Request if both parameters are provided.
     */
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

    /**
     * Creates a new recipe.
     *
     * @param details The authenticated user details.
     * @param recipe  The RecipeModel object representing the new recipe.
     * @return A ResponseEntity containing the ID of the created recipe or a 500 Internal Server Error if the user is not found.
     */
    @PostMapping("/api/recipe/new")
    public ResponseEntity<Map<String, Long>> postRecipe(@AuthenticationPrincipal UserDetails details, @Valid @RequestBody RecipeModel recipe) {
        if (details == null) {
            log.error("User not found for username: ");
            throw new BadCredentialsException("User not found");
        }
        log.info("User {} is creating a new recipe", details.getUsername());
        Long id = recipeService.saveRecipe(details.getUsername(), recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("Recipe created for id", id));
    }

    /**
     * Updates an existing recipe.
     *
     * @param details        The authenticated user details.
     * @param id             The ID of the recipe to update.
     * @param recipeRequest  The RecipeModel object representing the updated recipe.
     * @return A ResponseEntity with a message indicating the result of the update.
     */
    @PutMapping("/api/recipe/{id}")
    public ResponseEntity<String> updateRecipe(@AuthenticationPrincipal UserDetails details, @PathVariable Long id,
                                               @RequestBody RecipeModel recipeRequest) {
        return recipeService.updateRecipe(details.getUsername(), id, recipeRequest);
    }

    /**
     * Deletes a recipe by its unique identifier.
     *
     * @param details The authenticated user details.
     * @param id      The ID of the recipe to delete.
     * @return A ResponseEntity with a message indicating the result of the deletion.
     */
    @DeleteMapping("/api/recipe/{id}")
    public ResponseEntity<String> deleteRecipe(@AuthenticationPrincipal UserDetails details, @PathVariable Long id) {
        return recipeService.deleteRecipe(details.getUsername(), id);
    }
}
