package com.example.berkleysocketclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private Button botaoEnviar;
    @FXML
    private TextArea campoMensagem;
    @FXML
    private VBox vboxMensagens;
    @FXML
    private ScrollPane scrollMensagens;

    private Client client;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarInterface();
        try {
            client = new Client(new Socket("localhost", 1234), this);
            client.iniciarEscutaMensagens();
        } catch (IOException e) {
            mostrarErro("Falha ao conectar ao servidor: " + e.getMessage());
        }
    }

    private void configurarInterface() {
        // Auto-scroll sempre que o VBox de mensagens alterar sua altura
        vboxMensagens.heightProperty().addListener((obs, oldVal, newVal) ->
                scrollMensagens.setVvalue(1.0)
        );

        // Envio de mensagem ao pressionar ENTER
        campoMensagem.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                enviarMensagem();
            }
        });

        // Envio da mensagem ao clicar no botão
        botaoEnviar.setOnAction(event -> enviarMensagem());
    }

    private void enviarMensagem() {
        String mensagem = campoMensagem.getText().trim();
        if (mensagem.isEmpty()) {
            return;
        }
        // Exibe a mensagem enviada pelo usuário (lado direito)
        adicionarMensagem(mensagem, Pos.CENTER_RIGHT, "bubble-right");

        // Envia a mensagem para o servidor (caso o client esteja conectado)
        if (client != null) {
            client.enviarMensagem(mensagem);
        }

        campoMensagem.clear();
        campoMensagem.setPrefHeight(40);
    }

    public void adicionarMensagem(String mensagem, Pos posicao, String estilo) {
        Platform.runLater(() -> {
            // Container que agrupa a mensagem e o rótulo
            HBox container = new HBox();
            container.setPadding(new Insets(5, 10, 15, 10));
            container.setAlignment(posicao);

            // Rótulo com o nome do remetente
            Label lblSender = new Label();
            if (posicao == Pos.CENTER_RIGHT) {
                lblSender.setText("Você");
                lblSender.getStyleClass().addAll("sender-label", "sender-client");
            } else {
                lblSender.setText("Suporte");
                lblSender.getStyleClass().addAll("sender-label", "sender-server");
            }

            // Cria o conteúdo da mensagem
            Text texto = new Text(mensagem);
            texto.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
            TextFlow textFlow = new TextFlow(texto);
            // Adiciona a classe CSS (sem estilo inline)
            textFlow.getStyleClass().add(estilo);
            textFlow.maxWidthProperty().bind(scrollMensagens.widthProperty().multiply(0.75));
            textFlow.setPadding(new Insets(8));

            // Agrupa o remetente e a mensagem
            VBox messageBox = new VBox(2, lblSender, textFlow);
            messageBox.setAlignment(posicao);

            container.getChildren().add(messageBox);
            vboxMensagens.getChildren().add(container);
        });
    }

    public void mostrarErro(String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro no Cliente");
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.showAndWait();
        });
    }
}
