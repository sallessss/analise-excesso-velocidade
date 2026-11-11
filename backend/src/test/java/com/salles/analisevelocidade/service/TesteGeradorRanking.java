package com.salles.analisevelocidade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.salles.analisevelocidade.domain.EpisodioExcesso;
import com.salles.analisevelocidade.domain.Gravidade;
import com.salles.analisevelocidade.domain.PosicaoRanking;

class TesteGeradorRanking {

    private final GeradorRanking geradorRanking = new GeradorRanking();

    @Test
    void deveSomarPontosDosEpisodiosPorVeiculo() {
        List<PosicaoRanking> ranking = geradorRanking.gerar(List.of(
                episodio("ABC1D23", "80", Gravidade.LEVE),
                episodio("ABC1D23", "90", Gravidade.GRAVE),
                episodio("ABC1D23", "85", Gravidade.GRAVISSIMA)));

        assertEquals(1, ranking.size());
        assertEquals("ABC1D23", ranking.getFirst().placa());
        assertEquals(9, ranking.getFirst().totalPontos());
        assertEquals(new BigDecimal("90"), ranking.getFirst().maiorVelocidadeKmh());
    }

    @Test
    void deveOrdenarPorPontuacaoDecrescente() {
        List<PosicaoRanking> ranking = geradorRanking.gerar(List.of(
                episodio("ABC1D23", "100", Gravidade.LEVE),
                episodio("DEF2G34", "70", Gravidade.GRAVE)));

        assertEquals(List.of("DEF2G34", "ABC1D23"), extrairPlacas(ranking));
    }

    @Test
    void deveDesempatarPelaMaiorVelocidadeRegistradaNosEpisodios() {
        List<PosicaoRanking> ranking = geradorRanking.gerar(List.of(
                episodio("ABC1D23", "80", Gravidade.GRAVE),
                episodio("DEF2G34", "90", Gravidade.GRAVE)));

        assertEquals(List.of("DEF2G34", "ABC1D23"), extrairPlacas(ranking));
    }

    @Test
    void deveDesempatarPelaOrdemAlfabeticaDaPlaca() {
        List<PosicaoRanking> ranking = geradorRanking.gerar(List.of(
                episodio("DEF2G34", "80", Gravidade.GRAVE),
                episodio("ABC1D23", "80", Gravidade.GRAVE)));

        assertEquals(List.of("ABC1D23", "DEF2G34"), extrairPlacas(ranking));
    }

    @Test
    void naoDeveCriarPosicaoSemEpisodios() {
        assertTrue(geradorRanking.gerar(List.of()).isEmpty());
    }

    private EpisodioExcesso episodio(String placa, String velocidadeMaximaKmh, Gravidade gravidade) {
        LocalDateTime instante = LocalDateTime.parse("2026-10-05T08:00:00");
        return new EpisodioExcesso(
                placa,
                instante,
                instante,
                0,
                new BigDecimal(velocidadeMaximaKmh),
                BigDecimal.ZERO,
                gravidade);
    }

    private List<String> extrairPlacas(List<PosicaoRanking> ranking) {
        return ranking.stream().map(PosicaoRanking::placa).toList();
    }
}
