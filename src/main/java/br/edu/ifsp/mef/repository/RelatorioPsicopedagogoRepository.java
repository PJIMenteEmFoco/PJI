package br.edu.ifsp.mef.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.RelatorioPsicopedagogo;

/** Consultas de relatórios manuais do psicopedagogo autenticado. */
public interface RelatorioPsicopedagogoRepository extends JpaRepository<RelatorioPsicopedagogo, Long> {

    List<RelatorioPsicopedagogo> findByPsicopedagogo_IdAndAluno_IdInOrderByDataCriacaoDesc(
            Long psicopedagogoId, Collection<Long> alunoIds);

    Optional<RelatorioPsicopedagogo> findByIdAndPsicopedagogo_Id(Long id, Long psicopedagogoId);
}
