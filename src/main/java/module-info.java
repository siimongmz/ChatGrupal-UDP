module com.chat.chatgrupal {
    requires javafx.controls;
    requires javafx.fxml;
            
        requires org.controlsfx.controls;
    requires static lombok;

    opens com.chat.chatgrupal to javafx.fxml;
    exports com.chat.chatgrupal;
}