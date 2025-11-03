package nl.pancompany.spaceinvaders.sprite;

import nl.pancompany.spaceinvaders.shared.Constants;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static nl.pancompany.spaceinvaders.shared.Constants.PLAYER_BORDER;
import static nl.pancompany.spaceinvaders.shared.Constants.PLAYER_WIDTH;

public class Player extends Sprite {

    public Player() {

        initPlayer();
    }

    private void initPlayer() {

        var playerImg = "/images/player.png";

        var ii = new ImageIcon(getClass().getResource(playerImg));

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

        if (x >= Constants.BOARD_WIDTH - (PLAYER_WIDTH + PLAYER_BORDER)) {

            x = Constants.BOARD_WIDTH - (PLAYER_WIDTH + PLAYER_BORDER);
        }
    }

    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {

            dx = -2; // alien moves by |<direction>| = 1 pixel per gamecycle; player moves by 2 pixels per gamecycle
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
