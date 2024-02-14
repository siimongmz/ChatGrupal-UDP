package com.util;

import lombok.Getter;

import java.io.Serializable;

public class Usuario implements Serializable {
    @Getter
    private final String nombreUsuario;

    public Usuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

}
