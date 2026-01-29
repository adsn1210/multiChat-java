package multiChat.test.cliente;

import multiChat.test.common.Protocolo;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import multiChat.test.cliente.ChatClienteGUI;

public class ServerListener implements Runnable {

    private final BufferedReader in;
    private  ChatClienteGUI gui;

    public ServerListener(BufferedReader in, ChatClienteGUI gui) {
        this.in = in;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            String line;

            // Mientras el servidor siga enviando mensajes
            while ((line = in.readLine()) != null) {

                if (line.startsWith(Protocolo.OK)) {
                    continue;
                }
                // Parseo del protocolo
                if (line.startsWith(Protocolo.MSG + "|")) {
                    // MSG|user|text
                    String[] parts = line.split("\\|", 3);
                    String user = parts[1];
                    String text = parts[2];

                    SwingUtilities.invokeLater(() ->
                            gui.onChatMessage(user, text)
                    );

                } else if (line.startsWith(Protocolo.INFO + "|")) {
                    // INFO|text
                    String info = line.substring((Protocolo.INFO + "|").length());

                    SwingUtilities.invokeLater(() ->
                            gui.onSystemMessage(info)
                    );

                } else if (line.startsWith(Protocolo.ERROR + "|")) {
                    // ERROR|code
                    String error = line.substring((Protocolo.ERROR + "|").length());

                    SwingUtilities.invokeLater(() ->
                            gui.onError(error)
                    );

                } else {
                    // Mensaje desconocido
                    SwingUtilities.invokeLater(() ->
                            gui.onDisconnected()
                    );
                }
            }

        } catch (IOException e) {
            // El servidor se ha caído o la conexión se cerró
            SwingUtilities.invokeLater(gui::onDisconnected);
        }
    }
}
