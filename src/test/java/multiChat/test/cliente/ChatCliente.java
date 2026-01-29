package multiChat.test.cliente;

import multiChat.test.common.Protocolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatCliente {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Conecta con el servidor.
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);

        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
        );

        out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true
        );
    }

    /**
     * Envía petición de login: JOIN|username
     */
    public void sendJoin(String username) {
        out.println(Protocolo.join(username));
    }

    /**
     * Envía un mensaje de chat: MSG|texto
     */
    public void sendMessage(String text) {
        out.println(Protocolo.msg(text));
    }

    /**
     * Envía salida limpia: LEAVE|
     */
    public void sendLeave() {
        out.println(Protocolo.leave());
    }

    /**
     * Devuelve el lector para que el ServerListener escuche al servidor.
     */
    public BufferedReader getReader() {
        return in;
    }

    /**
     * Cierra la conexión.
     */
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
