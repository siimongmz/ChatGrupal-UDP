package com.chat.chatgrupal;

import static com.chat.chatgrupal.ChatApplication.*;
import com.util.Mensaje;
import com.util.Usuario;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import lombok.Getter;



public class ChatController {

    @FXML @Getter
    public VBox vBoxChat;
    @FXML
    public TextArea textAreaEnvio;
    @FXML @Getter
    private ListView<String> usuarios;

    @FXML
    private TextField textoUsuario;

    /**
     * Metodo encargado de gestionar el inicio de sesion, enviara el nombre de usuario escrito al servidor y mostrará una alerta en caso de que no sea valido y continuará con la ejecucion asignando el nuevo usuario al cliente en caso de que si
     */
    @FXML
    protected void comprobarInicioSesion(){
        Usuario usuario = new Usuario(textoUsuario.getText());
        //Enviamos el objeto al servidor
        enviarAlServidor(usuario);
        if ((Integer)recibirDelServidor()==1) {
            ChatApplication.setUsuario(usuario);
            Stage stage = (Stage) textoUsuario.getScene().getWindow();
            stage.close();
        } else {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null); // Puedes establecer un encabezado si lo deseas
            alerta.setContentText("El Usuario ya existe");

            alerta.showAndWait();
        }


    }

    /**
     * Metodo encargado de enviar un mensaje al servidor para que este se encarge de gestionarlo, borrara el texto escrito en el TextArea de input y se ejecutará al presionar el boton de envio
     */
    @FXML
    protected void enviarMensaje(){

        enviarAlServidor(new Mensaje(textAreaEnvio.getText(),ChatApplication.getUsuario()));
        textAreaEnvio.setText("");
    }



}