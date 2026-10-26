package br.edu.ifsp.mef.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.EventoPsicopedagogo;

public interface EventoPsicopedagogoRepository extends JpaRepository<EventoPsicopedagogo, Long> {

    List<EventoPsicopedagogo> findByPsicopedagogo_IdOrderByDataEventoAscHorarioAsc(Long psicopedagogoId);
}
