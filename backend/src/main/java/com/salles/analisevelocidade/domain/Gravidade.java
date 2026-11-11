package com.salles.analisevelocidade.domain;

import java.math.BigDecimal;

public enum Gravidade {
    LEVE(1),
    GRAVE(3),
    GRAVISSIMA(5);

    private static final BigDecimal LIMITE_LEVE = new BigDecimal("20.0");
    private static final BigDecimal LIMITE_GRAVE = new BigDecimal("50.0");

    private final int pontos;

    Gravidade(int pontos) {
        this.pontos = pontos;
    }

    public int pontos() {
        return pontos;
    }

    public static Gravidade classificar(BigDecimal excessoPercentual) {
        if (excessoPercentual.compareTo(LIMITE_LEVE) <= 0) {
            return LEVE;
        }
        if (excessoPercentual.compareTo(LIMITE_GRAVE) <= 0) {
            return GRAVE;
        }
        return GRAVISSIMA;
    }
}
