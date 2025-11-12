package com.example.project.repository;

import com.example.project.entity.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByTagId(String tagId);

    List<User> findByNicknameContainingIgnoreCase(String nickname);

    @Query("SELECT u FROM User u LEFT JOIN u.followers f GROUP BY u ORDER BY COUNT(f) DESC")
    Page<User> findPopularUsers(Pageable pageable);
}
