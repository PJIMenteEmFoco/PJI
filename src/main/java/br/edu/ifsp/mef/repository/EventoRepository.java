package br.edu.ifsp.mef.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Calendario;
import br.edu.ifsp.mef.model.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long>{

	List<Evento> findByCalendario(Calendario calendario);
}
