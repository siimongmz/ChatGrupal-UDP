package com.util;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Usuario implements Serializable {
    @Getter
    private final String nombreUsuario;
    @Getter @Setter
    private int puerto;

    public Usuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Usuario(String nombreUsuario, int puerto) {
        this.nombreUsuario = nombreUsuario;
        this.puerto = puerto;
    }
}
