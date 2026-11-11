package com.salles.analisevelocidade.parser;

import java.util.List;

import com.salles.analisevelocidade.domain.LeituraVelocidade;

public record ResultadoLeituraCsv(
        List<LeituraVelocidade> leiturasValidas,
        List<LinhaCsvInvalida> linhasInvalidas) {

    public ResultadoLeituraCsv {
        leiturasValidas = List.copyOf(leiturasValidas);
        linhasInvalidas = List.copyOf(linhasInvalidas);
    }
}
