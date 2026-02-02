package multiChat.test.cliente;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import multiChat.test.cliente.ChatCliente;
import multiChat.test.cliente.ServerListener;
import multiChat.test.common.Protocolo;

public class ChatClienteGUI extends JFrame {

    private ChatCliente cliente;
    private int ancho = 600;
    private int alto = 750;

    private JPanel chatContainer;
    private JTextField inputField;
    private JScrollPane scrollPane;
    private String miNombre;

    private BufferedImage imgColaRight, imgColaLeft, fondo, marco, im;
    private BufferedImage[] avatars = new BufferedImage[6]; // 0-4 users, 5 sistema
    private int avatarIndex = 0;

    private JButton[] avatarButtons = new JButton[4];
    private BufferedImage[] avatarsButtonsOff = new BufferedImage[6];
    private BufferedImage[] avatarsButtonsOn = new BufferedImage[6];

    private Map<String, Integer> userAvatars = new HashMap<>();
    private Boolean lastMsgWasMe = null;

    private final Color P5_RED = new Color(208, 0, 1);
    private final Color P5_BLACK = new Color(0, 0, 0);
    private final Color P5_WHITE = new Color(245, 245, 245);

    public ChatClienteGUI(String username) {
        cargarRecursos();
        this.miNombre = username;
        conectarServidor();

        setTitle("Instant Message");
        setSize(ancho, alto);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLayeredPane layeredPane = new JLayeredPane();

        // CAPA 1: FONDO
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (fondo != null) g.drawImage(fondo, 0, 0, ancho, alto - 39, null);
            }
        };
        backgroundPanel.setBounds(0, 0, ancho, alto);

        // CAPA 2: CHAT Y INPUT
        JPanel contentPanel = createContentPanel();

        // CAPA 3: MARCO (Encima de todo)
        JPanel frameOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (marco != null) g.drawImage(marco, 0, 0, ancho - 14, alto - 37, null);
            }
        };
        frameOverlay.setBounds(0, 0, ancho, alto);
        frameOverlay.setOpaque(false);
        frameOverlay.setFocusable(false);

        // CAPA 4: HUD (RECTÁNGULO ROJO + LOGO IM + 4 BOTONES)
        JPanel hudPanel = createHUD();

        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(contentPanel, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(frameOverlay, JLayeredPane.DRAG_LAYER);
        layeredPane.add(hudPanel, JLayeredPane.MODAL_LAYER);

        add(layeredPane);
    }

    private void conectarServidor() {
        try {
            cliente = new ChatCliente();
            cliente.connect("localhost", Protocolo.PORT);
            cliente.sendJoin(miNombre);
            new Thread(new ServerListener(cliente.getReader(), this)).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error de conexión", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private JPanel createHUD() {
        JPanel hud = new JPanel(null);
        hud.setOpaque(false);
        hud.setBounds(0, 0, ancho, 145);

        // Rectángulo rojo
        JPanel redRect = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(P5_RED);
                g.fillRect(0, 0, getWidth(), 70);
            }
        };
        redRect.setBounds(0, 0, ancho, 125);

        // Logo IM (Ya no es botón, solo decoración)
        JLabel imLabel = new JLabel();
        if (im != null) imLabel.setIcon(new ImageIcon(im.getScaledInstance(125, 125, Image.SCALE_SMOOTH)));
        imLabel.setBounds(25, 0, 150, 150);

        int startX = 140;
        for (int i = 0; i < 4; i++) {
            final int index = i;
            avatarButtons[i] = new JButton();
            avatarButtons[i].setBounds(startX + (i * 80), 20, 125, 125);

            // Hacer el botón invisible para que solo se vea tu imagen
            avatarButtons[i].setContentAreaFilled(false);
            avatarButtons[i].setBorderPainted(false);
            avatarButtons[i].setFocusPainted(false);

            avatarButtons[i].addActionListener(e -> {
                avatarIndex = index;
                userAvatars.put(miNombre, avatarIndex);
                cliente.sendAvatar(avatarIndex);
                actualizarImagenesBotones();
                reproducirSonido("a.wav");
                chatContainer.repaint();
            });

            hud.add(avatarButtons[i]);
        }

        actualizarImagenesBotones(); // Estado inicial
        hud.add(imLabel);
        hud.add(redRect);
        return hud;
    }

    private void actualizarImagenesBotones() {
        for (int i = 0; i < 4; i++) {
            BufferedImage imgEstado;
            if (i == avatarIndex) {
                imgEstado = avatarsButtonsOn[i]; // Tu recurso "ON"
            } else {
                imgEstado = avatarsButtonsOff[i]; // Tu recurso "OFF"
            }

            if (imgEstado != null) {
                avatarButtons[i].setIcon(new ImageIcon(imgEstado.getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
            }
        }
    }

    private JPanel createContentPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBounds(0, 0, ancho - 16, alto - 39);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));

        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setOpaque(false);
        chatContainer.setBorder(BorderFactory.createEmptyBorder(160, 20, 40, 20));

        scrollPane = new JScrollPane(chatContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new P5ScrollBarUI());

        inputField = new JTextField();
        inputField.setFont(new Font("Impact", Font.PLAIN, 18));
        inputField.setBackground(P5_WHITE);
        inputField.addActionListener(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) { cliente.sendMessage(text); inputField.setText(""); reproducirSonido("select.wav"); }
        });

        JPanel inputWrap = new JPanel(new BorderLayout());
        inputWrap.setOpaque(false);
        inputWrap.setBorder(BorderFactory.createEmptyBorder(20, 30, 40, 30));
        inputWrap.add(inputField);

        p.add(scrollPane, BorderLayout.CENTER);
        p.add(inputWrap, BorderLayout.SOUTH);
        return p;
    }

    //chatgpteada, es fácil de hacer pero me dio toda la putisima pereza
    private void reproducirSonido(String nombreArchivo) {
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

    // si el OTRO envia mensaje, suena un sonido (esto era para hacerlos distintos, pero al final lo dejo igual)
    public void onChatMessage(String user, String text) {
        if (!user.equals(miNombre)) {
            reproducirSonido("select.wav");
        }
        addMessage(user, text, user.equals(miNombre));
    }
    public void onAvatarChanged(String user, int index) {
        userAvatars.put(user, index); chatContainer.repaint();
    }

    //estos 3 tienen el logo distinto
    public void onSystemMessage(String text) {
        addMessage("Sistema", text, false);
    }
    public void onError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error del Servidor", JOptionPane.ERROR_MESSAGE);
    }
    public void onDisconnected() {
        JOptionPane.showMessageDialog(this, "Conexión perdida.", "Desconectado", JOptionPane.WARNING_MESSAGE);
    }

    private void cargarRecursos() {
        try {
            String base = "src/res/";
            imgColaRight = ImageIO.read(new File(base + "colaBlanca.png"));
            imgColaLeft = ImageIO.read(new File(base + "colaNegra.png"));
            fondo = ImageIO.read(new File(base + "background.png"));
            marco = ImageIO.read(new File(base + "marco.png"));
            im = ImageIO.read(new File(base + "IM.png"));
            for (int i = 0; i < 5; i++) {
                avatars[i] = ImageIO.read(new File(base + "photos/" + (i + 1) + ".png"));
            }
            for (int i = 0; i < 4; i++) {
                avatarsButtonsOff[i] = ImageIO.read(new File(base + "im/off" + (i + 1) + ".png"));
            }
            for (int i = 0; i < 4; i++) {
                avatarsButtonsOn[i] = ImageIO.read(new File(base + "im/on" + (i + 1) + ".png"));
            }
        } catch (IOException e) { System.err.println("Error recursos: " + e.getMessage()); }
    }

    public void addMessage(String user, String text, boolean isMe) {
        JPanel row = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(1000, 130));

        int idx = user.equals("Sistema") ? 4 : userAvatars.getOrDefault(user, 0);

        P5Bubble bubble = new P5Bubble(text, isMe, lastMsgWasMe);
        AvatarPanel avatar = new AvatarPanel(isMe, idx);

        if (isMe) { row.add(bubble); row.add(avatar); }
        else { row.add(avatar); row.add(bubble); }

        chatContainer.add(row);
        chatContainer.add(Box.createRigidArea(new Dimension(0, -40)));
        lastMsgWasMe = isMe;
        chatContainer.revalidate();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }


    class AvatarPanel extends JPanel {
        private boolean isMe; private int currentIdx; private float opacity = 0.0f, scale = 0.7f;
        public AvatarPanel(boolean isMe, int currentIdx) {
            this.isMe = isMe; this.currentIdx = currentIdx;
            setPreferredSize(new Dimension(125, 125)); setOpaque(false);
            new Timer(15, e -> {
                opacity += 0.11f; scale += 0.035f;
                if (opacity >= 1.0f) { opacity = 1.0f; scale = 1.0f; ((Timer) e.getSource()).stop(); }
                repaint();
            }).start();
        }
        @Override protected void paintComponent(Graphics g) {
            if (avatars[currentIdx] == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            if (isMe) g2.translate(getWidth() * (1 - scale), 0);
            g2.scale(scale, scale);
            if (isMe) { g2.translate(125, 0); g2.scale(-1, 1); }
            g2.drawImage(avatars[currentIdx], 0, 25, 125, 125, null);
            g2.dispose();
        }
    }


    //chatgpteada
    class P5ScrollBarUI extends BasicScrollBarUI {
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle rb) {}
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle tb) {
            if (tb.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            int x = tb.x, y = tb.y, w = tb.width, h = tb.height;
            Path2D.Double white = new Path2D.Double();
            white.moveTo(x - 2, y); white.lineTo(x + w + 2, y + 5); white.lineTo(x + w, y + h); white.lineTo(x - 4, y + h - 5); white.closePath();
            g2.setColor(Color.WHITE); g2.fill(white);
            Path2D.Double black = new Path2D.Double();
            black.moveTo(x, y + 2); black.lineTo(x + w, y); black.lineTo(x + w - 2, y + h - 2); black.lineTo(x + 2, y + h); black.closePath();
            g2.setColor(Color.BLACK); g2.fill(black); g2.dispose();
        }
        @Override protected JButton createDecreaseButton(int o) { return createHiddenButton(); }
        @Override protected JButton createIncreaseButton(int o) { return createHiddenButton(); }
        private JButton createHiddenButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
    }

    class P5Bubble extends JPanel {
        private String text; private boolean isMe; private Boolean prevWasMe; private float opacity = 0.0f, scale = 0.7f;
        public P5Bubble(String text, boolean isMe, Boolean prevWasMe) {
            this.text = text; this.isMe = isMe; this.prevWasMe = prevWasMe;
            setOpaque(false); setPreferredSize(new Dimension(360, 130));
            new Timer(15, e -> {
                opacity += 0.1f; scale += 0.03f;
                if (opacity >= 1.0f) { opacity = 1.0f; scale = 1.0f; ((Timer) e.getSource()).stop(); }
                repaint();
            }).start();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            if (isMe) g2.translate(getWidth() * (1 - scale), 0);
            g2.scale(scale, scale);
            int bubbleW = 260, bubbleH = 50, xBase = isMe ? 70 : 30, yBase = 60;
            if (prevWasMe != null) {
                g2.setColor(Color.BLACK); Path2D.Double link = new Path2D.Double();
                if (prevWasMe && isMe) { link.moveTo(xBase + bubbleW - 160, yBase); link.lineTo(xBase + bubbleW - 120, yBase - 50); link.lineTo(xBase + bubbleW - 150, yBase - 50); link.lineTo(xBase + bubbleW - 190, yBase); }
                else if (!prevWasMe && isMe) { link.moveTo(xBase + bubbleW - 60, yBase); link.lineTo(xBase + bubbleW - 20, yBase - 50); link.lineTo(xBase + bubbleW - 50, yBase - 50); link.lineTo(xBase + bubbleW - 90, yBase); }
                else if (!prevWasMe && !isMe) { link.moveTo(xBase + 160 + 50, yBase); link.lineTo(xBase + 120 + 50, yBase - 50); link.lineTo(xBase + 150 + 50, yBase - 50); link.lineTo(xBase + 190 + 50, yBase); }
                else if (prevWasMe && !isMe) { link.moveTo(xBase + 60, yBase); link.lineTo(xBase + 20, yBase - 50); link.lineTo(xBase + 50, yBase - 50); link.lineTo(xBase + 90, yBase); }
                link.closePath(); g2.fill(link);
            }
            Path2D.Double border = new Path2D.Double();
            border.moveTo(xBase + 5, yBase - 8); border.lineTo(xBase + bubbleW - 5, yBase - 7); border.lineTo(xBase + bubbleW + 12, yBase + bubbleH + 4); border.lineTo(xBase - 8, yBase + bubbleH + 5); border.closePath();
            g2.setColor(isMe ? Color.BLACK : Color.WHITE); g2.fill(border);
            Path2D.Double body = new Path2D.Double();
            body.moveTo(xBase + 12, yBase); body.lineTo(xBase + bubbleW - 8, yBase - 4); body.lineTo(xBase + bubbleW, yBase + bubbleH - 6); body.lineTo(xBase, yBase + bubbleH); body.closePath();
            g2.setColor(isMe ? P5_WHITE : P5_BLACK); g2.fill(body);
            BufferedImage img = isMe ? imgColaRight : imgColaLeft;
            if (img != null) { int xCola = isMe ? (xBase + bubbleW - 8) : (xBase - 20); g2.drawImage(img, xCola - 15, yBase - 13, 60, 70, null); }
            g2.setColor(isMe ? Color.BLACK : P5_WHITE); g2.setFont(new Font("Impact", Font.PLAIN, 16)); g2.drawString(text, xBase + 20, yBase + 32); g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClienteGUI("Joker").setVisible(true));
    }
}
