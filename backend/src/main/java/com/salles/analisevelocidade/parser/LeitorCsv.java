package com.salles.analisevelocidade.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.salles.analisevelocidade.domain.LeituraVelocidade;
import com.salles.analisevelocidade.exception.ArquivoCsvInvalidoException;

@Component
public class LeitorCsv {

    private static final String CABECALHO_ESPERADO = "placa;dataHora;velocidadeKmh;limiteKmh";
    private static final int QUANTIDADE_CAMPOS_ESPERADA = 4;

    public ResultadoLeituraCsv ler(InputStream fluxoEntrada) throws IOException {
        if (fluxoEntrada == null) {
            throw new ArquivoCsvInvalidoException("Arquivo não informado.");
        }

        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(fluxoEntrada, StandardCharsets.UTF_8))) {
            validarCabecalho(leitor.readLine());

            List<LeituraVelocidade> leiturasValidas = new ArrayList<>();
            List<LinhaCsvInvalida> linhasInvalidas = new ArrayList<>();
            int numeroLinha = 1;
            String linha;

            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                try {
                    leiturasValidas.add(interpretarLinha(linha, numeroLinha));
                } catch (LinhaCsvInvalidaException excecao) {
                    linhasInvalidas.add(new LinhaCsvInvalida(numeroLinha, excecao.getMessage()));
                }
            }

            return new ResultadoLeituraCsv(leiturasValidas, linhasInvalidas);
        }
    }

    private void validarCabecalho(String cabecalho) {
        if (cabecalho == null) {
            throw new ArquivoCsvInvalidoException("O arquivo está vazio.");
        }
        if (!CABECALHO_ESPERADO.equals(cabecalho)) {
            throw new ArquivoCsvInvalidoException(
                    "Cabeçalho inválido. Esperado: " + CABECALHO_ESPERADO + ".");
        }
    }

    private LeituraVelocidade interpretarLinha(String linha, int numeroLinha)
            throws LinhaCsvInvalidaException {
        if (linha.isBlank()) {
            throw new LinhaCsvInvalidaException("Linha vazia.");
        }

        String[] campos = linha.split(";", -1);
        if (campos.length != QUANTIDADE_CAMPOS_ESPERADA) {
            throw new LinhaCsvInvalidaException("Quantidade de campos inválida: esperado 4.");
        }

        String placa = validarValorObrigatorio(campos[0], "placa").toUpperCase(Locale.ROOT);
        LocalDateTime dataHora = interpretarDataHora(validarValorObrigatorio(campos[1], "dataHora"));
        BigDecimal velocidade = interpretarNumero(
                validarValorObrigatorio(campos[2], "velocidadeKmh"), "velocidadeKmh");
        BigDecimal limiteVelocidade = interpretarNumero(
                validarValorObrigatorio(campos[3], "limiteKmh"), "limiteKmh");

        if (velocidade.signum() < 0) {
            throw new LinhaCsvInvalidaException("velocidadeKmh não pode ser negativa.");
        }
        if (limiteVelocidade.signum() <= 0) {
            throw new LinhaCsvInvalidaException("limiteKmh deve ser maior que zero.");
        }

        return new LeituraVelocidade(placa, dataHora, velocidade, limiteVelocidade, numeroLinha);
    }

    private String validarValorObrigatorio(String valor, String nomeCampo)
            throws LinhaCsvInvalidaException {
        String valorNormalizado = valor.trim();
        if (valorNormalizado.isEmpty()) {
            throw new LinhaCsvInvalidaException("Campo obrigatório vazio: " + nomeCampo + ".");
        }
        return valorNormalizado;
    }

    private LocalDateTime interpretarDataHora(String valor) throws LinhaCsvInvalidaException {
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException excecao) {
            throw new LinhaCsvInvalidaException("dataHora inválida.");
        }
    }

    private BigDecimal interpretarNumero(String valor, String nomeCampo)
            throws LinhaCsvInvalidaException {
        try {
            return new BigDecimal(valor.replace(',', '.'));
        } catch (NumberFormatException excecao) {
            throw new LinhaCsvInvalidaException("Valor numérico inválido em " + nomeCampo + ".");
        }
    }

    private static class LinhaCsvInvalidaException extends Exception {

        LinhaCsvInvalidaException(String mensagem) {
            super(mensagem);
        }
    }
}
