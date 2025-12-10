package game;

import com.sun.opengl.util.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.opengl.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.List;

public class GameMain extends JFrame {

    private GLCanvas glcanvas;
    private Animator animator;
    private JPanel menuPanel;
    private JPanel gamePanel;

    public static void main(String[] args) {
        new GameMain();
    }

    public GameMain() {
        setTitle("Knight Combat Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        showMainMenu();

        setVisible(true);
    }


    private void showMainMenu() {

        SoundManager.stopMusic();
        SoundManager.playMusic("src//assets//sounds//MainMenu.wav");

        menuPanel = new BackgroundPanel("src//assets//ui//GameBackGround.png");
        menuPanel.setLayout(new BorderLayout());
        menuPanel.setBackground(new Color(30, 30, 40));

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(30, 30, 40));
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));

        JLabel titleLabel = new JLabel("KNIGHT COMBAT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Choose Your Battle");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitleLabel.setForeground(new Color(200, 0, 0));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        titlePanel.add(subtitleLabel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        buttonsPanel.setBackground(new Color(30, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);

        JButton singlePlayerBtn = createMenuButton(
                "SINGLE PLAYER",
                "Fight against AI opponent",
                new Color(70, 130, 180)
        );
        singlePlayerBtn.addActionListener(e -> startGame(GameMode.SinglePlayer));

        JButton multiPlayerBtn = createMenuButton(
                "MULTIPLAYER",
                "Fight against another player",
                new Color(220, 80, 80)
        );
        multiPlayerBtn.addActionListener(e -> startGame(GameMode.MultiPlayer));

        JButton controlsBtn = createMenuButton(
                "CONTROLS",
                "View game controls",
                new Color(100, 100, 100)
        );
        controlsBtn.addActionListener(e -> showControls());

        JButton exitBtn = createMenuButton(
                "EXIT",
                "Quit game",
                new Color(80, 80, 80)
        );
        exitBtn.addActionListener(e -> System.exit(0));

        JButton leaderboardBtn = createMenuButton(
                "LEADERBOARD",
                "View top single-player times",
                new Color(100, 200, 100)
        );
        leaderboardBtn.addActionListener(e -> showLeaderboard());

        gbc.gridy = 0;
        buttonsPanel.add(singlePlayerBtn, gbc);
        gbc.gridy = 1;
        buttonsPanel.add(multiPlayerBtn, gbc);
        gbc.gridy = 2;
        buttonsPanel.add(controlsBtn, gbc);
        gbc.gridy = 3;
        buttonsPanel.add(leaderboardBtn, gbc);
        gbc.gridy = 4;
        buttonsPanel.add(exitBtn, gbc);


        titlePanel.setOpaque(false);
        buttonsPanel.setOpaque(false);

        menuPanel.add(titlePanel, BorderLayout.NORTH);
        menuPanel.add(buttonsPanel, BorderLayout.CENTER);

        setContentPane(menuPanel);
        revalidate();
        repaint();
    }

    private JButton createMenuButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(400, 70));
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setToolTipText(tooltip);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.brighter());
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void showControls() {
        JDialog dialog = new JDialog(this, "Game Controls", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("GAME CONTROLS");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        panel.add(createControlSection("PLAYER 1", new String[][]{
                {"A/D", "Move"},
                {"Q", "Light Attack (10 dmg)"},
                {"W", "Medium Attack (15 dmg)"},
                {"E", "Heavy Attack (20 dmg)"},
                {"Shift", "Shield / Block"}
        }));

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        panel.add(createControlSection("PLAYER 2 (Multiplayer)", new String[][]{
                {"J For Left, L For Right", "Move"},
                {"U", "Light Attack"},
                {"I", "Medium Attack"},
                {"O", "Heavy Attack"},
                {"K", "Shield / Block"}
        }));

        JScrollPane scrollPane = new JScrollPane(panel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        dialog.add(closeBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createControlSection(String playerName, String[][] controls) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(240, 240, 240));
        section.setBorder(BorderFactory.createTitledBorder(playerName));

        for (String[] control : controls) {
            JLabel label = new JLabel(control[0] + " - " + control[1]);
            label.setFont(new Font("Monospaced", Font.PLAIN, 14));
            section.add(label);
            section.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        return section;
    }

    private void startGame(GameMode mode) {





        if (menuPanel != null) {
            remove(menuPanel);
        }

        gamePanel = new JPanel(new BorderLayout());

        GameGLEventListener listener;
        if (mode == GameMode.SinglePlayer) {
            String playerName = JOptionPane.showInputDialog(this, "Enter your name:");
            if (playerName == null || playerName.isEmpty()) playerName = "Player";
            SoundManager.stopMusic();
            SoundManager.playMusic("src//assets//sounds//SkeletonMusic.wav");
            listener = new GameGLEventListener(GameMode.SinglePlayer, this, playerName);
        } else {
            String p1 = JOptionPane.showInputDialog(this, "Enter Player 1 name:");
            if (p1 == null || p1.isEmpty()) p1 = "Player 1";
            String p2 = JOptionPane.showInputDialog(this, "Enter Player 2 name:");
            if (p2 == null || p2.isEmpty()) p2 = "Player 2";
            SoundManager.stopMusic();
            SoundManager.playMusic("src//assets//sounds//Round1.wav");
            listener = new GameGLEventListener(GameMode.MultiPlayer, this, p1, p2);
        }

        glcanvas = new GLCanvas();
        glcanvas.addGLEventListener(listener);
        glcanvas.addKeyListener(listener);

        gamePanel.add(glcanvas, BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Menu (ESC)");
        backButton.setBackground(new Color(100, 100, 100));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> returnToMenu());
        gamePanel.add(backButton, BorderLayout.SOUTH);

        setContentPane(gamePanel);
        revalidate();
        repaint();


        animator = new FPSAnimator(glcanvas, 30);
        animator.start();

        glcanvas.requestFocus();
    }

    public void returnToMenu() {
        if (animator != null) {
            animator.stop();
            animator = null;
        }

        if (gamePanel != null) {
            remove(gamePanel);
        }

        if (glcanvas != null) {
            glcanvas = null;
        }

        showMainMenu();
    }

    static class BackgroundPanel extends JPanel {
        private BufferedImage background;

        public BackgroundPanel(String imagePath) {
            try {
                background = ImageIO.read(new File(imagePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            setLayout(new BorderLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (background != null) {
                g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private void showLeaderboard() {
        List<Leaderboard.Record> recordList = Leaderboard.getRecords();
        String[] columnNames = {"Rank", "Player Name", "Time"};
        Object[][] data = new Object[recordList.size()][3];
        for (int i = 0; i < recordList.size(); i++) {
            Leaderboard.Record e = recordList.get(i);
            data[i][0] = i + 1;
            data[i][1] = e.playerName;
            data[i][2] = e.getTime();
        }
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 250));
        JOptionPane.showMessageDialog(this, scrollPane, "Top Single Player Times", JOptionPane.PLAIN_MESSAGE);
    }
}
