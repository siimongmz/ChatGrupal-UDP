package com.util;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;

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
