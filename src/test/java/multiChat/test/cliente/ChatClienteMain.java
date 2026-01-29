package multiChat.test.cliente;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ChatClienteMain {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            // Pedir nombre de usuario
            String username = JOptionPane.showInputDialog(
                    null,
                    "Introduce tu nombre de usuario:",
                    "Login",
                    JOptionPane.PLAIN_MESSAGE
            );

            // Si cancela o deja vacío, no arrancamos
            if (username == null || username.trim().isEmpty()) {
                System.exit(0);
            }

            // Crear y mostrar la GUI
            ChatClienteGUI gui = new ChatClienteGUI(username.trim());
            gui.setVisible(true);
        });
    }
}
