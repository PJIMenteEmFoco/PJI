package br.edu.ifsp.mef.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Feriado;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {

	Optional<Feriado> findByDataFeriado(LocalDate dataFeriado);

	List<Feriado> findByDataFeriadoBetweenOrderByDataFeriadoAsc(LocalDate inicio, LocalDate fim);
}
