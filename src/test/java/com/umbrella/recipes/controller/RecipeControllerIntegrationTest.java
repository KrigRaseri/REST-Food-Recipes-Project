package com.umbrella.recipes.controller;

import com.umbrella.recipes.model.RecipeModel;
import com.umbrella.recipes.model.UserModel;
import com.umbrella.recipes.persistence.RecipesRepository;
import com.umbrella.recipes.persistence.UserRepository;
import com.umbrella.recipes.web.dto.RecipeDTO;
import com.umbrella.recipes.web.mapper.RecipeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"unchecked", "rawtypes"})
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecipeControllerIntegrationTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private  RecipesRepository recipesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static String PASS = "test1234";

    List<RecipeModel> li;

    @Test
    void connectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @BeforeEach
    void setUp() {
        UserModel user = new UserModel("test3@test.com", passwordEncoder.encode(PASS), "ROLE_USER");
        li = provideRecipeModelList(user);
        userRepository.saveAndFlush(user);
        recipesRepository.saveAll(li);
    }

    //=========================================Get Mapping=========================================================

    @Test
    void getRecipe_ShouldReturnRecipe_WhenIdExists() {
        // Arrange
        RecipeDTO expectedResponse = recipeMapper.toDTO(recipesRepository.findByIdEager(1L).get());

        // Act
        ResponseEntity<?> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .getForEntity("/api/recipe/1", RecipeDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void getRecipe_ShouldReturnNotFound_WhenIdDoesNotExists() {

        // Act
        ResponseEntity<?> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .getForEntity("/api/recipe/99", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Recipe not found for ID: 99");
    }

    @Test
    void getRecipe_ShouldReturnUnauthorized_WhenUserDoesNotExist() {

        // Act
        ResponseEntity<?> response = restTemplate.getForEntity("/api/recipe/1", RecipeDTO.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void searchRecipe_ShouldReturnListOfValidRecipes_WhenCategoryExists() {

        // Act
        ResponseEntity<List<RecipeDTO>> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .exchange("/api/recipe/search?category=cat1", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {});

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(2);
        assertThat(response.getBody().get(0).category()).isEqualTo("cat1");
        assertThat(response.getBody().get(1).category()).isEqualTo("cat1");
    }

    @Test
    void searchRecipe_ShouldThrowException_WhenCategoryNotFound() {

        // Act
        ResponseEntity<?> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .getForEntity("/api/recipe/search?category=missingNO", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No recipes found for missingNO: missingNO");
    }

    @Test
    void searchRecipe_ShouldReturnListOfValidRecipes_WhenNameExists() {

        // Act
        ResponseEntity<List<RecipeDTO>> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .exchange("/api/recipe/search?name=test", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {});

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(4);
    }

    @Test
    void searchRecipe_ShouldReturnNotFound_WhenNameDoesNotExists() {

        // Act
        ResponseEntity<?> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .getForEntity("/api/recipe/search?name=missingNO", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("No recipes found for missingNO: missingNO");
    }

    @Test
    void searchRecipe_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() {
        // Act
        ResponseEntity<?> responseCategory = restTemplate.exchange("/api/recipe/search?category=test", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        ResponseEntity<?> responseName = restTemplate.exchange("/api/recipe/search?name=test", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        // Assert
        assertThat(responseCategory.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseName.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    //=========================================Post Mapping=========================================================
    @Test
    @Rollback
    void saveRecipe_ShouldCreateRecipe_WhenRecipeIsValid() {
        // Arrange
        RecipeModel recipeModel = provideRecipeModel();

        // Act
        ResponseEntity<Map> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .postForEntity("/api/recipe/new", recipeModel, Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("Recipe created for id", 3);
    }

    @Test
    void saveRecipe_ShouldReturnForbidden_WhenRecipeIsInvalid() {
        // Arrange
        RecipeModel recipeModel = provideInvalidRecipeModel();

        // Act
        ResponseEntity<?> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .postForEntity("/api/recipe/new", recipeModel, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void saveRecipe_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() {
        // Arrange
        RecipeModel recipeModel = provideRecipeModel();

        // Act
        ResponseEntity<?> response = restTemplate.postForEntity("/api/recipe/new", recipeModel, Map.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    //=========================================Put Mapping=========================================================

    @Test
    @Rollback
    void updateRecipe_ShouldUpdateRecipe_WhenUserValid() {
        // Arrange
        RecipeModel recipeModel = provideRecipeModel();
        recipeModel.setName("new name");
        recipeModel.setCategory("new category");

        // Act
        ResponseEntity<?> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .exchange("/api/recipe/3", HttpMethod.PUT, new HttpEntity<>(recipeModel), String.class);

        RecipeModel updatedRecipe = recipesRepository.findById(3L).get();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(updatedRecipe.getName()).isEqualTo("new name");
        assertThat(updatedRecipe.getCategory()).isEqualTo("new category");
    }

    @Test
    void updateRecipe_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() {
        // Arrange
        RecipeModel recipeModel = provideRecipeModel();
        recipeModel.setName("new name");
        recipeModel.setCategory("new category");

        // Act
        ResponseEntity<?> response = restTemplate.exchange("/api/recipe/3", HttpMethod.PUT, new HttpEntity<>(recipeModel), String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    //=========================================Delete Mapping======================================================

    @Test
    @Rollback
    void deleteRecipe_ShouldDeleteRecipe_WhenValidUser() {

        // Act
        ResponseEntity<?> response = restTemplate.withBasicAuth("test3@test.com", PASS)
                .exchange("/api/recipe/4", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteRecipe_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() {
        // Act
        ResponseEntity<?> response = restTemplate.exchange("/api/recipe/4", HttpMethod.DELETE, null, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }



    static RecipeModel provideRecipeModel() {
        return RecipeModel.builder()
                .recipeId(3L)
                .name("TEST POST")
                .description("test")
                .category("test")
                .ingredients(List.of("test"))
                .directions(List.of("test"))
                .build();
    }

    static RecipeModel provideInvalidRecipeModel() {
        return RecipeModel.builder()
                .recipeId(3L)
                .name("")
                .description("")
                .category("test")
                .ingredients(null)
                .directions(List.of("test"))
                .build();
    }

    static List<RecipeModel> provideRecipeModelList(UserModel user) {
        return List.of(
                RecipeModel.builder()
                        .recipeId(1L)
                        .name("test")
                        .description("test")
                        .category("cat1")
                        .ingredients(List.of("test"))
                        .directions(List.of("test"))
                        .userModel(user)
                        .build(),
                RecipeModel.builder()
                        .recipeId(2L)
                        .name("tesT")
                        .description("test2")
                        .category("cat1")
                        .ingredients(List.of("test2"))
                        .directions(List.of("test2"))
                        .userModel(user)
                        .build(),
                RecipeModel.builder()
                        .recipeId(3L)
                        .name("TEST")
                        .description("test2")
                        .category("cat2")
                        .ingredients(List.of("test2"))
                        .directions(List.of("test2"))
                        .userModel(user)
                        .build(),
                RecipeModel.builder()
                        .recipeId(4L)
                        .name("test POST")
                        .description("test2")
                        .category("cat2")
                        .ingredients(List.of("test2"))
                        .directions(List.of("test2"))
                        .userModel(user)
                        .build()
        );
    }
}
