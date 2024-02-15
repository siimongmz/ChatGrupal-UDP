package com.servidor;

import com.util.Mensaje;
import com.util.Usuario;
import com.servidor.hilos.GestionServidorUDP;
import lombok.Getter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    static final int PORT = 6000;
    private static InetAddress direccion;
    private static DatagramSocket server;
    private static final List<Usuario> usuarios = new ArrayList<>();
    private static final List<GestionServidorUDP> clientesConectados = new ArrayList<>();
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
        for(GestionServidorUDP h : clientesConectados){
            h.enviarUsuarios();
        }
    }

    /**
     * Se encarga de añadir el nuevo mensajes al historial de mensajes y notificar del nuevo mensaje a todos los clientes
     * @param mensaje El mensaje que será enviado
     */
    public static void addMensaje(Mensaje mensaje){
        mensjaes.add(mensaje);
        for(GestionServidorUDP h : clientesConectados){
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
     * @param gestionServidorUDP
     */
    public static void removeClienteConectado(GestionServidorUDP gestionServidorUDP) {
        usuarios.remove(gestionServidorUDP.getUsuario());
        clientesConectados.remove(gestionServidorUDP);
        for(GestionServidorUDP h : clientesConectados){
            h.enviarUsuarios();
        }

    }

    public static void iniciarComponentesServidor(){
        try {
            server = new DatagramSocket(6000);
            direccion = InetAddress.getByName("localhost");
        } catch (UnknownHostException | SocketException e) {
            System.out.println("Error en el inicio del servidor");
        }
    }
    public static void enviarAlCliente(Object o,int puerto){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        byte[] datos;

        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            datos = baos.toByteArray();

            DatagramPacket packet = new DatagramPacket(datos, datos.length, direccion, puerto);
            server.send(packet);
        } catch (IOException e) {
            System.out.println("Error al enviar al servidor");
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        try {
            iniciarComponentesServidor();
            byte[] datos = new byte[1024];
            DatagramPacket packet = new DatagramPacket(datos, datos.length);
            while (funcionando) {

                server.receive(packet);
                ByteArrayInputStream bais = new ByteArrayInputStream(datos);
                ObjectInputStream ois = new ObjectInputStream(bais);

                boolean puertoEncontrado = false;
                for(GestionServidorUDP gs : clientesConectados){
                    if (gs.getPuerto() == packet.getPort()){

                        gs.agregarObjetoRecibido(ois.readObject());
                        puertoEncontrado = true;
                        break;
                    }
                }
                if(!puertoEncontrado) {
                    GestionServidorUDP gestionServidorUDP = new GestionServidorUDP(packet.getPort());
                    Thread nuevoHiloCLiente = new Thread(gestionServidorUDP);
                    clientesConectados.add(gestionServidorUDP);
                    nuevoHiloCLiente.start();
                    gestionServidorUDP.agregarObjetoRecibido(ois.readObject());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
