package com.innowise.repository;

import com.innowise.model.Card;
import com.innowise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c.user FROM Card c WHERE c.id = :cardId")
    Optional<User> findOwnerIdById(@Param("cardId") Long cardId);
}
