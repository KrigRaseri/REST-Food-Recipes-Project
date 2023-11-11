package com.umbrella.recipes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a recipe entity with various attributes including name, description, category,
 * creation date, ingredients, directions, and the user who created the recipe. Yes I know I don't have to add model
 * to the end of the class name but it just kinda happened.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long recipeId;

    @NotBlank(message = "Recipe name is mandatory")
    private String name;

    @NotBlank(message = "Recipe description is mandatory")
    private String description;

    @NotBlank(message = "Recipe category is mandatory")
    private String category;

    @UpdateTimestamp
    private LocalDateTime date;


    @NotNull(message = "Recipe ingredients are mandatory")
    @Size(min = 1, message = "At least one ingredient is required")
    @ElementCollection
    private List<String> ingredients;


    @NotNull(message = "Recipe directions are mandatory")
    @Size(min = 1, message = "At least one direction is required")
    @ElementCollection
    private List<String> directions;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username")
    private UserModel userModel;
}
