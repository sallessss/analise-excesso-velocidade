package com.salles.analisevelocidade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.salles.analisevelocidade.domain.EpisodioExcesso;
import com.salles.analisevelocidade.domain.Gravidade;
import com.salles.analisevelocidade.domain.LeituraVelocidade;
import com.salles.analisevelocidade.parser.LeitorCsv;
import com.salles.analisevelocidade.parser.ResultadoLeituraCsv;

class TesteIdentificadorEpisodios {

    private final IdentificadorEpisodios identificador = new IdentificadorEpisodios();

    @Test
    void naoDeveCriarEpisodioQuandoVelocidadeForExatamenteCincoAcimaDoLimite() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:00:00", "65", "60", 2)));

        assertTrue(episodios.isEmpty());
    }

    @Test
    void deveAgruparLeiturasConsecutivasEmUmEpisodio() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:00:00", "72", "60", 2),
                leitura("ABC1D23", "2026-10-05T08:03:00", "80", "60", 3)));

        assertEquals(1, episodios.size());
        EpisodioExcesso episodio = episodios.getFirst();
        assertEquals("ABC1D23", episodio.placa());
        assertEquals(LocalDateTime.parse("2026-10-05T08:00:00"), episodio.inicio());
        assertEquals(LocalDateTime.parse("2026-10-05T08:03:00"), episodio.fim());
        assertEquals(180, episodio.duracaoSegundos());
        assertEquals(new BigDecimal("80"), episodio.velocidadeMaximaKmh());
        assertEquals(new BigDecimal("33.3"), episodio.excessoMaximoPercentual());
        assertEquals(Gravidade.GRAVE, episodio.gravidade());
    }

    @Test
    void deveManterEpisodioQuandoIntervaloForExatamenteCincoMinutos() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:00:00", "72", "60", 2),
                leitura("ABC1D23", "2026-10-05T08:05:00", "74", "60", 3)));

        assertEquals(1, episodios.size());
        assertEquals(300, episodios.getFirst().duracaoSegundos());
    }

    @Test
    void deveEncerrarEpisodioQuandoIntervaloForMaiorQueCincoMinutos() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:00:00", "72", "60", 2),
                leitura("ABC1D23", "2026-10-05T08:05:01", "74", "60", 3)));

        assertEquals(2, episodios.size());
        assertEquals(0, episodios.get(0).duracaoSegundos());
        assertEquals(0, episodios.get(1).duracaoSegundos());
    }

    @Test
    void deveEncerrarEpisodioAoEncontrarLeituraSemExcesso() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:00:00", "72", "60", 2),
                leitura("ABC1D23", "2026-10-05T08:01:00", "60", "60", 3),
                leitura("ABC1D23", "2026-10-05T08:02:00", "75", "60", 4)));

        assertEquals(2, episodios.size());
        assertEquals(LocalDateTime.parse("2026-10-05T08:00:00"), episodios.get(0).inicio());
        assertEquals(LocalDateTime.parse("2026-10-05T08:02:00"), episodios.get(1).inicio());
    }

    @Test
    void deveCalcularMaiorPercentualIndependentementeDaMaiorVelocidade() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:00:00", "120", "100", 2),
                leitura("ABC1D23", "2026-10-05T08:01:00", "90", "50", 3)));

        EpisodioExcesso episodio = episodios.getFirst();
        assertEquals(new BigDecimal("120"), episodio.velocidadeMaximaKmh());
        assertEquals(new BigDecimal("80.0"), episodio.excessoMaximoPercentual());
        assertEquals(Gravidade.GRAVISSIMA, episodio.gravidade());
    }

    @Test
    void deveArredondarPercentualParaUmaCasaDecimalComHalfUp() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:00:00", "66.675", "50", 2)));

        assertEquals(new BigDecimal("33.4"), episodios.getFirst().excessoMaximoPercentual());
    }

    @Test
    void deveOrdenarLeiturasCronologicamente() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:10:00", "72", "60", 4),
                leitura("ABC1D23", "2026-10-05T08:00:00", "72", "60", 2),
                leitura("ABC1D23", "2026-10-05T08:05:00", "72", "60", 3)));

        assertEquals(1, episodios.size());
        assertEquals(LocalDateTime.parse("2026-10-05T08:00:00"), episodios.getFirst().inicio());
        assertEquals(LocalDateTime.parse("2026-10-05T08:10:00"), episodios.getFirst().fim());
        assertEquals(600, episodios.getFirst().duracaoSegundos());
    }

    @Test
    void deveDesempatarMesmoHorarioPeloNumeroDaLinhaOriginal() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:01:00", "75", "60", 4),
                leitura("ABC1D23", "2026-10-05T08:00:00", "60", "60", 3),
                leitura("ABC1D23", "2026-10-05T08:00:00", "72", "60", 2)));

        assertEquals(2, episodios.size());
        assertEquals(LocalDateTime.parse("2026-10-05T08:00:00"), episodios.get(0).inicio());
        assertEquals(LocalDateTime.parse("2026-10-05T08:01:00"), episodios.get(1).inicio());
    }

    @Test
    void deveCalcularDuracaoCorretaAoAtravessarMeiaNoite() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T23:58:00", "72", "60", 2),
                leitura("ABC1D23", "2026-10-06T00:01:00", "75", "60", 3)));

        assertEquals(1, episodios.size());
        assertEquals(180, episodios.getFirst().duracaoSegundos());
    }

    @Test
    void deveCriarEpisodioDeDuracaoZeroParaUmaUnicaLeitura() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("ABC1D23", "2026-10-05T08:00:00", "72", "60", 2)));

        assertEquals(1, episodios.size());
        assertEquals(episodios.getFirst().inicio(), episodios.getFirst().fim());
        assertEquals(0, episodios.getFirst().duracaoSegundos());
    }

    @Test
    void deveSepararLeiturasPorPlaca() {
        List<EpisodioExcesso> episodios = identificador.identificar(List.of(
                leitura("DEF2G34", "2026-10-05T08:00:00", "72", "60", 2),
                leitura("ABC1D23", "2026-10-05T08:01:00", "75", "60", 3)));

        assertEquals(2, episodios.size());
        assertEquals("DEF2G34", episodios.get(0).placa());
        assertEquals("ABC1D23", episodios.get(1).placa());
    }

    @Test
    void deveIdentificarEpisodiosDoArquivoReferencia() throws Exception {
        Path diretorioClassesTeste = Path.of(
                TesteIdentificadorEpisodios.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI());
        Path arquivoReferencia = diretorioClassesTeste
                .getParent()
                .getParent()
                .getParent()
                .resolve("leituras.csv");

        ResultadoLeituraCsv resultadoLeitura;
        try (var fluxoEntrada = Files.newInputStream(arquivoReferencia)) {
            resultadoLeitura = new LeitorCsv().ler(fluxoEntrada);
        }

        List<EpisodioExcesso> episodios = identificador.identificar(resultadoLeitura.leiturasValidas());

        assertEquals(12, episodios.size());
        assertEquals(3, contarPorGravidade(episodios, Gravidade.LEVE));
        assertEquals(7, contarPorGravidade(episodios, Gravidade.GRAVE));
        assertEquals(2, contarPorGravidade(episodios, Gravidade.GRAVISSIMA));
    }

    private LeituraVelocidade leitura(
            String placa,
            String dataHora,
            String velocidadeKmh,
            String limiteKmh,
            int numeroLinha) {
        return new LeituraVelocidade(
                placa,
                LocalDateTime.parse(dataHora),
                new BigDecimal(velocidadeKmh),
                new BigDecimal(limiteKmh),
                numeroLinha);
    }

    private long contarPorGravidade(List<EpisodioExcesso> episodios, Gravidade gravidade) {
        return episodios.stream().filter(episodio -> episodio.gravidade() == gravidade).count();
    }
}
