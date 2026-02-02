package multiChat.test.cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class Login extends JDialog {
    private String username = null;
    private JTextField input;
    private float opacity = 0.0f, scale = 0.7f;

    private final Color P5_RED = new Color(208, 0, 1);
    private final Color P5_BLACK = new Color(0, 0, 0);
    private final Color P5_WHITE = new Color(245, 245, 245);

    public Login() {
        setUndecorated(true);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));
        setModal(true);

        new Timer(15, e -> {
            opacity += 0.11f; scale += 0.035f;
            if (opacity >= 1.0f) { opacity = 1.0f; scale = 1.0f; ((Timer) e.getSource()).stop(); input.requestFocusInWindow(); }
            repaint();
        }).start();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

                // Escala del fondo (desde la esquina superior izquierda del componente)
                g2.scale(scale, scale);

                //rectangulo irregular negro
                Path2D.Double bg = new Path2D.Double();
                bg.moveTo(20, 50);
                bg.lineTo(380, 20);
                bg.lineTo(350, 180);
                bg.lineTo(50, 160);
                bg.closePath();

                g2.setColor(P5_BLACK);
                g2.fill(bg);

                // borde
                g2.setStroke(new BasicStroke(4));
                g2.setColor(P5_WHITE);
                g2.draw(bg);

                g2.setFont(new Font("Impact", Font.ITALIC, 24));
                g2.drawString("LOG IN_", 60, 75);

                g2.dispose();
            }
        };
        panel.setLayout(null);
        panel.setOpaque(false);


        //chat-gpteada histórica, no sabia como coño se hacia esto
        input = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g2.scale(scale, scale);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());

                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g2.scale(scale, scale); //
                super.paintBorder(g2);
                g2.dispose();
            }
        };

        input.setOpaque(false);
        input.setBounds(70, 90, 250, 40);
        input.setFont(new Font("Impact", Font.PLAIN, 20));
        input.setBackground(P5_WHITE);
        input.setForeground(Color.BLACK);
        input.setCaretColor(P5_RED);
        input.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        input.addActionListener(e -> {
            username = input.getText().trim();
            if (!username.isEmpty()) dispose();
        });

        panel.add(input);
        add(panel);
    }

    public String getUsername() {
        return username;
    }
}