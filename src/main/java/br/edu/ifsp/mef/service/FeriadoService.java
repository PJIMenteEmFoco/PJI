package br.edu.ifsp.mef.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.edu.ifsp.mef.model.Feriado;
import br.edu.ifsp.mef.model.FeriadoApiDTO;
import br.edu.ifsp.mef.repository.FeriadoRepository;

@Service
public class FeriadoService {

    @Autowired
    private FeriadoRepository feriadoRepository;

    public List<FeriadoApiDTO> consultarApi(int ano) {

        RestClient restClient = RestClient.create();

        List<FeriadoApiDTO> resposta = restClient.get()
            .uri("https://brasilapi.com.br/api/feriados/v1/{ano}", ano)
            .retrieve()
            .body(new ParameterizedTypeReference<List<FeriadoApiDTO>>() {});

        if (resposta == null || resposta.isEmpty()) {
            throw new IllegalStateException(
                "Nenhum feriado retornado para " + ano
            );
        }

        List<FeriadoApiDTO> nacionais = resposta.stream()
            .filter(f -> "national".equalsIgnoreCase(f.type()))
            .filter(f -> f.date() != null
                && f.name() != null
                && !f.name().isBlank())
            .toList();

        if (nacionais.isEmpty()) {
            throw new IllegalStateException(
                "Nenhum feriado nacional encontrado para " + ano
            );
        }

        return nacionais;
    }

    public void salvarFeriados(List<FeriadoApiDTO> feriados) {

        List<Feriado> novos = new ArrayList<>();

        for (FeriadoApiDTO dto : feriados) {

            boolean existe = feriadoRepository
                .findByDataFeriado(dto.date())
                .isPresent();

            if (!existe) {
                Feriado feriado = new Feriado(
                    dto.name(),
                    dto.date()
                );

                novos.add(feriado);
            }
        }

        feriadoRepository.saveAll(novos);
    }

    public void sincronizarAno(int ano) {

        List<FeriadoApiDTO> feriados = consultarApi(ano);

        salvarFeriados(feriados);
    }


    public List<Feriado> buscarEntre(
        LocalDate inicio,
        LocalDate fim
    ) {
        return feriadoRepository
            .findByDataFeriadoBetweenOrderByDataFeriadoAsc(
                inicio, fim
            );
    }

    public List<Feriado> buscarOuSincronizarAno(int ano) {

        LocalDate inicio = LocalDate.of(ano, 1, 1);
        LocalDate fim = LocalDate.of(ano, 12, 31);

        List<Feriado> feriados = buscarEntre(inicio, fim);

        if (feriados.isEmpty()) {
            sincronizarAno(ano);
            feriados = buscarEntre(inicio, fim);
        }

        return feriados;
    }
}
