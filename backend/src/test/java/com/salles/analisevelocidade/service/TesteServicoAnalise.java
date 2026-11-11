package com.salles.analisevelocidade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.salles.analisevelocidade.dto.EpisodioDto;
import com.salles.analisevelocidade.dto.PosicaoRankingDto;
import com.salles.analisevelocidade.dto.RespostaAnaliseDto;
import com.salles.analisevelocidade.dto.ResumoDto;
import com.salles.analisevelocidade.parser.LeitorCsv;

class TesteServicoAnalise {

    private RespostaAnaliseDto resposta;

    @BeforeEach
    void analisarArquivoReferencia() throws Exception {
        ServicoAnalise servicoAnalise = new ServicoAnalise(
                new LeitorCsv(),
                new IdentificadorEpisodios(),
                new GeradorRanking());

        try (var fluxoEntrada = Files.newInputStream(localizarArquivoReferencia())) {
            resposta = servicoAnalise.analisar(fluxoEntrada);
        }
    }

    @Test
    void deveGerarResumoCompletoDoArquivoReferencia() {
        assertEquals(new ResumoDto(43, 35, 12), resposta.resumo());
        assertEquals(8, resposta.linhasInvalidas().size());
        assertEquals(
                List.of(4, 9, 12, 21, 22, 27, 38, 41),
                resposta.linhasInvalidas().stream().map(linha -> linha.numeroLinha()).toList());
    }

    @Test
    void deveGerarRankingCompletoNaOrdemEsperada() {
        assertEquals(
                List.of(
                        posicao("GHI3J45", 6, "100"),
                        posicao("ABC1D23", 6, "80"),
                        posicao("DEF2G34", 6, "78"),
                        posicao("YZA9B01", 5, "95"),
                        posicao("MNO5P67", 4, "72.5"),
                        posicao("STU7V89", 3, "90"),
                        posicao("VWX8Y90", 3, "74"),
                        posicao("JKL4M56", 1, "70")),
                resposta.ranking());
        assertFalse(resposta.ranking().stream().anyMatch(posicao -> posicao.placa().equals("PQR6S78")));
    }

    @Test
    void devePreservarCasosExtremosNosEpisodiosDoArquivoReferencia() {
        EpisodioDto episodioViradaDia = localizarEpisodio(
                "STU7V89",
                LocalDateTime.parse("2026-10-05T23:58:00"));
        assertEquals(LocalDateTime.parse("2026-10-06T00:01:00"), episodioViradaDia.fim());
        assertEquals(180, episodioViradaDia.duracaoSegundos());
        assertEquals(new BigDecimal("50.0"), episodioViradaDia.excessoMaximoPercentual());
        assertEquals("GRAVE", episodioViradaDia.gravidade());

        EpisodioDto episodioMaiorPercentual = localizarEpisodio(
                "ABC1D23",
                LocalDateTime.parse("2026-10-05T08:00:00"));
        assertEquals(new BigDecimal("80"), episodioMaiorPercentual.velocidadeMaximaKmh());
        assertEquals(new BigDecimal("55.0"), episodioMaiorPercentual.excessoMaximoPercentual());
        assertEquals("GRAVISSIMA", episodioMaiorPercentual.gravidade());
    }

    private PosicaoRankingDto posicao(String placa, int totalPontos, String maiorVelocidadeKmh) {
        return new PosicaoRankingDto(placa, totalPontos, new BigDecimal(maiorVelocidadeKmh));
    }

    private EpisodioDto localizarEpisodio(String placa, LocalDateTime inicio) {
        return resposta.episodios().stream()
                .filter(episodio -> episodio.placa().equals(placa) && episodio.inicio().equals(inicio))
                .findFirst()
                .orElseThrow();
    }

    private Path localizarArquivoReferencia() throws Exception {
        Path diretorioClassesTeste = Path.of(
                TesteServicoAnalise.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        return diretorioClassesTeste
                .getParent()
                .getParent()
                .getParent()
                .resolve("leituras.csv");
    }
}
