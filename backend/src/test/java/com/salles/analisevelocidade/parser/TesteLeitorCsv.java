package com.salles.analisevelocidade.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.salles.analisevelocidade.domain.LeituraVelocidade;
import com.salles.analisevelocidade.exception.ArquivoCsvInvalidoException;

class TesteLeitorCsv {

    private final LeitorCsv leitorCsv = new LeitorCsv();

    @Test
    void deveLerLinhasValidasENormalizarValoresSuportados() throws Exception {
        String conteudoCsv = """
                placa;dataHora;velocidadeKmh;limiteKmh
                abc1d23;2026-10-05T08:00:00;72,5;60
                """;

        ResultadoLeituraCsv resultado = leitorCsv.ler(criarFluxoEntrada(conteudoCsv));

        assertEquals(1, resultado.leiturasValidas().size());
        assertEquals(0, resultado.linhasInvalidas().size());

        LeituraVelocidade leitura = resultado.leiturasValidas().getFirst();
        assertEquals("ABC1D23", leitura.placa());
        assertEquals(LocalDateTime.of(2026, 10, 5, 8, 0), leitura.dataHora());
        assertEquals(new BigDecimal("72.5"), leitura.velocidadeKmh());
        assertEquals(new BigDecimal("60"), leitura.limiteKmh());
        assertEquals(2, leitura.numeroLinhaOrigem());
    }

    @Test
    void deveInformarLinhasInvalidasEContinuarLeitura() throws Exception {
        String conteudoCsv = """
                placa;dataHora;velocidadeKmh;limiteKmh
                ABC1D23;2026-10-05T08:00:00;72;60
                ;2026-10-05T08:01:00;72;60
                ABC1D23;05/10/2026 08:02:00;72;60
                ABC1D23;2026-10-05T08:03:00;-1;60
                ABC1D23;2026-10-05T08:04:00;72;0
                ABC1D23;2026-10-05T08:05:00;abc;60
                ABC1D23;2026-10-05T08:06:00;72

                DEF2G34;2026-10-05T08:07:00;80;60
                """;

        ResultadoLeituraCsv resultado = leitorCsv.ler(criarFluxoEntrada(conteudoCsv));

        assertEquals(2, resultado.leiturasValidas().size());
        assertEquals(7, resultado.linhasInvalidas().size());
        assertEquals(2, resultado.leiturasValidas().getFirst().numeroLinhaOrigem());
        assertEquals(10, resultado.leiturasValidas().getLast().numeroLinhaOrigem());
        assertEquals(
                new LinhaCsvInvalida(3, "Campo obrigatório vazio: placa."),
                resultado.linhasInvalidas().get(0));
        assertEquals(
                new LinhaCsvInvalida(4, "dataHora inválida."),
                resultado.linhasInvalidas().get(1));
        assertEquals(
                new LinhaCsvInvalida(5, "velocidadeKmh não pode ser negativa."),
                resultado.linhasInvalidas().get(2));
        assertEquals(
                new LinhaCsvInvalida(6, "limiteKmh deve ser maior que zero."),
                resultado.linhasInvalidas().get(3));
        assertEquals(
                new LinhaCsvInvalida(7, "Valor numérico inválido em velocidadeKmh."),
                resultado.linhasInvalidas().get(4));
        assertEquals(
                new LinhaCsvInvalida(8, "Quantidade de campos inválida: esperado 4."),
                resultado.linhasInvalidas().get(5));
        assertEquals(
                new LinhaCsvInvalida(9, "Linha vazia."),
                resultado.linhasInvalidas().get(6));
    }

    @Test
    void deveRejeitarArquivoVazio() {
        ArquivoCsvInvalidoException excecao = assertThrows(
                ArquivoCsvInvalidoException.class,
                () -> leitorCsv.ler(criarFluxoEntrada("")));

        assertEquals("O arquivo está vazio.", excecao.getMessage());
    }

    @Test
    void deveRejeitarCabecalhoInesperado() {
        String conteudoCsv = "placa;dataHora;velocidade;limite\n";

        ArquivoCsvInvalidoException excecao = assertThrows(
                ArquivoCsvInvalidoException.class,
                () -> leitorCsv.ler(criarFluxoEntrada(conteudoCsv)));

        assertEquals(
                "Cabeçalho inválido. Esperado: placa;dataHora;velocidadeKmh;limiteKmh.",
                excecao.getMessage());
    }

    @Test
    void deveTratarLimiteNegativoComoLinhaInvalidaEContinuarLeitura() throws Exception {
        String conteudoCsv = """
                placa;dataHora;velocidadeKmh;limiteKmh
                ABC1D23;2026-10-05T08:00:00;72;-60
                DEF2G34;2026-10-05T08:01:00;80;60
                """;

        ResultadoLeituraCsv resultado = leitorCsv.ler(criarFluxoEntrada(conteudoCsv));

        assertEquals(1, resultado.leiturasValidas().size());
        assertEquals("DEF2G34", resultado.leiturasValidas().getFirst().placa());
        assertEquals(
                List.of(new LinhaCsvInvalida(2, "limiteKmh deve ser maior que zero.")),
                resultado.linhasInvalidas());
    }

    @Test
    void deveLerArquivoReferenciaEInformarLinhasInvalidas() throws Exception {
        Path diretorioClassesTeste = Path.of(
                TesteLeitorCsv.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path arquivoReferencia = diretorioClassesTeste
                .getParent()
                .getParent()
                .getParent()
                .resolve("leituras.csv");

        ResultadoLeituraCsv resultado;
        try (var fluxoEntrada = Files.newInputStream(arquivoReferencia)) {
            resultado = leitorCsv.ler(fluxoEntrada);
        }

        assertEquals(35, resultado.leiturasValidas().size());
        assertEquals(8, resultado.linhasInvalidas().size());
        assertEquals(
                List.of(4, 9, 12, 21, 22, 27, 38, 41),
                resultado.linhasInvalidas().stream().map(LinhaCsvInvalida::numeroLinha).toList());
    }

    private ByteArrayInputStream criarFluxoEntrada(String conteudo) {
        return new ByteArrayInputStream(conteudo.getBytes(StandardCharsets.UTF_8));
    }
}
