package multiChat.test.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//Clase que se ocupa de guardar todo lo que ocurre en el chat.
public class ServerLogger {
    private final String fileName; //chatServer_logger.txt
    private final DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"); //Fecha y hora de log

    public ServerLogger(String fileName) {
        this.fileName = fileName;
    }

    //Debe ser Synchronized ya que seran hilos diferentes los que entraran con datos y si entran a la vez podra haber fallos de escritura.ClientHandler
    public synchronized void logLine(String line) {
        String fechaYHora = LocalDateTime.now().format(formato);
        //bw es un recurso que tiene implementado closable(). Cuando salga de este bloque (), llama automáticamente a bw.close(). Usamos el try-with-resources
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) //Necesita ser true para no borrar lo ya escrito
        {
            bw.write("En " + fechaYHora);
            bw.write(line);
            bw.newLine();
            bw.newLine();
            bw.write("===============");
        } catch (IOException ex) { //Si algo Falla lanzara IOException easyy
            System.err.println("Error de escritura en log: " + ex.getMessage());
        }
    }
}


