package multiChat.test.server;

import multiChat.test.common.NetUtils;
import multiChat.test.common.Protocolo;

import java.sql.SQLOutput;

public class ChatServerMain {
    public static void main(String[] args) {
        System.out.println("Servidor arrancado....Esperando conexiones (puerto ("+(Protocolo.PORT +")"));
        System.out.println("IP Local "+(NetUtils.localIp()));

        ServerLogger logger = new ServerLogger("ChatServer_log.txt");

        //Creamos servidor
        ChatServer server = new ChatServer(Protocolo.PORT,logger);

        //Bucle de Escucha
        server.start();
    }
}
