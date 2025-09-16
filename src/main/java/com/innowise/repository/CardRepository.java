package com.innowise.repository;

import com.innowise.model.Card;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c FROM Card c WHERE c.id IN :ids")
    List<Card> findAllByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query(value = "UPDATE card_info SET number = COALESCE(:number, number), holder = COALESCE(:holder, holder), expiration_date = COALESCE(:expirationDate, expirationDate) WHERE id = :id", nativeQuery = true)
    int updateCard(@Param("id") Long id, @Param("number") String number, @Param("holder") String holder, @Param("expiration_date") LocalDate expirationDate);

}
