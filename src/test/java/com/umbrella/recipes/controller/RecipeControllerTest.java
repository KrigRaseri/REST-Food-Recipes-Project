package com.umbrella.recipes.controller;

import com.umbrella.recipes.config.SecurityConfig;
import com.umbrella.recipes.model.UserModel;
import com.umbrella.recipes.web.dto.RecipeDTO;
import com.umbrella.recipes.web.exception.RecipeNotFoundException;
import com.umbrella.recipes.web.mapper.RecipeMapper;
import com.umbrella.recipes.model.RecipeModel;
import com.umbrella.recipes.service.RecipeService;
import com.umbrella.recipes.web.controller.RecipeController;
import com.umbrella.recipes.web.mapper.RecipeMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Import(SecurityConfig.class)
@ExtendWith(MockitoExtension.class)
public class RecipeControllerTest {

    private static final String TEST_USERNAME = "test@test.com";
    private static final String TEST_PASSWORD = "test1234";
    private static final String USER_ROLE = "USER_ROLE";
    private final UserDetails userDetails = new UserModel(TEST_USERNAME, TEST_PASSWORD, USER_ROLE);


    @InjectMocks
    private RecipeController recipeController;

    @Mock
    private RecipeService recipeService;

    private static final RecipeMapper recipeMapper = new RecipeMapperImpl();

    //=========================================Get Mapping=========================================================

    @ParameterizedTest
    @MethodSource("provideRecipeModels")
    void testGetRecipe_ShouldReturnRecipe(RecipeModel recipeModel) {
        // Arrange
        RecipeDTO recipeDTO = recipeMapper.toDTO(recipeModel);
        when(recipeService.getRecipe(recipeModel.getRecipeId())).thenReturn(Optional.of(recipeDTO));

        // Act
        ResponseEntity<RecipeDTO> recipe = recipeController.getRecipe(recipeModel.getRecipeId());

        // Assert
        assertThat(recipe.getBody()).isEqualTo(recipeDTO);
        assertThat(recipe.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testGetRecipe_ShouldReturnNotFound() {
        // Arrange
        when(recipeService.getRecipe(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<RecipeDTO> recipe = recipeController.getRecipe(1L);

        // Assert
        assertThat(recipe.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testSearchRecipeByCategory_ShouldReturnRecipes() {
        // Arrange
        List<RecipeDTO> expectedResults = List.of(recipeMapper.toDTO(provideRecipeModel()));
        when(recipeService.searchRecipeByCategory("Lunch")).thenReturn(expectedResults);

        // Act
        ResponseEntity<List<RecipeDTO>> recipes = recipeController.searchRecipe("Lunch", null);

        // Assert
        assertThat(recipes.getBody()).isEqualTo(expectedResults);
        assertThat(recipes.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testSearchRecipeByName_ShouldReturnRecipes() {
        // Arrange
        List<RecipeDTO> expectedResults = List.of(recipeMapper.toDTO(provideRecipeModel()));
        when(recipeService.searchRecipeByCategory("Recipe 1")).thenReturn(expectedResults);

        // Act
        ResponseEntity<List<RecipeDTO>> recipes = recipeController.searchRecipe("Recipe 1", null);

        // Assert
        assertThat(recipes.getBody()).isEqualTo(expectedResults);
        assertThat(recipes.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    //========================================Post Mapping=========================================================

    @ParameterizedTest
    @MethodSource("provideRecipeModels")
    void testPostRecipe_ShouldReturnCreated(RecipeModel recipeModel) {
        // Arrange
        when(recipeService.saveRecipe(TEST_USERNAME, recipeModel)).thenReturn(recipeModel.getRecipeId());

        // Act
        ResponseEntity<Map<String, Long>> response = recipeController.postRecipe(userDetails, recipeModel);

        // Assert
        assertThat(response.getBody()).isEqualTo(Map.of("Recipe created for id", recipeModel.getRecipeId()));
    }

    //=======================================Put Mapping========================================================

    @ParameterizedTest
    @MethodSource("provideRecipeModels")
    void testUpdateRecipe_ShouldReturnNoContent(RecipeModel recipeModel) {
        // Arrange
        when(recipeService.updateRecipe(TEST_USERNAME, recipeModel.getRecipeId(), recipeModel)).then(
                invocation -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        // Act
        ResponseEntity<String> response = recipeController.updateRecipe(userDetails, recipeModel.getRecipeId(), recipeModel);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testUpdateRecipe_ShouldReturnNotFound() {
        // Arrange
        RecipeModel recipeModel = provideRecipeModel();
        when(recipeService.updateRecipe(TEST_USERNAME, recipeModel.getRecipeId(), recipeModel))
                .thenThrow(new RecipeNotFoundException("Recipe not found for ID: " + recipeModel.getRecipeId()));

        // Assert
        assertThatThrownBy(() -> recipeController.updateRecipe(userDetails, recipeModel.getRecipeId(), recipeModel))
                .isInstanceOf(RecipeNotFoundException.class)
                .hasMessage("Recipe not found for ID: " + recipeModel.getRecipeId());
    }


    //=======================================Delete Mapping=========================================================
    @Test
    void testDeleteRecipe_ShouldReturnNoContent() {
        // Arrange
        when(recipeService.deleteRecipe(TEST_USERNAME, 1L)).thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());

        // Act
        ResponseEntity<String> response = recipeController.deleteRecipe(userDetails,1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    //=======================================Provider methods=========================================================
    private static RecipeModel provideRecipeModel() {
        return RecipeModel.builder()
                .recipeId(1L)
                .name("Recipe 1")
                .category("Category 1")
                .description("Description 1")
                .ingredients(List.of("Ingredient1 1", "Ingredient1 2"))
                .directions(List.of("Direction1 1", "Direction1 2"))
                .build();
    }

    private static Stream<Arguments> provideRecipeModels() {
        return Stream.of(
                Arguments.of(RecipeModel.builder()
                        .recipeId(1L)
                        .name("Recipe 1")
                        .category("Category 1")
                        .description("Description 1")
                        .ingredients(List.of("Ingredient1 1", "Ingredient1 2"))
                        .directions(List.of("Direction1 1", "Direction1 2"))
                        .build()),
                Arguments.of(RecipeModel.builder()
                        .recipeId(2L)
                        .name("Recipe 2")
                        .category("Category 2")
                        .description("Description 2")
                        .ingredients(List.of("Ingredient2 1", "Ingredient2 2"))
                        .directions(List.of("Direction2 1", "Direction2 2"))
                        .build()),
                Arguments.of(RecipeModel.builder()
                        .recipeId(3L)
                        .name("Recipe 3")
                        .category("Category 3")
                        .description("Description 3")
                        .ingredients(List.of("Ingredient3 1", "Ingredient3 2"))
                        .directions(List.of("Direction3 1", "Direction3 2"))
                        .build())
        );
    }
}
