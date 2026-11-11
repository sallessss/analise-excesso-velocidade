package com.salles.analisevelocidade.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.salles.analisevelocidade.dto.RespostaErroDto;

@RestControllerAdvice
public class TratadorGlobalExcecoes {

    @ExceptionHandler(ArquivoCsvInvalidoException.class)
    public ResponseEntity<RespostaErroDto> tratarArquivoInvalido(ArquivoCsvInvalidoException excecao) {
        return ResponseEntity.badRequest().body(new RespostaErroDto(excecao.getMessage()));
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<RespostaErroDto> tratarArquivoAusente(Exception excecao) {
        return ResponseEntity.badRequest()
                .body(new RespostaErroDto("O arquivo deve ser informado no campo 'arquivo'."));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<RespostaErroDto> tratarFalhaLeitura(IOException excecao) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RespostaErroDto("Não foi possível ler o arquivo enviado."));
    }
}
