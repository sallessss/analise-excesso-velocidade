package com.salles.analisevelocidade.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TesteGravidade {

    @Test
    void deveRespeitarLimitesDeVinteECinquentaPorCento() {
        assertEquals(Gravidade.LEVE, Gravidade.classificar(new BigDecimal("20.0")));
        assertEquals(Gravidade.GRAVE, Gravidade.classificar(new BigDecimal("20.1")));
        assertEquals(Gravidade.GRAVE, Gravidade.classificar(new BigDecimal("50.0")));
        assertEquals(Gravidade.GRAVISSIMA, Gravidade.classificar(new BigDecimal("50.1")));
    }
}
