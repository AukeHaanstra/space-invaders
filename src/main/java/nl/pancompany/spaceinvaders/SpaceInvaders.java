package nl.pancompany.spaceinvaders;

import javax.swing.*;
import java.awt.*;

public class SpaceInvaders extends JFrame  {

    static void main(String[] args) { // 1
        EventQueue.invokeLater(() -> {
            var ex = new SpaceInvaders();
            ex.setVisible(true);
        });
    }

    public SpaceInvaders() { // 2
        initUI();
    }

    private void initUI() { // 3
        setTitle("Space Invaders");
        setLayout(new BorderLayout());
        setContentPane(new Board()); // 4
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

}
