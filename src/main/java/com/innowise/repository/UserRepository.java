package com.innowise.repository;

import com.innowise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByIdIn(List<Long> ids);

    @Query("SELECT u FROM User u WHERE u.email = :userEmail")
    Optional<User> findByEmail(@Param("userEmail") String email);

    @Modifying
    @Query(value = "UPDATE users SET name = COALESCE(:name, name), surname = COALESCE(:surname, surname), birth_date = COALESCE(:birthDate, birth_date) ,email = COALESCE(:email, email) WHERE id = :id", nativeQuery = true)
    int updateUser(@Param("id") Long id, @Param("name") String name, @Param("surname") String surname, @Param("birthDate") LocalDate birthDate, @Param("email") String email);
}
