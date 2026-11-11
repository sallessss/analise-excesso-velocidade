package com.salles.analisevelocidade.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.salles.analisevelocidade.domain.EpisodioExcesso;
import com.salles.analisevelocidade.domain.PosicaoRanking;
import com.salles.analisevelocidade.dto.EpisodioDto;
import com.salles.analisevelocidade.dto.LinhaInvalidaDto;
import com.salles.analisevelocidade.dto.PosicaoRankingDto;
import com.salles.analisevelocidade.dto.RespostaAnaliseDto;
import com.salles.analisevelocidade.dto.ResumoDto;
import com.salles.analisevelocidade.parser.LeitorCsv;
import com.salles.analisevelocidade.parser.LinhaCsvInvalida;
import com.salles.analisevelocidade.parser.ResultadoLeituraCsv;

@Service
public class ServicoAnalise {

    private final LeitorCsv leitorCsv;
    private final IdentificadorEpisodios identificadorEpisodios;
    private final GeradorRanking geradorRanking;

    public ServicoAnalise(
            LeitorCsv leitorCsv,
            IdentificadorEpisodios identificadorEpisodios,
            GeradorRanking geradorRanking) {
        this.leitorCsv = leitorCsv;
        this.identificadorEpisodios = identificadorEpisodios;
        this.geradorRanking = geradorRanking;
    }

    public RespostaAnaliseDto analisar(InputStream fluxoEntrada) throws IOException {
        ResultadoLeituraCsv resultadoLeitura = leitorCsv.ler(fluxoEntrada);
        List<EpisodioExcesso> episodios = identificadorEpisodios.identificar(resultadoLeitura.leiturasValidas());
        List<PosicaoRanking> ranking = geradorRanking.gerar(episodios);

        ResumoDto resumo = new ResumoDto(
                resultadoLeitura.leiturasValidas().size() + resultadoLeitura.linhasInvalidas().size(),
                resultadoLeitura.leiturasValidas().size(),
                episodios.size());

        return new RespostaAnaliseDto(
                resumo,
                episodios.stream().map(this::converterEpisodio).toList(),
                ranking.stream().map(this::converterPosicaoRanking).toList(),
                resultadoLeitura.linhasInvalidas().stream().map(this::converterLinhaInvalida).toList());
    }

    private EpisodioDto converterEpisodio(EpisodioExcesso episodio) {
        return new EpisodioDto(
                episodio.placa(),
                episodio.inicio(),
                episodio.fim(),
                episodio.duracaoSegundos(),
                episodio.velocidadeMaximaKmh(),
                episodio.excessoMaximoPercentual(),
                episodio.gravidade().name());
    }

    private PosicaoRankingDto converterPosicaoRanking(PosicaoRanking posicao) {
        return new PosicaoRankingDto(
                posicao.placa(),
                posicao.totalPontos(),
                posicao.maiorVelocidadeKmh());
    }

    private LinhaInvalidaDto converterLinhaInvalida(LinhaCsvInvalida linhaInvalida) {
        return new LinhaInvalidaDto(linhaInvalida.numeroLinha(), linhaInvalida.motivo());
    }
}
