package com.umbrella.recipes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    //@OneToMany(cascade = CascadeType.ALL)
    //@JoinColumn(name = "recipe_id")
    @NotNull(message = "Recipe ingredients are mandatory")
    @Size(min = 1, message = "At least one ingredient is required")
    @ElementCollection
    private List<String> ingredients;

    //@OneToMany(cascade = CascadeType.ALL)
    //@JoinColumn(name = "recipe_id")
    @NotNull(message = "Recipe directions are mandatory")
    @Size(min = 1, message = "At least one direction is required")
    @ElementCollection
    private List<String> directions;
}
