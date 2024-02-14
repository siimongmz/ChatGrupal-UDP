package com.servidor.hilos;

import com.util.Mensaje;
import com.util.Usuario;
import com.servidor.Servidor;
import lombok.Getter;

import java.io.*;
import java.net.Socket;

public class GestionServidorTCP implements Runnable {
    private final Socket socket;
    @Getter
    private Usuario usuario;
    private boolean funcionando = true;
    private final ObjectInputStream entrada;
    private final ObjectOutputStream salida;
    private final String CODIGO_FIN = "termino";

    public GestionServidorTCP(Socket socket) {
        this.socket = socket;
        try {
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error en la salida o entrada");
        }
    }

    @Override
    public void run() {
        //Se pide un usuario para el inicio de sesion hasta que se entrege uno valido
        usuario = pedirUsuario();
        while (usuario == null) {
            usuario = pedirUsuario();
        }
        Servidor.addUser(usuario);

        //Se escucha la solicitud siempre y cuando haya bytes disponibles para leer
        while (funcionando) {
            try {
                if (socket.getInputStream().available() > 0) {
                    gestionarEntrada(entrada.readObject());
                }

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }


        }
        try {
            salida.close();
            entrada.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Recibe un objeto y en funcion del tipo se tratará de distinta forma
     * @param object El objeto sobre el cual queramos hacer la clasificacion
     */
    public void gestionarEntrada(Object object) {
        if (object instanceof String) {
            leerCodigo((String) object);
        } else if (object instanceof Mensaje) {
            leerMensaje((Mensaje) object);
        }
    }

    /**
     * Este metodo filtra en funcion del codigo enviado desde el cliente para saber que accion debe de realizar su socket correspondiente
     * @param codigo El codigo mediante el cual sabremos que sentencia ejecutar
     */
    private void leerCodigo(String codigo) {
        switch (codigo){
            case CODIGO_FIN ->{
                Servidor.removeClienteConectado(this);
                this.funcionando = false;
            }
        }
    }

    /**
     * Este metodo se encarga de gestionar el usuario que se envia desde el cliente
     * @return null en caso de que el usuario no sea valido y el usuario en caso de que sea valido
     */
    private Usuario pedirUsuario() {
        Usuario usuario;
        try {
            usuario = (Usuario) entrada.readObject();
            if (usuario != null && Servidor.esUsuarioValido(usuario)) {
                salida.writeInt(1);
                salida.writeObject(Servidor.getMensjaes());
                salida.flush();
                return usuario;

            } else {
                salida.writeInt(0);
                salida.flush();
            }

        } catch (IOException e) {
            System.out.println("IOException");
        } catch (ClassNotFoundException e) {
            System.out.println("Clase no encontrada");
        }


        return null;
    }

    /**
     * Metodo encargado de enviar la lista de usuarios del servidor al cliente del socket que gestiona
     */
    public void enviarUsuarios() {
        try {
            salida.writeObject(Servidor.getUserList());
            salida.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Este metodo se encarga de añadir el mensaje al servidor para que este se encarge de tratarlo
     * @param mensaje El mensaje que será tratado
     */
    private void leerMensaje(Mensaje mensaje) {
        Servidor.addMensaje(mensaje);
    }
    /**
     * Metodo encargado de enviar un mensaje del servidor al cliente del socket que gestiona
     * @param mensaje el mensaje que será enviado
     */
    public void enviarMensaje(Mensaje mensaje) {
        try {
            salida.writeObject(mensaje);
            salida.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
