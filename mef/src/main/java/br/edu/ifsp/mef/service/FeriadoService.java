
package br.edu.ifsp.mef.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import br.edu.ifsp.mef.model.Feriado;
import br.edu.ifsp.mef.repository.FeriadoApiDTO;
import br.edu.ifsp.mef.repository.FeriadoRepository;

@Service
public class FeriadoService {

	@Autowired
	FeriadoRepository feriadoRepository;
	@Autowired
	RestClient restClient;


	public FeriadoService(FeriadoRepository feriadoRepository, RestClient.Builder restClientBuilder) {

		this.feriadoRepository = feriadoRepository;

		this.restClient = restClientBuilder.baseUrl("https://brasilapi.com.br").build();
	}

	public void sincronizarAno(int ano) {

		List<FeriadoApiDTO> feriadosApi = restClient.get().uri("/api/feriados/v1/{ano}", ano).retrieve()
				.body(new ParameterizedTypeReference<List<FeriadoApiDTO>>() {
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
