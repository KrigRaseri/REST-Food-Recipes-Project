package com.umbrella.recipes.persistence;

import com.umbrella.recipes.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findAppUserByUsername(String username);
    boolean existsByUsername(String username);
}
