package com.util;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


public class Mensaje implements Serializable {
    @Getter
    private String contenido;
    @Getter
    private Usuario usuario;
    private final LocalDateTime fechaEnvio;

    public Mensaje(String contenido, Usuario usuario) {
        this.contenido = contenido;
        this.usuario = usuario;
        this.fechaEnvio = LocalDateTime.now();
    }
}
