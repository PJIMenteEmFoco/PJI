
package br.edu.ifsp.mef.service;

import java.time.LocalDate;
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
	FeriadoRepository feriadoRepository;

	public void sincronizarAno(int ano) {

		RestClient restClient = RestClient.create();

		List<FeriadoApiDTO> feriadosApi = restClient.get().uri("https://brasilapi.com.br/api/feriados/v1/{ano}", ano)
				.retrieve().body(new ParameterizedTypeReference<List<FeriadoApiDTO>>() {
				});

		if (feriadosApi == null || feriadosApi.isEmpty()) {
			throw new IllegalStateException("A API não retornou feriados para " + ano);
		}

		List<FeriadoApiDTO> nacionais = feriadosApi.stream()
				.filter(f -> f.type() != null && f.type().equalsIgnoreCase("national")).toList();

		if (nacionais.isEmpty()) {
			throw new IllegalStateException("Nenhum feriado nacional foi retornado para " + ano);
		}
	}

	public List<Feriado> buscarEntre(LocalDate inicio, LocalDate fim) {

		return feriadoRepository.findByDataFeriadoBetweenOrderByDataFeriadoAsc(inicio, fim);
	}
}
