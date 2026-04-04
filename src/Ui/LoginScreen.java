package Ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginScreen extends JPanel implements ActionListener {

    JFrame window;
    JTextField usernameField;
    JPasswordField passwordField;
    JButton loginBtn, registerBtn;
    Image bgImage;

    // Design Settings - No external files needed
    Color buttonColor = new Color(0, 100, 0); // Arcade Green
    Font retroFont = new Font(Font.MONOSPACED, Font.BOLD, 22);
    Font fieldFont = new Font(Font.MONOSPACED, Font.PLAIN, 20);

    public LoginScreen() {
        //  Load background
        try {
            bgImage = new ImageIcon(getClass().getResource("/backgrounds/background.png")).getImage();
        } catch (Exception e) {
            System.out.println("Background image not found at /backgrounds/background.png");
        }

        setLayout(null);

        // --- UI COMPONENTS ---

        // Username Label
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(retroFont);
        userLabel.setForeground(Color.WHITE);
        userLabel.setBounds(500, 250, 400, 40);
        add(userLabel);

        // Username Field
        usernameField = new JTextField();
        usernameField.setFont(fieldFont);
        usernameField.setBounds(500, 290, 500, 50);
        usernameField.setBackground(Color.BLACK);
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(new LineBorder(Color.WHITE, 3));
        add(usernameField);

        // Password Label
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(retroFont);
        passLabel.setForeground(Color.WHITE);
        passLabel.setBounds(500, 360, 400, 40);
        add(passLabel);

        // Password Field
        passwordField = new JPasswordField();
        passwordField.setFont(fieldFont);
        passwordField.setBounds(500, 400, 500, 50);
        passwordField.setBackground(Color.BLACK);
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(new LineBorder(Color.WHITE, 3));
        add(passwordField);

        // Login Button
        loginBtn = createRetroButton("LOGIN", 500, 490, 240, 60, buttonColor);
        add(loginBtn);

        // Register Button
        registerBtn = createRetroButton("REGISTER", 760, 490, 240, 60, buttonColor);
        add(registerBtn);

        // --- FRAME SETUP ---
        window = new JFrame("Login");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        window.add(this);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(window);
        } else {
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            window.setVisible(true);
        }
    }

    // Helper method for the Arcade Button Style
    private JButton createRetroButton(String text, int x, int y, int w, int h, Color bg) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, w, h);
        btn.setFont(retroFont.deriveFont(Font.BOLD, 18f));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

            // Semi-transparent overlay to make text readable
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(450, 200, 600, 420);

            // White arcade border
            g.setColor(Color.WHITE);
            g.drawRect(450, 200, 600, 420);
        } else {
            // Fallback background color if image is missing
            g.setColor(new Color(20, 30, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (e.getSource() == loginBtn) {
            if (FileHandler.loginUser(username, password)) {
                JOptionPane.showMessageDialog(this, "LOGIN SUCCESSFUL!");

                // This opens the next screen
                new TitleScreen();

                //  This closes the current login screen
                window.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "INVALID CREDENTIALS");
            }
        }

        if (e.getSource() == registerBtn) {
            if (FileHandler.registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "ACCOUNT CREATED!");
            } else {
                JOptionPane.showMessageDialog(this, "USER ALREADY EXISTS!");
            }
        }
    }
}