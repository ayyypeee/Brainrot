package Ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;

public class LoginScreen extends JPanel implements ActionListener {

    private JFrame         window;
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JButton        loginBtn;
    private JButton        registerBtn;
    private Image          bgImage;

    // Shared fonts and colors for a consistent arcade look.
    private Font  retroFont   = new Font(Font.MONOSPACED, Font.BOLD,  22);
    private Font  fieldFont   = new Font(Font.MONOSPACED, Font.PLAIN, 20);
    private Color buttonColor = new Color(0, 100, 0);

    // Constructor builds the UI and opens a full-screen window.
    public LoginScreen() {
        // Load the background image.
        try {
            bgImage = new ImageIcon(getClass().getResource("/backgrounds/background.png")).getImage();
        } catch (Exception e) {
            System.out.println("Background image not found.");
        }

        setLayout(null);

        // Username label and text field.
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(retroFont);
        userLabel.setForeground(Color.WHITE);
        userLabel.setBounds(500, 250, 400, 40);
        add(userLabel);

        usernameField = new JTextField();
        usernameField.setFont(fieldFont);
        usernameField.setBounds(500, 290, 500, 50);
        usernameField.setBackground(Color.BLACK);
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);
        usernameField.setBorder(new LineBorder(Color.WHITE, 3));
        add(usernameField);

        // Password label and password field.
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(retroFont);
        passLabel.setForeground(Color.WHITE);
        passLabel.setBounds(500, 360, 400, 40);
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(fieldFont);
        passwordField.setBounds(500, 400, 500, 50);
        passwordField.setBackground(Color.BLACK);
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(new LineBorder(Color.WHITE, 3));
        add(passwordField);

        // Login and Register buttons.
        loginBtn    = createButton("LOGIN",    500, 490, 240, 60);
        registerBtn = createButton("REGISTER", 760, 490, 240, 60);
        add(loginBtn);
        add(registerBtn);

        // Set up the full-screen window.
        window = new JFrame("Login");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setUndecorated(true);
        window.add(this);

        GraphicsEnvironment ge  = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice      gd  = ge.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(window);
        } else {
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            window.setVisible(true);
        }
    }

    private JButton createButton(String text, int x, int y, int w, int h) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, w, h);
        btn.setFont(retroFont.deriveFont(Font.BOLD, 18f));
        btn.setBackground(buttonColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(buttonColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(buttonColor);
            }
        });

        return btn;
    }

    // Draws the background image and the dark overlay box around the login fields.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(450, 200, 600, 420);

            g.setColor(Color.WHITE);
            g.drawRect(450, 200, 600, 420);
        } else {

            g.setColor(new Color(20, 30, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // Called when Login or Register is clicked
    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()); // Wrapper: char[] -> String

        if (e.getSource() == loginBtn) {
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.");
                return;
            }
            if (FileHandler.loginUser(username, password)) {
                JOptionPane.showMessageDialog(this, "LOGIN SUCCESSFUL!");
                window.dispose();
                // ── KEY CHANGE: pass the logged-in username to TitleScreen ──
                new TitleScreen(username);
            } else {
                JOptionPane.showMessageDialog(this, "INVALID CREDENTIALS");
            }
        }

        if (e.getSource() == registerBtn) {
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.");
                return;
            }
            if (FileHandler.registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "ACCOUNT CREATED!");
            } else {
                JOptionPane.showMessageDialog(this, "USER ALREADY EXISTS!");
            }
        }
    }
}