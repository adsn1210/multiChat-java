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
    //Cuando el Servidor->Clientes, hace un envio del mensaje que a enviado otro usuario necesitamos poner la informacion de TIPO|USUARIO|Contenido
    public static String serverMsg(String user, String text) {
        return MSG + SEP + user + SEP + text;
    }
    /*Esto significa que "serverMsg" deberemos usarlo luego en la funcion del servidor que haga el envio a todos los demas
    usuarios cuando necesiten recibir un mensaje. Recorrera todos los usuarios activos y les mandara el mensaje.
    #El servidor es el "Repartidor" ya que el cliente no conoce los demas usuarios ONLINE y si tuviera que hacerlo seria bastanet complicado de configurar.
    # Tener en cuenta tambien -> line.split("\\|", 3); ->split(regex, limit), en este caso el Limite seran 3 como bien dije antes (TIPO|USUARIO|Contenido).
          */
}

