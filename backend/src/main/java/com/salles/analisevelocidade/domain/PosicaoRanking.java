package com.salles.analisevelocidade.domain;

import java.math.BigDecimal;

public record PosicaoRanking(
        String placa,
        int totalPontos,
        BigDecimal maiorVelocidadeKmh) {
}
