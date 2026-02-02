package multiChat.test.cliente;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.SwingUtilities;
import java.io.File;

public class ChatClienteMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            //sonido al abrir el main
            reproducirSonido("a.wav");
            Login login = new Login();
            login.setVisible(true);
            String username = login.getUsername();

            if (username == null || username.isEmpty()) { System.exit(0);}

            ChatClienteGUI gui = new ChatClienteGUI(username);
            gui.setVisible(true);

            //sonido al entrar al chat
            reproducirSonido("noti.wav");
        });
    }

    //copypaste del GUI
    private static void reproducirSonido(String nombreArchivo) {
        try {
            File file = new File("src/res/sfx/" + nombreArchivo);
            if (!file.exists()) return;

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido: " + e.getMessage());
        }
    }
}
