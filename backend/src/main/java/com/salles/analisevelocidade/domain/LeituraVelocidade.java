package com.salles.analisevelocidade.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeituraVelocidade(
        String placa,
        LocalDateTime dataHora,
        BigDecimal velocidadeKmh,
        BigDecimal limiteKmh,
        int numeroLinhaOrigem) {
}
