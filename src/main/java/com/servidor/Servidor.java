package com.servidor;

import com.util.Mensaje;
import com.util.Usuario;
import com.servidor.hilos.GestionServidorTCP;
import lombok.Getter;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    static final int PORT = 6000;
    private static final List<Usuario> usuarios = new ArrayList<>();
    private static final List<GestionServidorTCP> clientesConectados = new ArrayList<>();
    @Getter
    private static final ArrayList<Mensaje> mensjaes = new ArrayList<>();
    protected static boolean funcionando = true;

    /**
     * Este metodo se encarga de validar si el usuario es valido (no esta ni vacio ni formado por espacios y ademas no existe ya en la lista de usuarios del servidor)
     * @param usuarioNuevo El usuario que será comprobado
     * @return true en caso de que sea valido, false en caso de que no sea valido
     */
    public static boolean esUsuarioValido(Usuario usuarioNuevo) {
        if (usuarioNuevo.getNombreUsuario() != null && usuarios.isEmpty() && !usuarioNuevo.getNombreUsuario().isBlank()){
            return true;
        }

        for (Usuario u : usuarios) {
            if (u == null || usuarioNuevo == null ||usuarioNuevo.getNombreUsuario().isBlank()||usuarioNuevo.getNombreUsuario().isEmpty()||u.getNombreUsuario().equalsIgnoreCase(usuarioNuevo.getNombreUsuario())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Se encarga de añadir el nuevo usuario a la lista y de notificar a todos los clientes del cambio
     * @param user el usuario que se añadirá
     */
    public static void addUser(Usuario user){
        usuarios.add(user);
        for(GestionServidorTCP h : clientesConectados){
            h.enviarUsuarios();
        }
    }

    /**
     * Se encarga de añadir el nuevo mensajes al historial de mensajes y notificar del nuevo mensaje a todos los clientes
     * @param mensaje El mensaje que será enviado
     */
    public static void addMensaje(Mensaje mensaje){
        mensjaes.add(mensaje);
        for(GestionServidorTCP h : clientesConectados){
            h.enviarMensaje(mensaje);
        }

    }

    /**
     * Se encarga de transformar la lista de usuarios en una lista de los nombres de los usuarios
     * @return Un ArrayList con los nombres de los usuarios
     */
    public static ArrayList<String> getUserList(){
        ArrayList<String> resultado= new ArrayList<>();
        for (Usuario u : usuarios){
            resultado.add(u.getNombreUsuario());
        }
        return resultado;
    }

    /**
     * Elimina un hilo gestor de los clientes y notifica al resto de clientes del cambio
     * @param gestionServidorTCP
     */
    public static void removeClienteConectado(GestionServidorTCP gestionServidorTCP) {
        usuarios.remove(gestionServidorTCP.getUsuario());
        clientesConectados.remove(gestionServidorTCP);
        for(GestionServidorTCP h : clientesConectados){
            h.enviarUsuarios();
        }

    }
    public static void main(String[] args) {
        try {
            ServerSocket servidor = new ServerSocket(PORT);
            while (funcionando) {
                GestionServidorTCP gestionServidorTCP = new GestionServidorTCP(servidor.accept());
                Thread nuevoHiloCLiente = new Thread(gestionServidorTCP);
                clientesConectados.add(gestionServidorTCP);
                nuevoHiloCLiente.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
