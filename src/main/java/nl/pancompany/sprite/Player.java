package nl.pancompany.sprite;

import nl.pancompany.Commons;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static nl.pancompany.Commons.PLAYER_BORDER;

public class Player extends Sprite {

    private int width;

    public Player() {

        initPlayer();
    }

    private void initPlayer() {

        var playerImg = "/images/player.png";

        var ii = new ImageIcon(getClass().getResource(playerImg));

        width = ii.getImage().getWidth(null); // = 15, just like declared in Commons
        setImage(ii.getImage());

        int START_X = 270;
        setX(START_X);

        int START_Y = 280;
        setY(START_Y);
    }

    public void act() {

        x += dx;

        if (x <= PLAYER_BORDER) {

            x = PLAYER_BORDER;
        }

        if (x >= Commons.BOARD_WIDTH - (width + PLAYER_BORDER)) { // might be replaced by constant

            x = Commons.BOARD_WIDTH - (width + PLAYER_BORDER);
        }
    }

    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {

            dx = -2;
        }

        if (key == KeyEvent.VK_RIGHT) {

            dx = 2;
        }
    }

    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {

            dx = 0;
        }

        if (key == KeyEvent.VK_RIGHT) {

            dx = 0;
        }
    }
}
