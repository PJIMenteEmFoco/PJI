package br.edu.ifsp.mef.service;

import br.edu.ifsp.mef.model.Direcao;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MontadorCruzadinha {
    private MontadorCruzadinha() {}
    public record Entrada(String palavra, String dica) {}
    public record Posicao(String palavra, String dica, int linha, int coluna, Direcao direcao) {}
    public record Resultado(int linhas, int colunas, List<Posicao> palavras) {}
    private record Coordenada(int linha, int coluna) {}
    private record Celula(char letra, Set<Direcao> direcoes) {}
    private record Candidata(int linha, int coluna, Direcao direcao, int pontuacao) {}
    private static final class Solucao {
        final Map<Coordenada, Celula> grade = new LinkedHashMap<>();
        final List<Posicao> posicionadas = new ArrayList<>();
        final Set<Integer> indices = new HashSet<>();
        void colocar(int indice, Entrada entrada, int linha, int coluna, Direcao direcao) {
            posicionadas.add(new Posicao(entrada.palavra(), entrada.dica(), linha, coluna, direcao));
            indices.add(indice);
            for (int n = 0; n < entrada.palavra().length(); n++) {
                Coordenada c = coord(linha, coluna, direcao, n);
                Celula anterior = grade.get(c);
                Set<Direcao> direcoes = new HashSet<>(anterior == null ? Set.of() : anterior.direcoes());
                direcoes.add(direcao);
                grade.put(c, new Celula(entrada.palavra().charAt(n), direcoes));
            }
        }
        boolean cabe(String palavra, int linha, int coluna, Direcao direcao) {
            int dr = direcao == Direcao.VERTICAL ? 1 : 0;
            int dc = direcao == Direcao.HORIZONTAL ? 1 : 0;
            if (grade.containsKey(new Coordenada(linha - dr, coluna - dc)) ||
                grade.containsKey(new Coordenada(linha + dr * palavra.length(), coluna + dc * palavra.length()))) return false;
            int cruzamentos = 0;
            for (int i = 0; i < palavra.length(); i++) {
                Coordenada c = coord(linha, coluna, direcao, i);
                Celula existente = grade.get(c);
                if (existente != null) {
                    if (existente.letra() != palavra.charAt(i) || existente.direcoes().contains(direcao)) return false;
                    cruzamentos++;
                } else {
                    if (grade.containsKey(new Coordenada(c.linha() + dc, c.coluna() + dr)) ||
                        grade.containsKey(new Coordenada(c.linha() - dc, c.coluna() - dr))) return false;
                }
            }
            return cruzamentos > 0;
        }
    }
    private static Coordenada coord(int r, int c, Direcao d, int i) {
        return new Coordenada(r + (d == Direcao.VERTICAL ? i : 0), c + (d == Direcao.HORIZONTAL ? i : 0));
    }
    public static Resultado montar(List<Entrada> entradas) {
        if (entradas == null || entradas.isEmpty()) throw new IllegalArgumentException("Adicione palavras à cruzadinha.");
        List<Integer> ordem = new ArrayList<>();
        for (int i = 0; i < entradas.size(); i++) ordem.add(i);
        ordem.sort(Comparator.comparingInt((Integer i) -> entradas.get(i).palavra().length()).reversed());
        Solucao melhor = null;
        for (int semente : ordem) {
            Solucao solucao = new Solucao();
            solucao.colocar(semente, entradas.get(semente), 0, 0, Direcao.HORIZONTAL);
            boolean progrediu = true;
            while (progrediu && solucao.indices.size() < entradas.size()) {
                progrediu = false;
                for (int i : ordem) {
                    if (solucao.indices.contains(i)) continue;
                    Entrada item = entradas.get(i);
                    Candidata melhorCandidata = null;
                    for (Map.Entry<Coordenada, Celula> cel : solucao.grade.entrySet()) {
                        for (int j = 0; j < item.palavra().length(); j++) {
                            if (item.palavra().charAt(j) != cel.getValue().letra()) continue;
                            for (Direcao dir : Direcao.values()) {
                                int r = cel.getKey().linha() - (dir == Direcao.VERTICAL ? j : 0);
                                int c = cel.getKey().coluna() - (dir == Direcao.HORIZONTAL ? j : 0);
                                if (!solucao.cabe(item.palavra(), r, c, dir)) continue;
                                int custo = Math.abs(r) + Math.abs(c);
                                if (melhorCandidata == null || custo < melhorCandidata.pontuacao())
                                    melhorCandidata = new Candidata(r, c, dir, custo);
                            }
                        }
                    }
                    if (melhorCandidata != null) {
                        solucao.colocar(i, item, melhorCandidata.linha(), melhorCandidata.coluna(), melhorCandidata.direcao());
                        progrediu = true;
                    }
                }
            }
            if (melhor == null || solucao.posicionadas.size() > melhor.posicionadas.size()) melhor = solucao;
            if (solucao.posicionadas.size() == entradas.size()) break;
        }
        if (melhor == null || melhor.posicionadas.size() != entradas.size()) {
            throw new IllegalArgumentException("Não foi possível cruzar todas as palavras. Escolha palavras que compartilhem letras e permitam cruzamentos.");
        }
        int minR = melhor.grade.keySet().stream().mapToInt(Coordenada::linha).min().orElse(0);
        int minC = melhor.grade.keySet().stream().mapToInt(Coordenada::coluna).min().orElse(0);
        int maxR = melhor.grade.keySet().stream().mapToInt(Coordenada::linha).max().orElse(0);
        int maxC = melhor.grade.keySet().stream().mapToInt(Coordenada::coluna).max().orElse(0);
        List<Posicao> ajustadas = melhor.posicionadas.stream().map(p ->
            new Posicao(p.palavra(), p.dica(), p.linha() - minR, p.coluna() - minC, p.direcao())).toList();
        return new Resultado(maxR - minR + 1, maxC - minC + 1, ajustadas);
    }
}
