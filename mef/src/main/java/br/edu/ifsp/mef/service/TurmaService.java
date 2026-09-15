package br.edu.ifsp.mef.service;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.repository.UsuarioRepository;

@Service
public class TurmaService {

	@Autowired
	TurmaRepository turmaRepository;
	@Autowired
	UsuarioRepository usuarioRepository;
	@Autowired
	ConviteService conviteService;

	public Turma criarTurma(String nome) {
		Turma turma = new Turma();
		turma.setNome(nome);
		return turmaRepository.save(turma);
	}

	public Turma buscarPorId(Long id) {
		return turmaRepository.findById(id).orElseThrow(() -> new RuntimeException("Turma não encontrada."));
	}

	public List<Turma> listarTurmas() {
		return turmaRepository.findAll();
	}

	public Turma atualizarTurma(Long id, String nome) {
		Turma turma = buscarPorId(id);
		turma.setNome(nome);
		return turmaRepository.save(turma);
	}

	@Transactional
	public void excluirTurma(Long id) {

		Turma turma = buscarPorId(id);
		for (Usuario usuario : turma.getUsuarios()) {
			usuario.getTurmas().remove(turma);
		}
		turma.getUsuarios().clear();
		turmaRepository.delete(turma);
	}

	@Transactional
	public void adicionarUsuario(Long idTurma, Long idUsuario) {
		Turma turma = buscarPorId(idTurma);
		Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
		turma.addUsuario(usuario);
		turmaRepository.save(turma);
	}

	@Transactional
	public void removerUsuario(Long idTurma, Long idUsuario) {
		Turma turma = buscarPorId(idTurma);
		Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
		turma.removeUsuario(usuario);
		turmaRepository.save(turma);
	}

	public Set<Usuario> listarUsuarios(Long idTurma) {

		Turma turma = buscarPorId(idTurma);

		return turma.getUsuarios();
	}
	
	public void gerarCodigoAcesso(Long idTurma) {
		conviteService.criarConvite(idTurma);
	}
}
