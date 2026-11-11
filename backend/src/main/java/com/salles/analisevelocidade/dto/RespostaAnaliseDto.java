package com.salles.analisevelocidade.dto;

import java.util.List;

public record RespostaAnaliseDto(
        ResumoDto resumo,
        List<EpisodioDto> episodios,
        List<PosicaoRankingDto> ranking,
        List<LinhaInvalidaDto> linhasInvalidas) {

    public RespostaAnaliseDto {
        episodios = List.copyOf(episodios);
        ranking = List.copyOf(ranking);
        linhasInvalidas = List.copyOf(linhasInvalidas);
    }
}
