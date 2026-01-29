package multiChat.test.server;

import multiChat.test.common.NetUtils;
import multiChat.test.common.Protocolo;

public class ChatServerMain {

    public static void main(String[] args) {

        System.out.println(
                "Servidor arrancado... Esperando conexiones (puerto " + Protocolo.PORT + ")"
        );
        System.out.println("IP Local: " + NetUtils.localIp());

        // Logger del servidor
        ServerLogger logger = new ServerLogger("ChatServer_log.txt");

        // Crear servidor
        ChatServer server = new ChatServer(Protocolo.PORT, logger);

        // Arrancar bucle de escucha (accept)
        server.start();
    }
}
