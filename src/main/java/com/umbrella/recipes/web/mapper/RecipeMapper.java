package com.umbrella.recipes.web.mapper;

import com.umbrella.recipes.web.dto.RecipeDTO;
import com.umbrella.recipes.model.RecipeModel;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    RecipeDTO toDTO(RecipeModel recipe);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRecipeFromDTO(RecipeModel reqBody, @MappingTarget RecipeModel recipe);
}
