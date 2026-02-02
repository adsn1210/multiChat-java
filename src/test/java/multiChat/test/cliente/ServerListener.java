package multiChat.test.cliente;

import multiChat.test.common.Protocolo;
import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;

public class ServerListener implements Runnable {

    private final BufferedReader in;
    private ChatClienteGUI gui;

    public ServerListener(BufferedReader in, ChatClienteGUI gui) {
        this.in = in;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            String line;

            while ((line = in.readLine()) != null) {

                if (line.startsWith(Protocolo.OK)) {
                    continue;
                }

                // --- 1. PROCESAR MENSAJES DE CHAT ---
                if (line.startsWith(Protocolo.MSG + "|")) {
                    String[] parts = line.split("\\|", 3);
                    if (parts.length == 3) {
                        String user = parts[1];
                        String text = parts[2];
                        SwingUtilities.invokeLater(() -> gui.onChatMessage(user, text));
                    }
                }

                // esto es pa procesar el avatar de cada uno
                else if (line.startsWith(Protocolo.AVATAR + "|")) {
                    String[] parts = line.split("\\|", 3);

                    if (parts.length == 3) {
                        String user = parts[1]; int avatarIndex = Integer.parseInt(parts[2]);
                        SwingUtilities.invokeLater(() -> gui.onAvatarChanged(user, avatarIndex));
                    }
                }

                else if (line.startsWith(Protocolo.INFO + "|")) {
                    String info = line.substring((Protocolo.INFO + "|").length());
                    SwingUtilities.invokeLater(() -> gui.onSystemMessage(info));
                }

                else if (line.startsWith(Protocolo.ERROR + "|")) {
                    String error = line.substring((Protocolo.ERROR + "|").length());
                    SwingUtilities.invokeLater(() -> gui.onError(error));
                }

                else {
                    System.out.println(line);
                }
            }

        } catch (IOException e) {
            SwingUtilities.invokeLater(gui::onDisconnected);
        } catch (NumberFormatException e) {
            System.err.println("NO CARGA AVATAR - Mira ServerListener o GUI: " + e.getMessage());
        }
    }
}