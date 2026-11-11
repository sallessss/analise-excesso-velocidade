package com.salles.analisevelocidade.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.salles.analisevelocidade.exception.TratadorGlobalExcecoes;
import com.salles.analisevelocidade.parser.LeitorCsv;
import com.salles.analisevelocidade.service.GeradorRanking;
import com.salles.analisevelocidade.service.IdentificadorEpisodios;
import com.salles.analisevelocidade.service.ServicoAnalise;

class TesteControladorAnalise {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        ServicoAnalise servicoAnalise = new ServicoAnalise(
                new LeitorCsv(),
                new IdentificadorEpisodios(),
                new GeradorRanking());
        ControladorAnalise controlador = new ControladorAnalise(servicoAnalise);

        mockMvc = MockMvcBuilders.standaloneSetup(controlador)
                .setControllerAdvice(new TratadorGlobalExcecoes())
                .build();
    }

    @Test
    void deveRetornarAnaliseCompleta() throws Exception {
        String conteudoCsv = """
                placa;dataHora;velocidadeKmh;limiteKmh
                ABC1D23;2026-10-05T08:00:00;72;60
                ABC1D23;2026-10-05T08:03:00;80;60
                DEF2G34;2026-10-05T08:00:00;-1;60
                """;
        MockMultipartFile arquivo = criarArquivo("leituras.csv", conteudoCsv);

        mockMvc.perform(multipart("/api/analises").file(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.linhasLidas").value(3))
                .andExpect(jsonPath("$.resumo.linhasValidas").value(2))
                .andExpect(jsonPath("$.resumo.totalEpisodios").value(1))
                .andExpect(jsonPath("$.episodios.length()").value(1))
                .andExpect(jsonPath("$.episodios[0].placa").value("ABC1D23"))
                .andExpect(jsonPath("$.episodios[0].duracaoSegundos").value(180))
                .andExpect(jsonPath("$.episodios[0].excessoMaximoPercentual").value(33.3))
                .andExpect(jsonPath("$.episodios[0].gravidade").value("GRAVE"))
                .andExpect(jsonPath("$.ranking.length()").value(1))
                .andExpect(jsonPath("$.ranking[0].placa").value("ABC1D23"))
                .andExpect(jsonPath("$.ranking[0].totalPontos").value(3))
                .andExpect(jsonPath("$.linhasInvalidas.length()").value(1))
                .andExpect(jsonPath("$.linhasInvalidas[0].numeroLinha").value(4));
    }

    @Test
    void deveRetornarHttp400ParaArquivoVazio() throws Exception {
        MockMultipartFile arquivo = criarArquivo("leituras.csv", "");

        mockMvc.perform(multipart("/api/analises").file(arquivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O arquivo está vazio."));
    }

    @Test
    void deveRetornarHttp400ParaCabecalhoInvalido() throws Exception {
        MockMultipartFile arquivo = criarArquivo(
                "leituras.csv",
                "placa;dataHora;velocidade;limite\n");

        mockMvc.perform(multipart("/api/analises").file(arquivo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "Cabeçalho inválido. Esperado: placa;dataHora;velocidadeKmh;limiteKmh."));
    }

    @Test
    void deveRetornarHttp400QuandoArquivoNaoForInformado() throws Exception {
        mockMvc.perform(multipart("/api/analises"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O arquivo deve ser informado no campo 'arquivo'."));
    }

    private MockMultipartFile criarArquivo(String nomeArquivo, String conteudo) {
        return new MockMultipartFile(
                "arquivo",
                nomeArquivo,
                "text/csv",
                conteudo.getBytes(StandardCharsets.UTF_8));
    }
}
