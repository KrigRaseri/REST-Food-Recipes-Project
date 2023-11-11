package com.umbrella.recipes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Represents a user entity with various attributes including username, password, and authority to be used with user
 * authentication and authorization. Yes I know I don't have to add model to the end of the class name, but in this case
 * I didn't know how it might affect the UserDetails interface.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class UserModel implements UserDetails {

    @Id
    @Column(unique=true)
    @Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@"
            + "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    @NotBlank(message = "Password is mandatory")
    private String password;

    private String authority;

    @OneToMany(mappedBy = "userModel")
    private List<RecipeModel> recipes;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UserModel(String username, String password, String authority) {
        this.username = username;
        this.password = password;
        this.authority = authority;
    }
}
