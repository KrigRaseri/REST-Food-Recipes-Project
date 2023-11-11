package com.umbrella.recipes.web.dto;

import java.util.List;

/**
 * DTO class for representing a recipe.
 */
public record RecipeDTO(
                        String name,
                        String description,
                        String category,
                        String date,
                        List<String> ingredients,
                        List<String> directions
                        ) {
}
