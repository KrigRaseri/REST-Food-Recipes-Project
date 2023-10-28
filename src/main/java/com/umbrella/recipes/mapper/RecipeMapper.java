package com.umbrella.recipes.mapper;

import com.umbrella.recipes.dto.RecipeDTO;
import com.umbrella.recipes.model.RecipeModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecipeMapper {
    RecipeDTO toDTO(RecipeModel recipe);
}
