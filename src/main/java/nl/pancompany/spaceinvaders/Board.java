package nl.pancompany.spaceinvaders;

import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.game.get.GetGame;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import nl.pancompany.spaceinvaders.game.stop.StopGame;
import nl.pancompany.spaceinvaders.player.stop.StopPlayer;
import nl.pancompany.spaceinvaders.sprite.turn.TurnSprite;
import nl.pancompany.spaceinvaders.sprite.Alien;
import nl.pancompany.spaceinvaders.sprite.Shot;
import nl.pancompany.spaceinvaders.sprite.changeimage.ChangeSpriteImage;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosion;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteById;
import nl.pancompany.spaceinvaders.sprite.get.SpriteReadModel;
import nl.pancompany.spaceinvaders.sprite.restinpeace.RestInPeaceSprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static nl.pancompany.spaceinvaders.Constants.PLAYER_SPRITE_ID;
import static nl.pancompany.spaceinvaders.shared.Direction.LEFT;
import static nl.pancompany.spaceinvaders.shared.Direction.RIGHT;

public class Board extends JPanel {

    private final Dimension dimensions = new Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
    private final CommandApi commandApi;
    private final QueryApi queryApi;
    private List<Alien> aliens;
    private Shot shot; // single shot from the player
    
    private int direction = -1;
    private int deaths = 0;

    private final String explImg = "/images/explosion.png";

    private Timer timer;

    public Board(CommandApi commandApi, QueryApi queryApi) {
        this.commandApi = commandApi;
        this.queryApi = queryApi;
        initBoard(); // 5
    }

    private void initBoard() {
        setFocusable(true);
        setBackground(Color.black);

        commandApi.publish(new CreateGame());
        gameInit(); // 6 initialize domain entities (sprites) as Board fields -> on BoardReady event

        addKeyListener(new TAdapter()); // 7 start listening to keystrokes: modify entities accordingly -> translation
        timer = new Timer(Constants.DELAY, new GameCycle()); // 8 start scheduled update-repaint gamecycles (9-12: update entities & repaint UI) -> can also emit events
        timer.start();
    }

    private void gameInit() {

        aliens = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {

                var alien = new Alien(Constants.ALIEN_START_X + 18 * j,
                        Constants.ALIEN_START_Y + 18 * i);
                aliens.add(alien);
            }
        }

//        player = new Player();
        shot = new Shot();
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {

            // delegate left & right key released events to player entity
            directionKeyReleased(e);
        }

        private void directionKeyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_LEFT) {
                commandApi.publish(new StopPlayer());
            }

            if (key == KeyEvent.VK_RIGHT) {
                commandApi.publish(new StopPlayer());
            }
        }


        @Override
        public void keyPressed(KeyEvent e) {

            // delegate left & right keystrokes to player entity
            directionKeyPressed(e); // translator

            SpriteReadModel playerReadModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID))
                    .orElseThrow(() -> new IllegalStateException("Player Sprite not found."));
            int x = playerReadModel.x();
            int y = playerReadModel.y();

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_SPACE) {

            boolean inGame = queryApi.query(new GetGame()).orElseThrow(() -> new IllegalStateException("Game not found.")).inGame();
                if (inGame) {

                    if (!shot.isVisible()) { // create a new shot when space pressed and previous shot is not visible anymore

                        shot = new Shot(x, y);
                    }
                }
            }
        }

        private void directionKeyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_LEFT) {
                commandApi.publish(new TurnSprite(PLAYER_SPRITE_ID, LEFT));
            }

            if (key == KeyEvent.VK_RIGHT) {
                commandApi.publish(new TurnSprite(PLAYER_SPRITE_ID, RIGHT));
            }
        }
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            commandApi.publish(new InitiateGameCycle()); // translation
            doGameCycle();
        }

        private void doGameCycle() {
            boolean inGame = queryApi.query(new GetGame()).orElseThrow(() -> new IllegalStateException("Game not found.")).inGame();
            if (!inGame) {
                timer.stop(); // only stop time when gameover is a fact
            }
            update(); // 9 update domain entities according to business (game) rules -> state changes, can be split per entity, on each cycle event; business rules in update()
            repaint(); // 10 results in paintComponent() being called -> automation: react to above state changes in @EventHandling UI components in each slice
            // emit Commands for new desired state changes
        }
    }

    @Override
    public void paintComponent(Graphics g) { // 11
        super.paintComponent(g);

        doDrawing(g); // 12 draws entities, finishes some multi-step state transitions (e.g., dying - becoming invisible)
    }

    private void update() {

        if (deaths == Constants.NUMBER_OF_ALIENS_TO_DESTROY) {

            commandApi.publish(new StopGame("Game won!"));
        }

        // shot
        if (shot.isVisible()) {

            int shotX = shot.getX();
            int shotY = shot.getY();

            for (Alien alien : aliens) {

                int alienX = alien.getX();
                int alienY = alien.getY();

                if (alien.isVisible() && shot.isVisible()) {
                    if (shotX >= (alienX)
                            && shotX <= (alienX + Constants.ALIEN_WIDTH)
                            && shotY >= (alienY)
                            && shotY <= (alienY + Constants.ALIEN_HEIGHT)) {

                        var ii = new ImageIcon(getClass().getResource(explImg)); // explosion image
                        alien.setImage(ii.getImage());
                        alien.setDying(true); // allows the explosion image set above to be shown for one gamecycle
                        deaths++;
                        shot.die();
                    }
                }
            }

            int y = shot.getY();
            y -= 4;

            if (y < 0) {
                shot.die();
            } else {
                shot.setY(y);
            }
        }

        // aliens

        for (Alien alien : aliens) {

            int x = alien.getX();

            if (x >= Constants.BOARD_WIDTH - Constants.ALIEN_BORDER_RIGHT && direction != -1) {

                direction = -1;

                Iterator<Alien> i1 = aliens.iterator();

                while (i1.hasNext()) {

                    Alien a2 = i1.next();
                    a2.setY(a2.getY() + Constants.GO_DOWN);
                }
            }

            if (x <= Constants.ALIEN_BORDER_LEFT && direction != 1) {

                direction = 1;

                Iterator<Alien> i2 = aliens.iterator();

                while (i2.hasNext()) {

                    Alien a = i2.next();
                    a.setY(a.getY() + Constants.GO_DOWN);
                }
            }
        }

        Iterator<Alien> it = aliens.iterator();

        while (it.hasNext()) {

            Alien alien = it.next();

            if (alien.isVisible()) {

                int y = alien.getY();

                if (y > Constants.GROUND - Constants.ALIEN_HEIGHT) {
                    commandApi.publish(new StopGame("Invasion!"));
                }

                alien.act(direction);  // alien moves by |<direction>| = 1 pixel per gamecycle; player moves by 2 pixels per gamecycle
            }
        }

        // bombs
        var generator = new Random();

        for (Alien alien : aliens) {

            int shot = generator.nextInt(15);
            Alien.Bomb bomb = alien.getBomb();

            if (shot == Constants.CHANCE && alien.isVisible() && bomb.isDestroyed()) {

                bomb.setDestroyed(false);
                bomb.setX(alien.getX());
                bomb.setY(alien.getY());
            }

            int bombX = bomb.getX();
            int bombY = bomb.getY();
            SpriteReadModel playerReadModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID))
                    .orElseThrow(() -> new IllegalStateException("Player Sprite not found."));
            int playerX = playerReadModel.x();
            int playerY = playerReadModel.y();

            if (playerReadModel.visible() && !bomb.isDestroyed()) {

                if (bombX >= (playerX)
                        && bombX <= (playerX + Constants.PLAYER_WIDTH)
                        && bombY >= (playerY)
                        && bombY <= (playerY + Constants.PLAYER_HEIGHT)) {

                    var ii = new ImageIcon(getClass().getResource(explImg));
                    commandApi.publish(new TriggerSpriteExplosion(PLAYER_SPRITE_ID));
                    commandApi.publish(new ChangeSpriteImage(PLAYER_SPRITE_ID, explImg)); // make simple automation to trigger ChangeSpriteImage upon SpriteExplosionTriggered
                    bomb.setDestroyed(true);
                }
            }

            if (!bomb.isDestroyed()) {

                bomb.setY(bomb.getY() + 1);

                if (bomb.getY() >= Constants.GROUND - Constants.BOMB_HEIGHT) {

                    bomb.setDestroyed(true);
                }
            }
        }
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, dimensions.width, dimensions.height);
//        g.setColor(new Color(100, 0, 0)); // draw border for debugging purposes
//        g.drawRect(1, 1, Commons.BOARD_WIDTH-1, Commons.BOARD_HEIGHT-1);
        g.setColor(Color.green);

        boolean inGame = queryApi.query(new GetGame()).orElseThrow(() -> new IllegalStateException("Game not found.")).inGame();
        if (inGame) { // check whether player is still alive or game over

            g.drawLine(0, Constants.GROUND,
                    Constants.BOARD_WIDTH, Constants.GROUND);

            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawBombing(g);

        } else {
            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawAliens(Graphics g) {

        for (Alien alien : aliens) {

            if (alien.isVisible()) { // if RIP, in previous gamecycle, alien will now become invisible
                // if explosionTriggered, first display set explosion image
                g.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);
            }

            if (alien.isDying()) {

                alien.die(); // Since gamecycle is almost over (update() already ran), alien will become invisible (and game will stop) in the next gamecycle
            }
        }
    }

    private void drawPlayer(Graphics g) {
        SpriteReadModel playerReadModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID))
                .orElseThrow(() -> new IllegalStateException("Player Sprite not found."));
        if (playerReadModel.visible()) { // if RIP, in previous gamecycle, player will now become invisible
            // if explosionTriggered, first display set explosion image
            Image playerImage = new ImageIcon(getClass().getResource(playerReadModel.imagePath())).getImage();
            g.drawImage(playerImage, playerReadModel.x(), playerReadModel.y(), this);
        }

        if (playerReadModel.explosionTriggered()) {
            commandApi.publish(new RestInPeaceSprite(PLAYER_SPRITE_ID)); // Since gamecycle is almost over (update() already ran), player will become invisible (and game will stop) in the next gamecycle
            commandApi.publish(new StopGame("Game Over"));
        }
    }

    private void drawShot(Graphics g) {

        if (shot.isVisible()) {

            g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
        }
    }

    private void drawBombing(Graphics g) {

        for (Alien a : aliens) {

            Alien.Bomb b = a.getBomb();

            if (!b.isDestroyed()) {

                g.drawImage(b.getImage(), b.getX(), b.getY(), this);
            }
        }
    }

    private void gameOver(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);

        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, Constants.BOARD_WIDTH / 2 - 30, Constants.BOARD_WIDTH - 100, 50);
        g.setColor(Color.white);
        g.drawRect(50, Constants.BOARD_WIDTH / 2 - 30, Constants.BOARD_WIDTH - 100, 50);

        var small = new Font("Helvetica", Font.BOLD, 14);
        var fontMetrics = this.getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        String message = queryApi.query(new GetGame()).orElseThrow(() -> new IllegalStateException("Game not found."))
                .message().orElseThrow(() -> new IllegalStateException("Gameover message not found."));
        g.drawString(message, (Constants.BOARD_WIDTH - fontMetrics.stringWidth(message)) / 2,
                Constants.BOARD_WIDTH / 2);
    }

    @Override
    public Dimension getPreferredSize() {
        return dimensions;
    }

}
