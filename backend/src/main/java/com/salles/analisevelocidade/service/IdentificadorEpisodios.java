package com.salles.analisevelocidade.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.salles.analisevelocidade.domain.EpisodioExcesso;
import com.salles.analisevelocidade.domain.Gravidade;
import com.salles.analisevelocidade.domain.LeituraVelocidade;

@Component
public class IdentificadorEpisodios {

    private static final BigDecimal TOLERANCIA_KMH = new BigDecimal("5");
    private static final Duration INTERVALO_MAXIMO = Duration.ofMinutes(5);
    private static final BigDecimal CEM = new BigDecimal("100");

    private static final Comparator<LeituraVelocidade> ORDEM_LEITURAS = Comparator
            .comparing(LeituraVelocidade::dataHora)
            .thenComparingInt(LeituraVelocidade::numeroLinhaOrigem);

    private static final Comparator<EpisodioExcesso> ORDEM_EPISODIOS = Comparator
            .comparing(EpisodioExcesso::inicio)
            .thenComparing(EpisodioExcesso::placa);

    public List<EpisodioExcesso> identificar(List<LeituraVelocidade> leituras) {
        Map<String, List<LeituraVelocidade>> leiturasPorPlaca = leituras.stream()
                .collect(Collectors.groupingBy(
                        LeituraVelocidade::placa,
                        TreeMap::new,
                        Collectors.toCollection(ArrayList::new)));

        List<EpisodioExcesso> episodios = new ArrayList<>();
        leiturasPorPlaca.values().forEach(leiturasVeiculo -> {
            leiturasVeiculo.sort(ORDEM_LEITURAS);
            episodios.addAll(identificarPorVeiculo(leiturasVeiculo));
        });
        episodios.sort(ORDEM_EPISODIOS);

        return List.copyOf(episodios);
    }

    private List<EpisodioExcesso> identificarPorVeiculo(List<LeituraVelocidade> leituras) {
        List<EpisodioExcesso> episodios = new ArrayList<>();
        List<LeituraVelocidade> leiturasEpisodio = new ArrayList<>();

        for (LeituraVelocidade leitura : leituras) {
            if (!estaEmExcesso(leitura)) {
                finalizarEpisodio(leiturasEpisodio, episodios);
                continue;
            }

            if (!leiturasEpisodio.isEmpty()) {
                LeituraVelocidade leituraAnterior = leiturasEpisodio.getLast();
                Duration intervalo = Duration.between(leituraAnterior.dataHora(), leitura.dataHora());
                if (intervalo.compareTo(INTERVALO_MAXIMO) > 0) {
                    finalizarEpisodio(leiturasEpisodio, episodios);
                }
            }

            leiturasEpisodio.add(leitura);
        }

        finalizarEpisodio(leiturasEpisodio, episodios);
        return episodios;
    }

    private boolean estaEmExcesso(LeituraVelocidade leitura) {
        BigDecimal limiteComTolerancia = leitura.limiteKmh().add(TOLERANCIA_KMH);
        return leitura.velocidadeKmh().compareTo(limiteComTolerancia) > 0;
    }

    private void finalizarEpisodio(
            List<LeituraVelocidade> leiturasEpisodio,
            List<EpisodioExcesso> episodios) {
        if (leiturasEpisodio.isEmpty()) {
            return;
        }

        LeituraVelocidade primeiraLeitura = leiturasEpisodio.getFirst();
        LeituraVelocidade ultimaLeitura = leiturasEpisodio.getLast();
        BigDecimal velocidadeMaxima = leiturasEpisodio.stream()
                .map(LeituraVelocidade::velocidadeKmh)
                .max(BigDecimal::compareTo)
                .orElseThrow();
        BigDecimal excessoMaximo = leiturasEpisodio.stream()
                .map(this::calcularExcessoPercentual)
                .max(BigDecimal::compareTo)
                .orElseThrow();

        episodios.add(new EpisodioExcesso(
                primeiraLeitura.placa(),
                primeiraLeitura.dataHora(),
                ultimaLeitura.dataHora(),
                Duration.between(primeiraLeitura.dataHora(), ultimaLeitura.dataHora()).getSeconds(),
                velocidadeMaxima,
                excessoMaximo,
                Gravidade.classificar(excessoMaximo)));
        leiturasEpisodio.clear();
    }

    private BigDecimal calcularExcessoPercentual(LeituraVelocidade leitura) {
        return leitura.velocidadeKmh()
                .subtract(leitura.limiteKmh())
                .multiply(CEM)
                .divide(leitura.limiteKmh(), 1, RoundingMode.HALF_UP);
    }
}
