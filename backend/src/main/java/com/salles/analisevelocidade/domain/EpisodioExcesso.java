package com.salles.analisevelocidade.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EpisodioExcesso(
        String placa,
        LocalDateTime inicio,
        LocalDateTime fim,
        long duracaoSegundos,
        BigDecimal velocidadeMaximaKmh,
        BigDecimal excessoMaximoPercentual,
        Gravidade gravidade) {
}
