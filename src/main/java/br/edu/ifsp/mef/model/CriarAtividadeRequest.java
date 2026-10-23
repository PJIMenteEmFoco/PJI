package br.edu.ifsp.mef.model;

import java.time.LocalDate;
import java.util.List;

public record CriarAtividadeRequest(
        String titulo,
        String instrucoes,
        LocalDate prazo,
        String tipo,
        List<Long> alunos,
        List<PerguntaInput> perguntas,
        List<ParInput> pares,
        List<PalavraInput> palavras,
        List<String> trechos,
        String status) {

    public record PerguntaInput(String pergunta, List<String> opcoes, Integer correta) {}
    public record ParInput(String termo, String significado) {}
    public record PalavraInput(String palavra, String dica) {}
}
