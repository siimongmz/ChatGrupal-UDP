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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.Setter;

public class ChatApplication extends Application {
    @Getter @Setter
    private static Usuario usuario;
    private static GestionCliente gestionCliente;
    private static ObservableList<String> usuarios = FXCollections.observableArrayList();
    @Getter
    private static Socket server;
    @Getter
    private static VBox vBoxChat;
    @Getter
    private static ObjectInputStream entrada;
    @Getter
    private static ObjectOutputStream salida;


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
            server = new Socket("localhost",6000);
            salida = new ObjectOutputStream(server.getOutputStream());
            entrada = new ObjectInputStream(server.getInputStream());

        } catch (IOException e) {
            throw new RuntimeException("Error al conectar con el servidor");
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
        try {
            mensajes = (ArrayList<Mensaje>) entrada.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for(Mensaje m : mensajes){
            //textoMensajes.appendText("\n"+m.getUsuario().getNombreUsuario()+": "+m.getContenido());
            gestionCliente.recibirMensaje(m);
        }
    }

    public static void main(String[] args) {
        launch();
    }

}