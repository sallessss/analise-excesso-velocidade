package com.salles.analisevelocidade.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.salles.analisevelocidade.domain.EpisodioExcesso;
import com.salles.analisevelocidade.domain.PosicaoRanking;

@Component
public class GeradorRanking {

    public List<PosicaoRanking> gerar(List<EpisodioExcesso> episodios) {
        Map<String, AcumuladorRanking> acumuladoresPorPlaca = new HashMap<>();

        for (EpisodioExcesso episodio : episodios) {
            acumuladoresPorPlaca
                    .computeIfAbsent(episodio.placa(), placa -> new AcumuladorRanking())
                    .adicionar(episodio);
        }

        return acumuladoresPorPlaca.entrySet().stream()
                .map(entrada -> entrada.getValue().criarPosicao(entrada.getKey()))
                .sorted(Comparator.comparingInt(PosicaoRanking::totalPontos).reversed()
                        .thenComparing(PosicaoRanking::maiorVelocidadeKmh, Comparator.reverseOrder())
                        .thenComparing(PosicaoRanking::placa))
                .toList();
    }

    private static final class AcumuladorRanking {

        private int totalPontos;
        private BigDecimal maiorVelocidadeKmh;

        private void adicionar(EpisodioExcesso episodio) {
            totalPontos += episodio.gravidade().pontos();

            if (maiorVelocidadeKmh == null
                    || episodio.velocidadeMaximaKmh().compareTo(maiorVelocidadeKmh) > 0) {
                maiorVelocidadeKmh = episodio.velocidadeMaximaKmh();
            }
        }

        private PosicaoRanking criarPosicao(String placa) {
            return new PosicaoRanking(placa, totalPontos, maiorVelocidadeKmh);
        }
    }
}
