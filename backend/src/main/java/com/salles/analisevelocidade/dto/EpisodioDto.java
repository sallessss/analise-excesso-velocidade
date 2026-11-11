package com.salles.analisevelocidade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EpisodioDto(
        String placa,
        LocalDateTime inicio,
        LocalDateTime fim,
        long duracaoSegundos,
        BigDecimal velocidadeMaximaKmh,
        BigDecimal excessoMaximoPercentual,
        String gravidade) {
}
