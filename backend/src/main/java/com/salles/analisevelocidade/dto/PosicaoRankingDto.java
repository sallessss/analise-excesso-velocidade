package com.salles.analisevelocidade.dto;

import java.math.BigDecimal;

public record PosicaoRankingDto(
        String placa,
        int totalPontos,
        BigDecimal maiorVelocidadeKmh) {
}
