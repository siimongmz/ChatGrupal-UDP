package com.chat.chatgrupal;
import com.chat.hilos.GestionCliente;
import com.util.Mensaje;
import com.util.Usuario;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class ChatApplication extends Application {
    @Getter @Setter
    private static Usuario usuario;
    private static GestionCliente gestionCliente;
    private static ObservableList<String> usuarios = FXCollections.observableArrayList();
    @Getter
    private static DatagramSocket server;
    @Getter
    private static InetAddress direccion;
    @Getter
    private static int PUERTO_SERVIDOR = 6000;
    @Getter
    private static VBox vBoxChat;


    @Override
    public void start(Stage stage) throws IOException {
        conexionServidor();
        mostrarInicioSesion();
        //mostrar ventana chat
        if (usuario != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("general-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 800);

            stage.setTitle("ChatGrupal - @"+usuario.getNombreUsuario());
            stage.setScene(scene);
            stage.show();
            //Cerrar hilo antes de terminar la ventana del cliente
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    gestionCliente.terminarProceso();

                    System.out.println("terminarProcesoHandler");
                }
            });
            vBoxChat = ((ChatController)fxmlLoader.getController()).getVBoxChat();
            vBoxChat.setSpacing(3.5);
            HBox.setHgrow(vBoxChat,Priority.ALWAYS);
            vBoxChat.setFillWidth(true);
            ListView<String>  listView= ((ChatController)fxmlLoader.getController()).getUsuarios();

            listView.setItems(usuarios);
            gestionCliente = new GestionCliente(vBoxChat,usuarios,usuario);
            cargarMensajes();
            new Thread(gestionCliente).start();

        }


    }

    /**
     * Metodo encargado de crear la conexion con el socket del servidor
     */
    public void conexionServidor(){
        try {
            server = new DatagramSocket();
            server.setSoTimeout(500);
            direccion = InetAddress.getByName("localhost");

        } catch (IOException e) {
            throw new RuntimeException("Error al conectar con el servidor");
        }

    }

    public static void enviarAlServidor(Object o){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        byte[] datos;

        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            datos = baos.toByteArray();

            DatagramPacket packet = new DatagramPacket(datos, datos.length, direccion, PUERTO_SERVIDOR);
            server.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static Object recibirDelServidor(){
        byte[] datos = new byte[1024];
        DatagramPacket packet = new DatagramPacket(datos, datos.length);
        try {
            server.receive(packet);

            ByteArrayInputStream bais = new ByteArrayInputStream(datos);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }

    }

    /**
     * Metodo encargado de mostrar la ventana de inicio de sesión
     * @throws IOException
     */
    public void mostrarInicioSesion() throws IOException {
        Stage inicioSesion = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("inicio-sesion-view.fxml"));
        Scene sceneInicioSesion = new Scene(fxmlLoader.load(), 200, 200);
        inicioSesion.initModality(Modality.APPLICATION_MODAL);
        inicioSesion.setTitle("Inicio de Sesion");
        inicioSesion.setScene(sceneInicioSesion);
        inicioSesion.showAndWait();
    }

    /**
     * Metodo encargado de leer el historial de mensajes enviados por el servidor y de mostrarlo en el TextArea
     */
    private void cargarMensajes() {
        ArrayList<Mensaje> mensajes = null;
        mensajes = (ArrayList<Mensaje>) recibirDelServidor();
        for(Mensaje m : mensajes){
            //textoMensajes.appendText("\n"+m.getUsuario().getNombreUsuario()+": "+m.getContenido());
            gestionCliente.recibirMensaje(m);
        }
    }

    public static void main(String[] args) {
        launch();
    }

}