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
            // 1) Streams (texto)
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // 2) LOGIN: esperar JOIN|username
            while (true) {
                String line = in.readLine(); // bloquea hasta que llegue algo
                if (line == null) {
                    return; // cliente cerró antes de loguearse
                }

                String[] parts = line.split("\\|", 2); // TIPO|RESTO (máx 2 partes)
                String type = parts[0];
                String payload = (parts.length == 2) ? parts[1] : "";

                //Si el TYPE de mensaje no es JOIN, asi no aceptamos accion de usuarios que no estan registrados.
                if (!type.equals(Protocolo.JOIN)) {
                    // si no manda JOIN primero, lo tratamos como error
                    out.println(Protocolo.error(Protocolo.ERR_BAD_JOIN));
                    continue;
                }

                //desired = el nombre registrado. entonces este atributo sera todo el rato que lo usemos como el nombre del usuario que entro en este hilo *this*
                String desired = payload.trim();
                if (desired.isEmpty()) {
                    out.println(Protocolo.error(Protocolo.ERR_BAD_JOIN));
                    continue;
                }

                boolean registered = server.UsuarioRegistrado(desired, this);
                if (!registered) {
                    out.println(Protocolo.error(Protocolo.ERR_USERNAME_ELIGIDO));
                    continue;
                }

                // OK: ya tenemos username asignado
                this.username = desired; //Aqui vemos el usuario
                out.println(Protocolo.ok()); //Los protocolos creados anteriormente empiezan a usarse

                // (opcional) info de conexión
                String ip = socket.getInetAddress().getHostAddress();
                server.broadcast(Protocolo.info(username + " se ha conectado (" + ip + ")."));
                logger.logLine("[INFO] " + username + " se ha conectado (" + ip + ").");

                break; // salimos del login
            }

            // 3) CHAT LOOP
            String line;
            //O Manda mensaje o se desconecta
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                String type = parts[0];
                String payload = (parts.length == 2) ? parts[1] : "";

                if (type.equals(Protocolo.MSG)) {
                    String text = payload; // no trim para no perder espacios internos
                    // Broadcast: MSG|user|text
                    server.broadcast(Protocolo.serverMsg(username, text));
                    // Log: "user : text"
                    logger.logLine(username + " : " + text);

                } else if (type.equals(Protocolo.LEAVE)) {
                    break; // salida limpia
                } else {
                    // Mensaje desconocido (opcional)
                    out.println(Protocolo.error("UNKNOWN_TYPE"));
                }
            }

        } catch (IOException e) {
            // Aquí normalmente cae cuando el cliente se desconecta “a lo bruto”
        } finally {
            // 4) LIMPIEZA SIEMPRE
            try {
                socket.close();
            } catch (IOException ignored) {
            }

            if (username != null) {
                server.removeUsuario(username);
                server.broadcast(Protocolo.info(username + " ha salido del chat."));
                logger.logLine("[INFO] " + username + " ha salido del chat.");
            }
        }
    }

    /**
     * Esto lo usa ChatServer en broadcast() para enviar una línea a este cliente.
     */
    public void send(String line) {
        if (out != null) {
            out.println(line);
        }
    }
}
