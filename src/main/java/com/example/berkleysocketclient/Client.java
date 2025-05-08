package com.example.berkleysocketclient;

import javafx.geometry.Pos;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedReader entrada;
    private BufferedWriter saida;
    private Controller controller;

    // Removemos a variável ultimaMensagemRecebida, pois não será mais necessária
    // private String ultimaMensagemRecebida = "";

    public Client(Socket socket, Controller controller) {
        try {
            this.socket = socket;
            this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.saida = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.controller = controller;
        } catch (IOException e) {
            fecharTudo();
        }
    }

    public void iniciarEscutaMensagens() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String mensagem = entrada.readLine();
                    if (mensagem == null)
                        break;

                    System.out.println("[DEBUG] Servidor enviou: " + mensagem);

                    // Exibe a mensagem recebida (lado esquerdo – balão do suporte)
                    controller.adicionarMensagem(mensagem, Pos.CENTER_LEFT, "bubble-left");

                } catch (IOException e) {
                    controller.mostrarErro("Conexão com servidor perdida");
                    fecharTudo();
                    break;
                }
            }
        }).start();
    }

    public void enviarMensagem(String mensagem) {
        try {
            System.out.println("[DEBUG] Enviando para o servidor: " + mensagem);
            saida.write(mensagem);
            saida.newLine();
            saida.flush();
        } catch (IOException e) {
            controller.mostrarErro("Erro ao enviar mensagem");
            fecharTudo();
        }
    }

    public void fecharTudo() {
        try {
            if (entrada != null)
                entrada.close();
            if (saida != null)
                saida.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
