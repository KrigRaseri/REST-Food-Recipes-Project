package com.umbrella.recipes.web.dto;

import java.util.List;

public record RecipeDTO(
                        String name,
                        String description,
                        String category,
                        String date,
                        List<String> ingredients,
                        List<String> directions
                        ) {
}
