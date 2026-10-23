package br.edu.ifsp.mef.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.ConviteTurma;

public interface ConviteTurmaRepository extends JpaRepository<ConviteTurma, Long> {
    Optional<ConviteTurma> findById(Long token);
}