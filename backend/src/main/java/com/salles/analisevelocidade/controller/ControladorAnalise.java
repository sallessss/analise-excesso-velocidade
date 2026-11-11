package com.salles.analisevelocidade.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.salles.analisevelocidade.dto.RespostaAnaliseDto;
import com.salles.analisevelocidade.exception.ArquivoCsvInvalidoException;
import com.salles.analisevelocidade.service.ServicoAnalise;

@RestController
@RequestMapping("/api/analises")
public class ControladorAnalise {

    private final ServicoAnalise servicoAnalise;

    public ControladorAnalise(ServicoAnalise servicoAnalise) {
        this.servicoAnalise = servicoAnalise;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RespostaAnaliseDto analisar(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            throw new ArquivoCsvInvalidoException("O arquivo está vazio.");
        }

        return servicoAnalise.analisar(arquivo.getInputStream());
    }
}
