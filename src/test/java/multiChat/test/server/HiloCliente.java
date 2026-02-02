package multiChat.test.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import multiChat.test.common.Protocolo;
import java.io.IOException;

public class HiloCliente extends Thread {
    private final Socket socket;
    private final ChatServer server;
    private final ServerLogger logger;

    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public HiloCliente(Socket socket, ChatServer server, ServerLogger logger) {
        this.socket = socket;
        this.server = server;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            while (true) {
                String line = in.readLine();
                if (line == null) return;

                String[] parts = line.split("\\|", 2);
                String type = parts[0];
                String payload = (parts.length == 2) ? parts[1] : "";

                if (!type.equals(Protocolo.JOIN)) {
                    out.println(Protocolo.error(Protocolo.ERR_BAD_JOIN));
                    continue;
                }

                String desired = payload.trim();
                if (desired.isEmpty() || !server.UsuarioRegistrado(desired, this)) {
                    out.println(Protocolo.error(Protocolo.ERR_USERNAME_ELIGIDO));
                    continue;
                }

                this.username = desired;
                out.println(Protocolo.ok());

                String ip = socket.getInetAddress().getHostAddress();
                server.broadcast(Protocolo.info(username + " se ha conectado."));
                logger.logLine("[INFO] " + username + " conectado desde " + ip);
                break;
            }


            String line;
<<<<<<<
=======
>>>>>>>
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                String type = parts[0];
                String payload = (parts.length == 2) ? parts[1] : "";

                if (type.equals(Protocolo.MSG)) {
                    server.broadcast(Protocolo.serverMsg(username, payload));
                    logger.logLine(username + " : " + payload);

                } else if (type.equals(Protocolo.AVATAR)) {
                    try {
                        int avatarIdx = Integer.parseInt(payload);
                        server.broadcast(Protocolo.serverAvatar(username, avatarIdx));
                        logger.logLine("[AVATAR] " + username + " cambió al índice " + avatarIdx);
                    } catch (NumberFormatException e) {
                        out.println(Protocolo.error("SERVER LISTENER o GUI"));
                    }

                } else if (type.equals(Protocolo.LEAVE)) {
                    break;
                }
            }

        } catch (IOException e) {
        } finally {
            limpiarConexion();
        }
    }

    private void limpiarConexion() {
        try { socket.close(); } catch (IOException ignored) {}
        if (username != null) {
            server.removeUsuario(username);
            server.broadcast(Protocolo.info(username + " ha salido del chat."));
            logger.logLine("[INFO] " + username + " ha salido.");
        }
    }

    public void send(String line) {
        if (out != null) out.println(line);
    }
}