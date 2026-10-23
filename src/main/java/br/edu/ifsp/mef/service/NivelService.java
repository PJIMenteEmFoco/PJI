package br.edu.ifsp.mef.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.edu.ifsp.mef.model.Nivel;
import br.edu.ifsp.mef.repository.NivelRepository;

@Service
public class NivelService {

	@Autowired
	NivelRepository nivelRepository;
	
	public Nivel getById(long id) {
		Optional<Nivel> byId = nivelRepository.findById(id);
		if (byId.isEmpty()) {
			return null;
		}
		return byId.get();
	}
}
