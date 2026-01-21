package multiChat.test.common;

//Esta clase es un Acuerdo entre el Cliente y el Servidor para que se comuniquen de una forma ordenada.
//Nos ayudara a entender los mensajes lanzados por consolas de una forma clara y estructurada.
public final class Protocolo {

    //Puerto del servidor(Cliente y Server)
    public static final int PORT = 5000;
    /* Nota:Usamos la conexion TCP (capa de transporte Modelo OSI)
        con esto decimos que el puerto de Destino para los clientes sera el -> 5000, pero el de origen no tiene porque.
         */

    //Un separador para continuar con la estructura clara de las notificaciones.
    public static final String SEP = "|";

    //Tipos de mensaje Cliente -> Servidor
    public static final String JOIN = "Conexion de: ";
    public static final String MSG = "Mensaje:";
    public static final String LEAVE = "Desconexion de: ";

    //Tipos de respuesta Servidor->Cliente
    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String INFO = "INFO";

    //Codigos de Error explicitos
    public static final String ERR_USERNAME_ELIGIDO = "Usuario_Ya_En_Uso";
    public static final String ERR_BAD_JOIN = "FALLO_CONEXION";

    // Helpers para construir mensajes (opcional, pero evita bugs)Limpio y legible
    public static String join(String username) {
        return JOIN + SEP + username;  //Uso ->out.println(Protocol.join(username));
    }

    public static String msg(String text) {
        return MSG + SEP + text;
    }

    public static String leave() {
        return LEAVE + SEP;
    }

    public static String ok() {
        return OK + SEP;
    }

    public static String error(String code) {
        return ERROR + SEP + code;
    }

    public static String info(String text) {
        return INFO + SEP + text;
    }

    // Mensaje broadcast del servidor: MSG|user|text
    public static String serverMsg(String user, String text) {
        return MSG + SEP + user + SEP + text;
    }
}
