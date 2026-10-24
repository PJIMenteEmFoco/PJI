package br.edu.ifsp.mef.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Feriado;
import br.edu.ifsp.mef.model.TentativaAtividade;
import br.edu.ifsp.mef.model.TipoAtividade;
import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.AtividadeRepository;
import br.edu.ifsp.mef.repository.DashboardPsicopedagogoRepository;
import br.edu.ifsp.mef.repository.TentativaAtividadeRepository;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.service.EventoPsicopedagogoService;
import br.edu.ifsp.mef.service.FeriadoService;

@Controller
public class PsicopedagogoViewController {
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private DashboardPsicopedagogoRepository dashboardRepository;
    @Autowired private EventoPsicopedagogoService eventoPsicopedagogoService;
    @Autowired private FeriadoService feriadoService;
    @Autowired private AtividadeRepository atividadeRepository;
    @Autowired private TentativaAtividadeRepository tentativaAtividadeRepository;

    private Long usuarioId(Authentication auth) {
        return ((UsuarioDetails) auth.getPrincipal()).getId();
    }

    @GetMapping("/psicopedagogo/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Long psicopedagogoId = usuarioId(auth);
        List<Turma> turmas = turmaRepository.buscarTurmasPorMembro(psicopedagogoId);
        List<Usuario> alunos = turmaRepository.buscarAlunosDoMembro(psicopedagogoId);
        model.addAttribute("turmas", turmas);
        model.addAttribute("alunos", alunos);
        model.addAttribute("quantidadeAlunos", alunos.size());

        List<Long> alunoIds = alunos.stream()
                .map(Usuario::getId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        List<Object[]> atribuicoes = List.of();
        Set<String> paresConcluidos = new HashSet<>();
        if (!alunoIds.isEmpty()) {
            atribuicoes = dashboardRepository.buscarAtribuicoes(alunoIds);
            for (Object[] par : dashboardRepository.buscarParesConcluidos(alunoIds)) {
                paresConcluidos.add(chave(par[0], par[1]));
            }
        }

        long totalAtribuicoes = atribuicoes.size();
        long totalConcluidas = atribuicoes.stream()
                .filter(row -> paresConcluidos.contains(chave(row[0], row[3])))
                .count();
        String taxaConclusao = totalAtribuicoes == 0
                ? "—"
                : Math.round(totalConcluidas * 100.0 / totalAtribuicoes) + "%";
        model.addAttribute("taxaConclusaoJogos", taxaConclusao);

        List<Map<String, String>> alertasAtrasadas = construirAlertasAtrasadas(atribuicoes, paresConcluidos);
        List<Map<String, String>> alertasRecentes = new ArrayList<>();
        if (alertasAtrasadas.isEmpty() && !alunoIds.isEmpty()) {
            List<Object[]> recentes = dashboardRepository.buscarTentativaMaisRecente(alunoIds);
            if (!recentes.isEmpty()) {
                Object[] recente = recentes.get(0);
                String nomeAluno = String.valueOf(recente[0]);
                String nomeAtividade = String.valueOf(recente[1]);
                String data = formatarData(recente[2]);
                alertasRecentes.add(alerta(
                        "Atividade concluída recentemente",
                        nomeAluno + " concluiu “" + nomeAtividade + "”" + (data.isBlank() ? "." : " em " + data + ".")));
            }
        }
        model.addAttribute("alertasAtrasadas", alertasAtrasadas);
        model.addAttribute("alertasRecentes", alertasRecentes);

        // Não existe entidade/repositório de relatórios pendentes no backend analisado;
        // o template mostra um traço em vez de inventar uma contagem.
        model.addAttribute("relatoriosPendentesDisponiveis", false);
        return "psicopedagogo/dashboard_psico";
    }

    private List<Map<String, String>> construirAlertasAtrasadas(List<Object[]> atribuicoes, Set<String> paresConcluidos) {
        LocalDate hoje = LocalDate.now();
        List<Map<String, String>> alertas = new ArrayList<>();
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Object[] row : atribuicoes) {
            LocalDate prazo = (LocalDate) row[2];
            if (prazo == null || !prazo.isBefore(hoje) || paresConcluidos.contains(chave(row[0], row[3]))) {
                continue;
            }
            alertas.add(alerta(
                    "Atividade com prazo vencido",
                    String.valueOf(row[4]) + " ainda não concluiu “" + String.valueOf(row[1])
                            + "”, cujo prazo era " + prazo.format(formatador) + "."));
            if (alertas.size() == 3) break;
        }
        return alertas;
    }

    private Map<String, String> alerta(String titulo, String descricao) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("titulo", titulo);
        item.put("descricao", descricao);
        return item;
    }

    private String chave(Object atividadeId, Object alunoId) {
        return String.valueOf(atividadeId) + ":" + String.valueOf(alunoId);
    }

    private String formatarData(Object valor) {
        if (valor instanceof LocalDateTime data) {
            return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "";
    }

    @GetMapping("/psicopedagogo/turmas")
    public String turmas(Authentication auth, Model model) {
        List<Turma> turmas = turmaRepository.buscarTurmasPorMembro(usuarioId(auth));
        model.addAttribute("turmas", turmas);

        Map<Long, List<Usuario>> alunosPorTurma = new LinkedHashMap<>();
        Map<Long, List<Usuario>> psicopedagogosPorTurma = new LinkedHashMap<>();
        Map<Long, String> nomesPsicopedagogosPorTurma = new LinkedHashMap<>();
        Map<Long, String> idsPsicopedagogosPorTurma = new LinkedHashMap<>();
        Map<Long, Usuario> psicopedagogosUnicos = new LinkedHashMap<>();

        for (Turma turma : turmas) {
            List<Usuario> membros = turma.getUsuarios() == null
                    ? List.of()
                    : new ArrayList<>(turma.getUsuarios());

            List<Usuario> alunos = membros.stream()
                    .filter(usuario -> usuario.getPerfil() != null
                            && "ALUNO".equalsIgnoreCase(usuario.getPerfil()))
                    .sorted(Comparator.comparing(Usuario::getNome,
                            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList();
            List<Usuario> psicopedagogos = membros.stream()
                    .filter(usuario -> usuario.getPerfil() != null
                            && "PSICOPEDAGOGO".equalsIgnoreCase(usuario.getPerfil()))
                    .sorted(Comparator.comparing(Usuario::getNome,
                            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList();

            alunosPorTurma.put(turma.getId(), alunos);
            psicopedagogosPorTurma.put(turma.getId(), psicopedagogos);
            nomesPsicopedagogosPorTurma.put(turma.getId(), psicopedagogos.stream()
                    .map(Usuario::getNome)
                    .filter(nome -> nome != null && !nome.isBlank())
                    .collect(Collectors.joining(", ")));
            idsPsicopedagogosPorTurma.put(turma.getId(), psicopedagogos.stream()
                    .map(Usuario::getId)
                    .filter(id -> id != null)
                    .map(String::valueOf)
                    .collect(Collectors.joining("|")));

            for (Usuario psicopedagogo : psicopedagogos) {
                if (psicopedagogo.getId() != null) {
                    psicopedagogosUnicos.putIfAbsent(psicopedagogo.getId(), psicopedagogo);
                }
            }
        }

        List<Usuario> psicopedagogosFiltro = psicopedagogosUnicos.values().stream()
                .sorted(Comparator.comparing(Usuario::getNome,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        model.addAttribute("alunosPorTurma", alunosPorTurma);
        model.addAttribute("psicopedagogosPorTurma", psicopedagogosPorTurma);
        model.addAttribute("nomesPsicopedagogosPorTurma", nomesPsicopedagogosPorTurma);
        model.addAttribute("idsPsicopedagogosPorTurma", idsPsicopedagogosPorTurma);
        model.addAttribute("psicopedagogosFiltro", psicopedagogosFiltro);
        return "psicopedagogo/turmas_psico";
    }

    @GetMapping("/psicopedagogo/detalhes-turma-psico/{id}")
    @Transactional(readOnly = true)
    public String detalhesTurma(@PathVariable Long id, Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()
                || auth.getAuthorities().stream().noneMatch(a -> "ROLE_PSICOPEDAGOGO".equals(a.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas psicopedagogos podem consultar os detalhes desta turma.");
        }

        Long psicopedagogoId = usuarioId(auth);
        Turma turma = turmaRepository.buscarTurmasPorMembro(psicopedagogoId).stream()
                .filter(t -> t.getId() != null && t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Turma não encontrada nas turmas acompanhadas por este psicopedagogo."));

        List<Usuario> alunos = turmaRepository.buscarAlunosDaTurma(id);
        List<AlunoTurmaResumo> alunosResumo = alunos.stream()
                .map(this::criarResumoAluno)
                .sorted(Comparator.comparing(AlunoTurmaResumo::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        String professorTexto = turma.getProfessorResponsavel() != null
                ? textoSeguro(turma.getProfessorResponsavel().getNome(), "Não informado")
                : "Não informado";

        String psicopedagogosTexto = "Não informado";
        if (turma.getUsuarios() != null) {
            String nomes = turma.getUsuarios().stream()
                    .filter(membro -> membro.getPerfil() != null)
                    .filter(membro -> "PSICOPEDAGOGO".equalsIgnoreCase(membro.getPerfil()))
                    .map(Usuario::getNome)
                    .filter(nome -> nome != null && !nome.isBlank())
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.joining(", "));
            if (!nomes.isBlank()) psicopedagogosTexto = nomes;
        }

        model.addAttribute("turma", turma);
        model.addAttribute("alunos", alunos); // preservado para compatibilidade com outros usos do modelo
        model.addAttribute("alunosResumo", alunosResumo);
        model.addAttribute("quantidadeAlunos", alunosResumo.size());
        model.addAttribute("totalAtividades", atividadeRepository.countAtividadesDaTurma(id));
        model.addAttribute("professorTexto", professorTexto);
        model.addAttribute("psicopedagogosTexto", psicopedagogosTexto);
        return "psicopedagogo/detalhes-turma-psico";
    }

    private AlunoTurmaResumo criarResumoAluno(Usuario usuario) {
        String nome = textoSeguro(usuario.getNome(), "Aluno sem nome");
        String hiperfoco = "Não informado";
        String tempoFocoTexto = "Não informado";
        if (usuario instanceof Aluno aluno) {
            hiperfoco = textoSeguro(aluno.getHiperfoco(), "Não informado");
            tempoFocoTexto = formatarTempoFoco(aluno.getTempoFoco());
        }
        String inicial = nome.isBlank() ? "A" : nome.substring(0, 1).toUpperCase(Locale.forLanguageTag("pt-BR"));
        return new AlunoTurmaResumo(usuario.getId(), nome, usuario.getEmail(), inicial, hiperfoco, tempoFocoTexto);
    }

    private String formatarTempoFoco(float minutos) {
        if (!Float.isFinite(minutos) || minutos <= 0) return "Não informado";
        if (Math.abs(minutos - Math.round(minutos)) < 0.0001f) return Math.round(minutos) + " min";
        return String.format(Locale.forLanguageTag("pt-BR"), "%.1f min", minutos);
    }

    public static final class AlunoTurmaResumo {
        private final Long id;
        private final String nome;
        private final String email;
        private final String inicial;
        private final String hiperfocoTexto;
        private final String tempoFocoTexto;

        public AlunoTurmaResumo(Long id, String nome, String email, String inicial,
                String hiperfocoTexto, String tempoFocoTexto) {
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.inicial = inicial;
            this.hiperfocoTexto = hiperfocoTexto;
            this.tempoFocoTexto = tempoFocoTexto;
        }
        public Long getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }
        public String getInicial() { return inicial; }
        public String getHiperfocoTexto() { return hiperfocoTexto; }
        public String getTempoFocoTexto() { return tempoFocoTexto; }
    }    @GetMapping("/psicopedagogo/tarefas")
    @Transactional(readOnly = true)
    public String tarefas(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()
                || auth.getAuthorities().stream().noneMatch(authority -> "ROLE_PSICOPEDAGOGO".equals(authority.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas psicopedagogos podem acessar esta página.");
        }
        Long psicopedagogoId = usuarioId(auth);
        List<Turma> turmas = turmaRepository.buscarTurmasPorMembro(psicopedagogoId);
        List<Usuario> alunos = turmaRepository.buscarAlunosDoMembro(psicopedagogoId).stream()
                .filter(aluno -> aluno != null && aluno.getId() != null)
                .collect(Collectors.toList());

        Map<Long, Set<String>> turmasPorAluno = new LinkedHashMap<>();
        for (Turma turma : turmas) {
            if (turma.getId() == null) continue;
            for (Usuario aluno : turmaRepository.buscarAlunosDaTurma(turma.getId())) {
                if (aluno == null || aluno.getId() == null) continue;
                turmasPorAluno.computeIfAbsent(aluno.getId(), chave -> new LinkedHashSet<>());
                if (turma.getNome() != null && !turma.getNome().isBlank()) {
                    turmasPorAluno.get(aluno.getId()).add(turma.getNome());
                }
            }
        }

        List<Long> alunoIds = alunos.stream().map(Usuario::getId).distinct().collect(Collectors.toList());
        List<TentativaAtividade> tentativas = alunoIds.isEmpty()
                ? List.of()
                : tentativaAtividadeRepository.findByAluno_IdInOrderByFinalizadaEmDescIdDesc(alunoIds);
        Map<String, TentativaAtividade> tentativaMaisRecentePorPar = new HashMap<>();
        for (TentativaAtividade tentativa : tentativas) {
            if (tentativa == null || tentativa.getAtividade() == null || tentativa.getAtividade().getId() == null
                    || tentativa.getAluno() == null || tentativa.getAluno().getId() == null) {
                continue;
            }
            tentativaMaisRecentePorPar.putIfAbsent(chaveTarefa(tentativa.getAtividade().getId(), tentativa.getAluno().getId()), tentativa);
        }

        List<Map<String, Object>> tarefas = new ArrayList<>();
        Set<String> atribuicoesIncluidas = new HashSet<>();
        YearMonth mesAtual = YearMonth.now();
        long totalSegundosNoMes = 0;
        int tentativasNoMes = 0;
        int concluidasEsteMes = 0;

        for (Usuario aluno : alunos) {
            List<Atividade> atividadesDoAluno = atividadeRepository.findAtividadesDoAluno(aluno.getId());
            for (Atividade atividade : atividadesDoAluno) {
                if (atividade == null || atividade.getId() == null) continue;
                String chave = chaveTarefa(atividade.getId(), aluno.getId());
                if (!atribuicoesIncluidas.add(chave)) continue;

                TentativaAtividade tentativa = tentativaMaisRecentePorPar.get(chave);
                boolean concluida = tentativa != null;
                boolean pendenteLeitura = atividade.getTipoAtividade() == TipoAtividade.LEITURA_GUIADA && !concluida;
                boolean atrasada = !concluida && atividade.getPrazo() != null
                        && atividade.getPrazo().isBefore(LocalDate.now());

                Map<String, Object> card = new LinkedHashMap<>();
                card.put("atividadeId", atividade.getId());
                card.put("alunoId", aluno.getId());
                card.put("titulo", textoSeguro(atividade.getNome(), "Atividade sem título"));
                card.put("alunoNome", textoSeguro(aluno.getNome(), "Aluno"));
                Set<String> nomesTurmas = turmasPorAluno.getOrDefault(aluno.getId(), Set.of());
                card.put("turmaNome", nomesTurmas.isEmpty() ? "Turma não identificada" : String.join(", ", nomesTurmas));
                card.put("tipoCodigo", atividade.getTipoAtividade() == null ? "" : atividade.getTipoAtividade().name());
                card.put("tipoLabel", rotuloTipoTarefa(atividade.getTipoAtividade()));
                card.put("tipoClasse", atividade.getTipoAtividade() == TipoAtividade.LEITURA_GUIADA ? "comic" : "memory");
                card.put("concluida", concluida);
                card.put("pendenteLeitura", pendenteLeitura);
                card.put("atrasada", atrasada);
                card.put("statusTexto", concluida ? "Concluída" : atrasada ? "Prazo vencido" : "Pendente");
                card.put("resultadoTexto", textoResultado(tentativa));
                card.put("tempoTexto", tentativa == null ? "Tempo ainda não registrado" : "Tempo de execução: " + formatarTempo(tentativa.getSegundos()));
                card.put("prazoTexto", atividade.getPrazo() == null ? "Sem prazo" : "Prazo: " + atividade.getPrazo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                tarefas.add(card);

                if (tentativa != null && tentativa.getFinalizadaEm() != null
                        && YearMonth.from(tentativa.getFinalizadaEm()).equals(mesAtual)) {
                    concluidasEsteMes++;
                    totalSegundosNoMes += Math.max(0, tentativa.getSegundos());
                    tentativasNoMes++;
                }
            }
        }

        model.addAttribute("tarefasPsico", tarefas);
        model.addAttribute("concluidasEsteMes", concluidasEsteMes);
        model.addAttribute("tempoMedioAtencaoTexto", tentativasNoMes == 0
                ? "—" : formatarMediaAtencao(totalSegundosNoMes / (double) tentativasNoMes));
        // Não há critério configurado para inferir uma necessidade de readequação de tempo.
        model.addAttribute("alunosReadequacaoTexto", "—");
        model.addAttribute("readequacaoTempoDescricao", "Indicador ainda não definido no sistema");
        model.addAttribute("quantidadeAtribuicoes", tarefas.size());
        return "psicopedagogo/tarefas_psico";
    }

    private String chaveTarefa(Long atividadeId, Long alunoId) {
        return atividadeId + ":" + alunoId;
    }

    private String textoSeguro(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private String rotuloTipoTarefa(TipoAtividade tipo) {
        if (tipo == null) return "Tipo não informado";
        return switch (tipo) {
            case JOGO_DA_MEMORIA -> "Jogo da Memória";
            case QUIZ -> "Quiz";
            case CRUZADINHA -> "Cruzadinha";
            case LEITURA_GUIADA -> "Leitura Guiada";
        };
    }

    private String textoResultado(TentativaAtividade tentativa) {
        if (tentativa == null) return "Sem tentativa registrada";
        if (tentativa.getTotal() <= 0) return "Tentativa registrada";
        long percentual = Math.round(tentativa.getAcertos() * 100.0 / tentativa.getTotal());
        return "Taxa de Acertos: " + percentual + "%";
    }

    private String formatarTempo(int segundos) {
        int seguro = Math.max(0, segundos);
        return String.format("%02d:%02d", seguro / 60, seguro % 60);
    }

    private String formatarMediaAtencao(double segundos) {
        long arredondado = Math.round(Math.max(0, segundos));
        if (arredondado < 60) return "Menos de 1 min";
        return Math.round(arredondado / 60.0) + " min";
    }

    @GetMapping("/psicopedagogo/calendario")
    public String calendario(Authentication auth, Model model) {
        Long psicopedagogoId = usuarioId(auth);
        model.addAttribute("eventos", eventoPsicopedagogoService.listarDoPsicopedagogo(psicopedagogoId));

        // Mantém os feriados nacionais no calendário. Se a API externa estiver indisponível,
        // os feriados já salvos no banco continuam sendo exibidos e a página permanece acessível.
        int anoAtual = LocalDate.now().getYear();
        List<Feriado> feriados = new ArrayList<>();
        for (int ano = anoAtual - 1; ano <= anoAtual + 1; ano++) {
            try {
                feriados.addAll(feriadoService.buscarOuSincronizarAno(ano));
            } catch (RuntimeException ex) {
                feriados.addAll(feriadoService.buscarEntre(LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31)));
            }
        }
        model.addAttribute("feriados", feriados);
        // O backend não dispõe de uma fonte persistida para calcular relatórios a emitir.
        model.addAttribute("relatoriosPendentesDisponiveis", false);
        return "psicopedagogo/calendario_psico";
    }

    @GetMapping("/psicopedagogo/chat")
    public String chat(Authentication auth, Model model) {
        UsuarioDetails usuario = (UsuarioDetails) auth.getPrincipal();
        model.addAttribute("usuarioLogadoId", usuario.getId());
        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("alunos", turmaRepository.buscarAlunosDoMembro(usuario.getId()));
        return "psicopedagogo/chat_psico";
    }

    @GetMapping("/psicopedagogo/configuracoes")
    public String configuracoes() { return "psicopedagogo/configuracoes_psico"; }
}
