package com.servidor.hilos;

import com.util.Mensaje;
import com.util.Usuario;
import com.servidor.Servidor;

import lombok.Getter;

import java.util.concurrent.LinkedBlockingQueue;

public class GestionServidorUDP implements Runnable {
    @Getter
    private Usuario usuario;
    @Getter
    private int puerto;
    private boolean funcionando = true;
    private LinkedBlockingQueue<Object> listaDeEspera = new LinkedBlockingQueue<>();
    private final String CODIGO_FIN = "termino";

    public GestionServidorUDP(int puerto) {
        this.puerto = puerto;
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
                    gestionarEntrada(listaDeEspera.take());

            } catch (InterruptedException e) {
                break;
            }


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
            //se queda bloqueado
            usuario = (Usuario) listaDeEspera.take();
            if (usuario != null && Servidor.esUsuarioValido(usuario)) {
                Servidor.enviarAlCliente(Integer.valueOf(1),this.puerto);
                Servidor.enviarAlCliente(Servidor.getMensjaes(),this.puerto);
                return usuario;

            } else {
                Servidor.enviarAlCliente(Integer.valueOf(0),this.puerto);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        return null;
    }

    /**
     * Metodo encargado de enviar la lista de usuarios del servidor al cliente del socket que gestiona
     */
    public void enviarUsuarios() {
        Servidor.enviarAlCliente(Servidor.getUserList(),this.puerto);
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
        Servidor.enviarAlCliente(mensaje,this.puerto);
    }




    public void agregarObjetoRecibido(Object o) {
        try {
            listaDeEspera.put(o);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
