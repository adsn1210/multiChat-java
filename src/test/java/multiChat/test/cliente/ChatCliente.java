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


    public void sendAvatar(int index) {
        out.println(Protocolo.avatar(index));
    }

    public void sendJoin(String username) {
        out.println(Protocolo.join(username));
    }

    public void sendMessage(String text) {
        out.println(Protocolo.msg(text));
    }

    public void sendLeave() {
        out.println(Protocolo.leave());
    }

    public BufferedReader getReader() {
        return in;
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
