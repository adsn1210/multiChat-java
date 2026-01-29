package multiChat.test.cliente;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import multiChat.test.cliente.ChatCliente;
import multiChat.test.cliente.ServerListener;
import multiChat.test.common.Protocolo;


public class ChatClienteGUI extends JFrame {

    private ChatCliente cliente;
    //dimensiones, estoy hasta los huevos de cambiar 40 parametros a la vez
    private int ancho = 600;
    private int alto = 750;

    private JPanel chatContainer;
    private JTextField inputField;
    private JScrollPane scrollPane;
    private String miNombre;

    // recursos gráficos
    private BufferedImage imgColaRight, imgColaLeft, fondo, marco, im;
    private BufferedImage[] avatars = new BufferedImage[5];
    private int avatarIndex = 0;
    private BufferedImage photoTest;

    // estado para la barra que conecta los mensajes
    private Boolean lastMsgWasMe = null;

    // Paleta de colores P5
    private final Color P5_RED = new Color(210, 17, 17);
    private final Color P5_BLACK = new Color(0, 0, 0);
    private final Color P5_WHITE = new Color(245, 245, 245);

    public ChatClienteGUI(String username) {
        cargarRecursos();

        this.miNombre = username;

        try {
            cliente = new ChatCliente();

            cliente.connect("localhost", Protocolo.PORT);
            cliente.sendJoin(miNombre);

            ServerListener listener = new ServerListener(cliente.getReader(), this);

            new Thread(listener).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo conectar al servidor",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
        setTitle("Instant Message");
        setSize(ancho, alto);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // PROFUNDIDAD, como son varias capas, hay que hacerlo así por q si no es un lío
        JLayeredPane layeredPane = new JLayeredPane();

        // CAPA 1 : FONDO
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (fondo != null) {
                    g.drawImage(fondo, 0, 0, ancho, alto - 39, this);
                }
            }
        };
        backgroundPanel.setBounds(0, 0, ancho, alto);

        // CAPA 2: CHAT Y USER INPUT
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBounds(0, 0, ancho - 16, alto - 39);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));

        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setOpaque(false);
        chatContainer.setBorder(BorderFactory.createEmptyBorder(160, 20, 40, 20));

        // SCROLLBAR (POR CAMBIAR)                                                              - !!!!!!!!!!!!!!!!!!!!!!
        scrollPane = new JScrollPane(chatContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new P5ScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));

        // USER INPUT
        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setOpaque(false);
        inputWrapper.setBorder(BorderFactory.createEmptyBorder(20, 30, 40, 30));

        inputField = new JTextField();
        inputField.setFont(new Font("Impact", Font.PLAIN, 18));
        inputField.setBackground(P5_WHITE);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 15, 0, 0, P5_BLACK),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        inputWrapper.add(inputField, BorderLayout.CENTER);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(inputWrapper, BorderLayout.SOUTH);

        // CAPA 3: MARCO (solo diseño)
        JPanel frameOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (marco != null) g.drawImage(marco, 0, 0, ancho - 14, alto - 37, this);
            }
        };
        frameOverlay.setBounds(0, 0, ancho, alto);
        frameOverlay.setOpaque(false);
        frameOverlay.setFocusable(false);

        // CAPA 4: LOGO Y RECTANGULO ROJO
        // RECTÁNGULO ROJO
        JPanel rectangleTop = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(P5_RED);
                g.fillRect(0, 0, getWidth(), 125);
            }
        };
        rectangleTop.setOpaque(false);
        rectangleTop.setBounds(0, 0, ancho, 125);

        // LOGO IM CON BOTÓN (CAMBIAR)                                                          -!!!!!!!!!!!!!!!!!!!!!!!
        JButton imButton = new JButton();
        imButton.setBounds(30, 20, 120, 120);
        imButton.setContentAreaFilled(false);
        imButton.setBorderPainted(false);
        if (im != null) imButton.setIcon(new ImageIcon(im.getScaledInstance(120, 120, Image.SCALE_SMOOTH)));

        imButton.addActionListener(e -> {
            avatarIndex = (avatarIndex + 1) % avatars.length;
            photoTest = avatars[avatarIndex];
            chatContainer.repaint();
        });

        // Aquí se mete las capas por orden
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(contentPanel, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(frameOverlay, JLayeredPane.DRAG_LAYER);
        layeredPane.add(imButton, JLayeredPane.DRAG_LAYER);
        layeredPane.add(rectangleTop, JLayeredPane.MODAL_LAYER);

        add(layeredPane);

        inputField.addActionListener(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                cliente.sendMessage(text); // 🔥 SOLO ENVÍAS
                inputField.setText("");
            }
        });


    }
    public void onChatMessage(String user, String text) {
        boolean isMe = user.equals(miNombre);
        addMessage(user, text, isMe);
    }

    public void onSystemMessage(String text) {
        addMessage("Sistema", text, false);
    }

    public void onError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void onDisconnected() {
        JOptionPane.showMessageDialog(this, "Desconectado del servidor");
    }



    private void cargarRecursos() {
        try {
            // CAMBIAR
            String base = "src/res/";
            imgColaRight = ImageIO.read(new File(base + "colaBlanca.png"));
            imgColaLeft = ImageIO.read(new File(base + "colaNegra.png"));
            fondo = ImageIO.read(new File(base + "background.png"));
            marco = ImageIO.read(new File(base + "marco.png"));
            im = ImageIO.read(new File(base + "IM.png"));

            for (int i = 0; i < 5; i++) {
                avatars[i] = ImageIO.read(new File(base + "photos\\" + (i + 1) + ".png"));
            }
            photoTest = avatars[0];
        } catch (IOException e) {
            System.err.println("Error cargando imágenes: " + e.getMessage() + " pd: si no va, pon ruta absolutas en cargarRecursos()");
        }
    }

    public void addMessage(String user, String text, boolean isMe) {
        //JPanel del bocadillo y la imagen
        JPanel row = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(1000, 130));

        // bocadillo del mensaje
        P5Bubble bubble = new P5Bubble(text, isMe, lastMsgWasMe);
        //avatar del mensaje
        AvatarPanel avatar = new AvatarPanel(isMe);

        if (isMe) { // SI EL MENSAJE ES MÍO, SE IMPRIME PRIMERO BOCADILLO LUEGO IMAGEN
            row.add(bubble);
            row.add(avatar);
        } else { // SI EL MENSAJE ES TUYO, SE IMPRIME IMAGEN, LUEGO BOCADILLO
            row.add(avatar);
            row.add(bubble);
        }

        chatContainer.add(row);
        // Espaciado negativo para que las conexiones visuales se solapen correctamente
        chatContainer.add(Box.createRigidArea(new Dimension(0, -40)));

        lastMsgWasMe = isMe; // Actualizamos el estado para el próximo mensaje
        chatContainer.revalidate();

        // Auto-scroll al final (chatGPT)                                                                 -!!!!!!!!!!!!!
        Timer scrollTimer = new Timer(50, e -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
        scrollTimer.setRepeats(false);
        scrollTimer.start();
    }

    // SCROLLBAR (chatGPT)                                                                             -!!!!!!!!!!!!!!!!
    class P5ScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        } // Track invisible

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = thumbBounds.x, y = thumbBounds.y, w = thumbBounds.width, h = thumbBounds.height;

            // Borde blanco irregular
            Path2D.Double whiteOutline = new Path2D.Double();
            whiteOutline.moveTo(x - 2, y);
            whiteOutline.lineTo(x + w + 2, y + 5);
            whiteOutline.lineTo(x + w, y + h);
            whiteOutline.lineTo(x - 4, y + h - 5);
            whiteOutline.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(whiteOutline);

            // Centro negro irregular
            Path2D.Double blackThumb = new Path2D.Double();
            blackThumb.moveTo(x, y + 2);
            blackThumb.lineTo(x + w, y);
            blackThumb.lineTo(x + w - 2, y + h - 2);
            blackThumb.lineTo(x + 2, y + h);
            blackThumb.closePath();
            g2.setColor(Color.BLACK);
            g2.fill(blackThumb);
            g2.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createHiddenButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createHiddenButton();
        }

        private JButton createHiddenButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }


    // AVATAR / FOTO ||| La idea es que esto sea algo modificable por el usuario
    class AvatarPanel extends JPanel {
        private boolean isMe;
        private float opacity = 0.0f, scale = 0.7f;

        public AvatarPanel(boolean isMe) {
            this.isMe = isMe;
            setPreferredSize(new Dimension(125, 125));
            setOpaque(false);

            // animación
            Timer timer = new Timer(15, e -> {
                opacity += 0.11f;
                scale += 0.035f;
                if (opacity >= 1.0f) {
                    opacity = 1.0f;
                    scale = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (photoTest == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            if (isMe) g2.translate(getWidth() * (1 - scale), 0);
            g2.scale(scale, scale);

            if (isMe) { // SI SOY YO, LA IMAGEN SE "VOLTEA" (en el juego tú no tienes un icono como tal)
                g2.translate(125, 0);
                g2.scale(-1, 1); // Espejo para el usuario actual
            }
            g2.drawImage(photoTest, 0, 25, 125, 125, this);
            g2.dispose();
        }
    }

    // BOCADILLO MENSAJE ||| QUE PUTISIMA PEREZA ESTOY HASTA LOS HUEVOS DE VIVIR (28-01-2026)
    class P5Bubble extends JPanel {
        private String text;
        private boolean isMe;
        private Boolean prevWasMe;
        private float opacity = 0.0f, scale = 0.7f;

        public P5Bubble(String text, boolean isMe, Boolean prevWasMe) {
            this.text = text;
            this.isMe = isMe;
            this.prevWasMe = prevWasMe;
            setOpaque(false);
            setPreferredSize(new Dimension(360, 130));

            // animación
            Timer timer = new Timer(15, e -> {
                opacity += 0.1f;
                scale += 0.03f;
                if (opacity >= 1.0f) {
                    opacity = 1.0f;
                    scale = 1.0f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            if (isMe) g2.translate(getWidth() * (1 - scale), 0);
            g2.scale(scale, scale);

            int bubbleW = 260, bubbleH = 50, xBase = isMe ? 70 : 30, yBase = 60;

            // DIBUJO DE CONEXIÓN (Lógica de mensajes conectados), llevo con esto 1 hora
            if (prevWasMe != null) {
                g2.setColor(Color.BLACK);
                Path2D.Double link = new Path2D.Double();
                if (prevWasMe && isMe) { // CASO 1: Mío -> Mío
                    link.moveTo(xBase + bubbleW - 160, yBase);
                    link.lineTo(xBase + bubbleW - 120, yBase - 50);
                    link.lineTo(xBase + bubbleW - 150, yBase - 50);
                    link.lineTo(xBase + bubbleW - 190, yBase);
                } else if (!prevWasMe && isMe) { // CASO 2: Suyo -> Mío
                    link.moveTo(xBase + bubbleW - 60, yBase);
                    link.lineTo(xBase + bubbleW - 20, yBase - 50);
                    link.lineTo(xBase + bubbleW - 50, yBase - 50);
                    link.lineTo(xBase + bubbleW - 90, yBase);
                } else if (!prevWasMe && !isMe) { // CASO 3: Suyo -> Suyo
                    link.moveTo(xBase + 160 + 50, yBase);
                    link.lineTo(xBase + 120 + 50, yBase - 50);
                    link.lineTo(xBase + 150 + 50, yBase - 50);
                    link.lineTo(xBase + 190 + 50, yBase);
                } else if (prevWasMe && !isMe) { // CASO 4: Mío -> Suyo
                    link.moveTo(xBase + 60, yBase);
                    link.lineTo(xBase + 20, yBase - 50);
                    link.lineTo(xBase + 50, yBase - 50);
                    link.lineTo(xBase + 90, yBase);
                }
                link.closePath();
                g2.fill(link);
            }

            // Fondo de la burbuja (Borde)
            Path2D.Double border = new Path2D.Double();
            border.moveTo(xBase + 5, yBase - 8);
            border.lineTo(xBase + bubbleW - 5, yBase - 7);
            border.lineTo(xBase + bubbleW + 12, yBase + bubbleH + 4);
            border.lineTo(xBase - 8, yBase + bubbleH + 5);
            border.closePath();
            // si el mensaje es mío: negro, si es suyo: blanco
            g2.setColor(isMe ? Color.BLACK : Color.WHITE);
            g2.fill(border);

            // Cuerpo de la burbuja
            Path2D.Double body = new Path2D.Double();
            body.moveTo(xBase + 12, yBase);
            body.lineTo(xBase + bubbleW - 8, yBase - 4);
            body.lineTo(xBase + bubbleW, yBase + bubbleH - 6);
            body.lineTo(xBase, yBase + bubbleH);
            body.closePath();
            // si el mensaje es mío: blanco, si es suyo: negro
            g2.setColor(isMe ? P5_WHITE : P5_BLACK);
            g2.fill(body);

            // dibujar cola del bocadillo (desistí de hacerlo manual, lo dibujé en photoshop y lo he pegado)
            // si es mío: derecha, si es suyo: izquierda
            BufferedImage img = isMe ? imgColaRight : imgColaLeft;
            if (img != null) {
                int xCola = isMe ? (xBase + bubbleW - 8) : (xBase - 20);
                g2.drawImage(img, xCola - 15, yBase - 13, 60, 70, null);
            }

            // Texto
            g2.setColor(isMe ? Color.BLACK : P5_WHITE);
            g2.setFont(new Font("Impact", Font.PLAIN, 16));
            g2.drawString(text, xBase + 20, yBase + 32);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatClienteGUI gui = new ChatClienteGUI("Joker");
            gui.setVisible(true);

            // simulaciòn, SI QUIERES PROBARLO, CAMBIA EL ISME A FALSE (SUYO) O TRUE (MÍO)
            gui.addMessage("Z", "mensaje", false);
            gui.addMessage("Morgana", "mensaje", false);
            gui.addMessage("Joker", "mensaje", true);
            gui.addMessage("Morgana", "mensaje", false);
            gui.addMessage("Morgana", "mensaje", false);
            gui.addMessage("Joker", "mensaje", true);
            gui.addMessage("Joker", "mensaje", true);
            gui.addMessage("Z", "mensaje", false);
            gui.addMessage("Z", "mensaje", false);
            gui.addMessage("Morgana", "mensaje", false);
            gui.addMessage("Joker", "mensaje", true);
            gui.addMessage("Morgana", "mensaje", false);
            gui.addMessage("Morgana", "mensaje", false);
            gui.addMessage("Joker", "mensaje", true);
            gui.addMessage("Joker", "mensaje", true);
            gui.addMessage("Z", "mensaje", false);
        });
    }
}