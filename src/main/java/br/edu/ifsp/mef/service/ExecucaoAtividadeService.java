package br.edu.ifsp.mef.service;

import br.edu.ifsp.mef.model.*;
import br.edu.ifsp.mef.repository.AtividadeRepository;
import br.edu.ifsp.mef.repository.TentativaAtividadeRepository;
import java.time.LocalDate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.text.Normalizer;

@Service
public class ExecucaoAtividadeService {
    public record RespostaPalavra(Long id, String texto) {}
    public record Respostas(List<Integer> alternativas, List<RespostaPalavra> palavras,
            List<Long> paresEncontrados, Integer segundos) {}

    private final AtividadeRepository atividades;
    private final UsuarioDetailsService usuarios;
    private final EntityManager em;
    private final TentativaAtividadeRepository tentativas;
    private final AlunoService alunoService;
    public ExecucaoAtividadeService(AtividadeRepository atividades,
            UsuarioDetailsService usuarios, EntityManager em,
            TentativaAtividadeRepository tentativas, AlunoService alunoService) {
        this.atividades = atividades;
        this.usuarios = usuarios;
        this.em = em;
        this.tentativas = tentativas;
        this.alunoService = alunoService;
    }

    private record Acesso(Aluno aluno, Atividade atividade) {}
    private Acesso autorizar(Long id, String email) {
        Usuario usuario = usuarios.getByEmail(email);
        if (!(usuario instanceof Aluno aluno)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo aos alunos.");
        Atividade a = atividades.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atividade não encontrada."));
        if (a.getStatus() == StatusAtividade.NAO_VINCULADO || a.getTipoAtividade() == TipoAtividade.LEITURA_GUIADA)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Atividade indisponível para execução.");
        if (a.getAlunos().stream().noneMatch(v -> v.getId().equals(aluno.getId())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Atividade não atribuída a este aluno.");
        return new Acesso(aluno, a);
    }

    /** Lista apenas as atividades publicadas e atribuídas ao aluno autenticado. */
    @Transactional(readOnly = true)
    public List<Map<String,Object>> minhasAtividades(String email) {
        Usuario usuario = usuarios.getByEmail(email);
        if (!(usuario instanceof Aluno aluno))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo aos alunos.");
        List<Map<String,Object>> saida = new ArrayList<>();
        for (Atividade a : atividades.findAtividadesDoAluno(aluno.getId())) {
            if (a.getStatus() == StatusAtividade.NAO_VINCULADO || a.getTipoAtividade() == TipoAtividade.LEITURA_GUIADA) continue;
            var ultimaTentativa = tentativas.findFirstByAtividade_IdAndAluno_IdOrderByFinalizadaEmDescIdDesc(a.getId(), aluno.getId());
            boolean concluida = ultimaTentativa.isPresent();
            boolean atrasada = !concluida && a.getPrazo() != null && a.getPrazo().isBefore(LocalDate.now());
            Map<String,Object> item = new LinkedHashMap<>();
            item.put("tentativaId", ultimaTentativa.map(t -> t.getId()).orElse(null));
            item.put("id", a.getId());item.put("titulo", a.getNome());item.put("tipo", a.getTipoAtividade().name());
            item.put("prazo", a.getPrazo());item.put("situacao", concluida ? "concluida" : atrasada ? "atrasada" : "pendente");
            saida.add(item);
        }
        return saida;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dados(Long id, String email) {
        Acesso acesso = autorizar(id, email);
        Atividade a = acesso.atividade();
        Map<String,Object> resultado = new LinkedHashMap<>();
        resultado.put("id", a.getId());
        resultado.put("titulo", a.getNome());
        resultado.put("instrucoes", a.getDescricao());
        resultado.put("prazo", a.getPrazo());
        switch (a.getTipoAtividade()) {
            case QUIZ -> {
                resultado.put("tipo", "quiz");
                Quiz quiz = em.find(Quiz.class, id);
                if (quiz == null) throw new IllegalArgumentException("Quiz não encontrado.");
                List<Map<String,Object>> perguntas = new ArrayList<>();
                for (Pergunta p : quiz.getPerguntas().stream()
                        .sorted(java.util.Comparator.comparing(Pergunta::getId)).toList()) {
                    perguntas.add(Map.of("id", p.getId(), "pergunta", p.getEnunciado(), "opcoes", opcoes(p)));
                }
                resultado.put("perguntas", perguntas); // Não inclui a alternativa correta.
            }
            case JOGO_DA_MEMORIA -> {
                resultado.put("tipo", "memoria");
                JogoMemoria jogo = em.find(JogoMemoria.class, id);
                if (jogo == null) throw new IllegalArgumentException("Jogo não encontrado.");
                List<Map<String,Object>> pares = new ArrayList<>();
                for (ParCarta p : jogo.getPares()) {
                    pares.add(Map.of("id", p.getId(), "termo", p.getCartaA(), "significado", p.getCartaB()));
                }
                resultado.put("pares", pares);
            }
            case CRUZADINHA -> {
                resultado.put("tipo", "cruzadas");
                Cruzadinha c = em.find(Cruzadinha.class, id);
                if (c == null) throw new IllegalArgumentException("Cruzadinha não encontrada.");
                resultado.put("linhas", c.getLinhas());
                resultado.put("colunas", c.getColunas());
                List<Map<String,Object>> itens = new ArrayList<>();
                for (PalavraCruzada p : c.getPalavraCruzada()) {
                    itens.add(Map.of("id", p.getId(), "dica", p.getDica(), "linha", p.getLinha(),
                          "coluna", p.getColuna(), "direcao", p.getDirecao().name(), "tamanho", p.getResposta().length()));
                }
                resultado.put("palavras", itens); // Não envia letras/respostas ao aluno.
            }
            default -> throw new IllegalArgumentException("Tipo de atividade ainda não suportado.");
        }
        return resultado;
    }


    private Aluno bloquearAlunoPorEmail(String email) {
        List<Aluno> encontrados = em.createQuery(
                "SELECT a FROM Aluno a WHERE LOWER(a.email) = LOWER(:email)", Aluno.class)
                .setParameter("email", email)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setMaxResults(1)
                .getResultList();
        if (encontrados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo aos alunos.");
        }
        return encontrados.get(0);
    }

    @Transactional
    public Map<String,Object> concluir(Long id, String email, Respostas respostas) {
        // Bloqueia o registro do aluno antes das leituras normais da transação. Assim, duas requisições
        // simultâneas do mesmo aluno não conseguem conceder a recompensa duas vezes para a mesma atividade.
        Aluno alunoBloqueado = bloquearAlunoPorEmail(email);
        Acesso acesso = autorizar(id,email);
        if (!alunoBloqueado.getId().equals(acesso.aluno().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Aluno autenticado inválido.");
        }
        Atividade a = acesso.atividade();
        if (respostas == null) throw new IllegalArgumentException("Envie as respostas.");
        int total, acertos = 0;
        switch (a.getTipoAtividade()) {
            case QUIZ -> {
                Quiz quiz = em.find(Quiz.class,id);
                List<Pergunta> perguntas = quiz.getPerguntas().stream()
                        .sorted(java.util.Comparator.comparing(Pergunta::getId)).toList();
                total = perguntas.size();
                if (total == 0 || respostas.alternativas() == null || respostas.alternativas().size() != total)
                    throw new IllegalArgumentException("Responda todas as perguntas.");
                for (int i = 0; i < total; i++) {
                    Integer r = respostas.alternativas().get(i);
                    if (r == null || r < 0 || r >= opcoes(perguntas.get(i)).size()) throw new IllegalArgumentException("Resposta inválida.");
                    if (r + 1 == perguntas.get(i).getAlternativaCorreta()) acertos++;
                }
            }
            case JOGO_DA_MEMORIA -> {
                JogoMemoria jogo = em.find(JogoMemoria.class,id);
                Set<Long> esperados = new HashSet<>();
                for (ParCarta p : jogo.getPares()) esperados.add(p.getId());
                List<Long> enviados = respostas.paresEncontrados();
                if (enviados == null || enviados.size() != esperados.size() ||
                    new HashSet<>(enviados).size() != enviados.size() || !esperados.equals(new HashSet<>(enviados)))
                    throw new IllegalArgumentException("Conclua todos os pares antes de enviar.");
                total = esperados.size(); acertos = total;
            }
            case CRUZADINHA -> {
                Cruzadinha cruz = em.find(Cruzadinha.class,id);
                List<PalavraCruzada> palavras = cruz.getPalavraCruzada();
                total = palavras.size();
                List<RespostaPalavra> enviadas = respostas.palavras();
                if (enviadas == null || enviadas.size() != total) throw new IllegalArgumentException("Preencha todas as palavras.");
                Map<Long,String> mapa = new HashMap<>();
                for (RespostaPalavra r : enviadas) {
                    if (r == null || r.id() == null || mapa.put(r.id(), normalizar(r.texto())) != null)
                        throw new IllegalArgumentException("Palavras duplicadas ou inválidas.");
                }
                for (PalavraCruzada p : palavras) {
                    if (!mapa.containsKey(p.getId())) throw new IllegalArgumentException("Faltam palavras.");
                    if (normalizar(p.getResposta()).equals(mapa.get(p.getId()))) acertos++;
                }
            }
            default -> throw new IllegalArgumentException("Tipo indisponível.");
        }
        // Verifica o histórico somente depois de obter o bloqueio do aluno.
        // A primeira tentativa dessa atividade recebe os 50 XP; as seguintes recebem zero.
        boolean primeiraConclusao = !tentativas.existsByAtividade_IdAndAluno_Id(a.getId(), alunoBloqueado.getId());
        int xpRecebido = primeiraConclusao ? 50 : 0;

        TentativaAtividade tentativa = new TentativaAtividade();
        tentativa.setAtividade(a);
        tentativa.setAluno(alunoBloqueado);
        tentativa.setAcertos(acertos);
        tentativa.setTotal(total);
        tentativa.setSegundos(respostas.segundos() == null ? 0 : Math.max(0, Math.min(86400, respostas.segundos())));
        tentativa.setXpRecebido(xpRecebido);
        em.persist(tentativa);
        em.flush();

        boolean subiuNivel = false;
        if (primeiraConclusao) {
            // A soma de XP e a tentativa são parte da mesma transação: se uma falhar, ambas são revertidas.
            subiuNivel = alunoService.receberXp(alunoBloqueado.getId(), 50.0);
            em.flush();
        }
        double xpTotal = alunoBloqueado.getXp() == null ? 0.0 : alunoBloqueado.getXp();
        Map<String,Object> resultado = new LinkedHashMap<>();
        resultado.put("sucesso", true);
        resultado.put("tentativaId", tentativa.getId());
        resultado.put("acertos", acertos);
        resultado.put("total", total);
        resultado.put("percentual", total == 0 ? 0 : (acertos * 100 / total));
        resultado.put("xpRecebido", xpRecebido);
        resultado.put("xpTotal", xpTotal);
        resultado.put("subiuNivel", subiuNivel);
        resultado.put("mensagem", primeiraConclusao
                ? "Atividade concluída! Você ganhou 50 XP pela primeira conclusão."
                : "Nova tentativa registrada. Os XP desta atividade já foram concedidos na primeira conclusão.");
        return resultado;
    }

    private static List<String> opcoes(Pergunta p) {
        List<String> a = new ArrayList<>();
        for (String s : new String[]{p.getAlternativa1(), p.getAlternativa2(), p.getAlternativa3(), p.getAlternativa4(), p.getAlternativa5()}) {
            if (s != null && !s.isBlank()) a.add(s);
        }
        return a;
    }
    private static String normalizar(String s) {
        return Normalizer.normalize(s == null ? "" : s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT);
    }
}
