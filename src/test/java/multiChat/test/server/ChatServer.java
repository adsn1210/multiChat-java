package multiChat.test.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ChatServer {
    private final int port;
    private final ServerLogger logger;

    //Almacena datos en Clave-valor, Su nombre y Su Hilo,Concurrent
    private final Map<String, HiloCliente> clients = new ConcurrentHashMap<>();//Se crea el mapa pero de tipo Concurrente

    public ChatServer(int port, ServerLogger logger) {
        this.port = port;
        this.logger = logger;
    }                           /*Race Condition se resuelve con el ConcurrentHashMap ya que lo resuelve con la Atomicidad del
                                    putIfAbsent. Con hashMap no es como Synchronized, es mas eficiente, Y escalable.
                                                                          */


    public void start() {
        try(ServerSocket ss = new ServerSocket(port)){
            //Bucle infinito
            while(true){
                Socket clientSocket = ss.accept();//Esperando hasta que alguien entre

                HiloCliente hilo = new HiloCliente(clientSocket,this,logger);
                hilo.start();//arranca el hilo
            }
        } catch (IOException e) {
            System.err.println("Error al arranca el servidor, puerto: " + port + e.getMessage());
        }
    }

    //Metodos

    //La joya de la corona.
    public boolean UsuarioRegistrado(String username, HiloCliente hilo) {
        return clients.putIfAbsent(username, hilo) == null;

    }
    //Al ser ConcurrentHashMap,no hace falta Synchronized
    public void removeUsuario(String username){
        if(username !=null) clients.remove(username);
    }

    public void broadcast(String line){

        for (HiloCliente ch : clients.values()){
            ch.send(line);
        }
    }
}
