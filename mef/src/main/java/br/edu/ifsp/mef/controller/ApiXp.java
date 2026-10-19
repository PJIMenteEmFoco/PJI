package br.edu.ifsp.mef.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.mef.service.AlunoService;

@RestController
@RequestMapping("/apixp")
public class ApiXp {

	@Autowired
	AlunoService alunoService;
	
	public void adicionarXp(@RequestParam Long id, @RequestParam double xp) {
		alunoService.receberXp(id, xp);
	}
}
