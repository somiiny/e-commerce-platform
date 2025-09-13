package com.sparta.camp.java.FinalProject.domain.user.repository;

import com.sparta.camp.java.FinalProject.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  @Query("SELECT u FROM User u WHERE u.email = :email")
  Optional<User> findUserByEmail(@Param("email") String email);

  @Query(value = "SELECT * FROM user WHERE username = ?1", nativeQuery = true)
  User findByUsernameNative(String username);

}
