package multiChat.test.common;


import java.net.InetAddress;
import java.net.UnknownHostException;

public final class NetUtils {

    //Normalize.
    public static String normalizeHost(String host){
        if(host == null)return "";//Para que trim no pete devolvemos un "" vacio si el host llega a quedarse como null.
        return host.trim();//El metodo trim() se utiliza para eliminar los espacion en blanco.
    }
    //La IP del Servidor: "Servidor con IP Local ....
    public static String localIp(){
        try{
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException  e) {
            return "UnknownHostException";
        }
    }
    //Para ver la IP del cliente conectado.
    public static String safeIp(InetAddress adres){

        if (adres == null)return "Unknow";
        return adres.getHostAddress();
    } //Donde usarlo : InetAddress adres = socket.getInetAddress(); -> IP del cliente
}
