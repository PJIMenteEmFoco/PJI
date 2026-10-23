package br.edu.ifsp.mef.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long>{

}
