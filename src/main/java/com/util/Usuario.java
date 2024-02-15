package com.util;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Usuario implements Serializable {
    @Getter
    private final String nombreUsuario;

    public Usuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

}
