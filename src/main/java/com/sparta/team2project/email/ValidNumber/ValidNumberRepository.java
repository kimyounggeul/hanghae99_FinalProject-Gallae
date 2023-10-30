package com.sparta.team2project.email.ValidNumber;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValidNumberRepository extends JpaRepository<ValidNumber, Long> {
    Optional<ValidNumber> findByEmail(String email);
}
