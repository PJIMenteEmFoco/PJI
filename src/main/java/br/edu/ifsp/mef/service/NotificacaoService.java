package br.edu.ifsp.mef.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.ConfiguracoesUsuario;
import br.edu.ifsp.mef.model.Notificacao;
import br.edu.ifsp.mef.model.Psicopedagogo;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.ConfiguracoesUsuarioRepository;
import br.edu.ifsp.mef.repository.NotificacaoRepository;

@Service
public class NotificacaoService {
	private final NotificacaoRepository notificacoes;
	private final ConfiguracoesUsuarioRepository configuracoes;
	private final UsuarioDetailsService usuarios;

	public NotificacaoService(NotificacaoRepository notificacoes, ConfiguracoesUsuarioRepository configuracoes,
			UsuarioDetailsService usuarios) {
		this.notificacoes = notificacoes;
		this.configuracoes = configuracoes;
		this.usuarios = usuarios;
	}

	@Transactional
	public List<Notificacao> listar(Long usuarioId) {
		return notificacoes.findAllByDestinatario_IdOrderByDataCriacaoDesc(usuarioId);
	}

	@Transactional
	public boolean receberNotificacoes(Usuario usuario) {
		return configuracoes.findById(usuario.getId()).map(ConfiguracoesUsuario::isNotificacao).orElse(true);
	}

	@Transactional
	public boolean atualizarPreferencia(Long usuarioId, boolean receber) {
		Usuario usuario = usuarios.getById(usuarioId);
		if (usuario == null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
		ConfiguracoesUsuario config = configuracoes.findById(usuarioId).orElseGet(ConfiguracoesUsuario::new);
		config.setId(usuarioId);
		config.setUsuario(usuario);
		config.setNotificacao(receber);
		configuracoes.save(config);
		return receber;
	}

	@Transactional
	public void marcarComoLida(Long notificacaoId, Long usuarioId) {
		Notificacao notificacao = notificacoes.findByIdAndDestinatario_Id(notificacaoId, usuarioId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada."));
		notificacao.setLida(true);
		notificacoes.save(notificacao);
	}

	@Transactional
	public int marcarTodasComoLidas(Long usuarioId) {
		List<Notificacao> lista = notificacoes.findAllByDestinatario_IdOrderByDataCriacaoDesc(usuarioId);
		int alteradas = 0;
		for (Notificacao notificacao : lista) {
			if (!notificacao.isLida()) {
				notificacao.setLida(true);
				alteradas++;
			}
		}
		notificacoes.saveAll(lista);
		return alteradas;
	}

	@Transactional
	public void notificarNovaAtividade(Atividade atividade) {
		if (atividade == null || atividade.getAlunos() == null)
			return;
		for (Aluno aluno : atividade.getAlunos()) {
			criarSeHabilitado(aluno, "Nova atividade", atividade.getNome(), "/aluno/tarefas");
		}
	}

	@Transactional
	public void notificarConclusaoAtividade(Atividade atividade, Aluno aluno) {
		if (atividade == null || aluno == null)
			return;
		criarSeHabilitado(aluno, "Atividade concluída", atividade.getNome(), "/aluno/tarefas");

		Set<Usuario> destinatarios = new LinkedHashSet<>();
		if (atividade.getProfessor() != null)
			destinatarios.add(atividade.getProfessor());
		if (aluno.getTurmas() != null) {
			aluno.getTurmas().forEach(turma -> {
				if (turma.getUsuarios() == null)
					return;
				turma.getUsuarios().stream().filter(usuario -> usuario instanceof Psicopedagogo
						|| "PSICOPEDAGOGO".equalsIgnoreCase(usuario.getPerfil())).forEach(destinatarios::add);
			});
		}
		String mensagem = aluno.getNome() + " concluiu " + atividade.getNome() + ".";
		String linkRelatorios = "/professor/relatorios?aluno=" + aluno.getId();
		for (Usuario destinatario : destinatarios) {
			criarSeHabilitado(destinatario, "Novo resultado de atividade", mensagem, linkRelatorios);
		}
	}

	private void criarSeHabilitado(Usuario destinatario, String titulo, String mensagem, String link) {
		if (destinatario == null || destinatario.getId() == null || !receberNotificacoes(destinatario))
			return;
		Notificacao notificacao = new Notificacao();
		notificacao.setDestinatario(destinatario);
		notificacao.setTitulo(limitar(titulo, 180));
		notificacao.setMensagem(limitar(mensagem, 1600));
		notificacao.setLink(linkInterno(link));
		notificacao.setLida(false);
		notificacoes.save(notificacao);
	}

	private String linkInterno(String link) {
		if (link == null || !link.startsWith("/") || link.startsWith("//") || link.startsWith("/\\")) {
			return "/professor/dashboard";
		}
		return limitar(link, 600);
	}

	private static String limitar(String valor, int maximo) {
		if (valor == null)
			return "";
		String normalizado = valor.trim();
		return normalizado.length() > maximo ? normalizado.substring(0, maximo) : normalizado;
	}
}
