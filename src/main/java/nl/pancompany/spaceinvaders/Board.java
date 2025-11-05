package nl.pancompany.spaceinvaders;

import lombok.extern.slf4j.Slf4j;
import nl.pancompany.spaceinvaders.alien.getcount.GetAlienDownCount;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.game.get.GameReadModel;
import nl.pancompany.spaceinvaders.game.get.GetGame;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import nl.pancompany.spaceinvaders.game.resume.ResumeGame;
import nl.pancompany.spaceinvaders.game.stop.StopGame;
import nl.pancompany.spaceinvaders.laserbeam.create.CreateLaserBeam;
import nl.pancompany.spaceinvaders.player.stop.StopPlayer;
import nl.pancompany.spaceinvaders.shared.Constants;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.sprite.destroy.DestroySprite;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteByEntityName;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteById;
import nl.pancompany.spaceinvaders.sprite.get.SpriteReadModel;
import nl.pancompany.spaceinvaders.sprite.turn.TurnSprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;

import static nl.pancompany.spaceinvaders.shared.Constants.*;
import static nl.pancompany.spaceinvaders.shared.Direction.LEFT;
import static nl.pancompany.spaceinvaders.shared.Direction.RIGHT;

@Slf4j
public class Board extends JPanel {

    private final Dimension dimensions = new Dimension(Constants.BOARD_WIDTH, Constants.BOARD_HEIGHT);
    private final CommandApi commandApi;
    private final QueryApi queryApi;
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

        addKeyListener(new TAdapter()); // 7 start listening to keystrokes: modify entities accordingly -> translation
        addKeyListener(new ReplayManager());
        timer = new Timer(Constants.DELAY, new GameCycle()); // 8 start scheduled update-repaint gamecycles (9-12: update entities & repaint UI) -> can also emit events
        timer.start();
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

            SpriteReadModel player = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID))
                    .orElseThrow(() -> new IllegalStateException("Player Sprite not found."));
            int x = player.x();
            int y = player.y();

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_SPACE) {

                boolean inGame = queryApi.query(new GetGame()).orElseThrow(() -> new IllegalStateException("Game not found.")).inGame();

                if (inGame) {
                    Optional<SpriteReadModel> laser = queryApi.query(new GetSpriteById(LASER_SPRITE_ID));

                    if (laser.isEmpty() || !laser.get().visible()) { // create a new laser beam when space pressed and previous beam is not visible anymore
                        commandApi.publish(new CreateLaserBeam(LASER_SPRITE_ID, LASER_ENTITY, LASER_IMAGE_PATH, x, y, LASER_SPEED, Direction.UP));
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

    private class ReplayManager extends KeyAdapter {

        private final static int REPLAY_FRAMES = 30;
        private int replayPosition = 0;
        private int lastEventPosition = 0;
        private int step = 0;
        private boolean inReplay;

        @Override
        public void keyPressed(KeyEvent e) {
            replayKeyPressed(e);
        }

        private void replayKeyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_ENTER) {
                if (inReplay) {
                    replayPosition = 0;
                    lastEventPosition = 0;
                    step = 0;
                    commandApi.resume();
                    commandApi.publish(new ResumeGame());
                    inReplay = false;
                    timer.start();
                } else {
                    commandApi.publish(new StopGame(REPLAY_MESSAGE));
                    lastEventPosition = queryApi.getLastEventPosition();
                    step = lastEventPosition / REPLAY_FRAMES;
                    replayPosition = lastEventPosition;
                    repaint();
                    inReplay = true;
                }
            }

            if (key == KeyEvent.VK_OPEN_BRACKET) {
                if (inReplay) {
                    replayPosition -= step;
                    if (replayPosition < 50) { // sequence position 50: first gamecycle initiated
                        replayPosition = 50;
                    }
                    commandApi.replay(replayPosition);
                    log.info("Replaying to position: {} of {}", replayPosition, lastEventPosition);
                    sleep(300);
                    repaint();
                }
            }

            if (key == KeyEvent.VK_CLOSE_BRACKET) {
                if (inReplay) {
                    replayPosition += step;
                    if (replayPosition > lastEventPosition) {
                        replayPosition = lastEventPosition;
                    }
                    commandApi.replay(replayPosition);
                    log.info("Replaying to position: {} of {}", replayPosition, lastEventPosition);
                    sleep(300);
                    repaint();
                }
            }
        }

        private static void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
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
        }
    }

    @Override
    public void paintComponent(Graphics g) { // 11
        super.paintComponent(g);

        doDrawing(g); // 12 draws entities, finishes some multi-step state transitions (e.g., dying - becoming invisible)
    }

    private void update() {
        // might have gone into an automation too, this just shows the use of the command API
        int deaths = queryApi.query(new GetAlienDownCount());
        if (deaths == Constants.NUMBER_OF_ALIENS_TO_DESTROY) {
            commandApi.publish(new StopGame("Game won!"));
        }
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, dimensions.width, dimensions.height);
//        g.setColor(new Color(100, 0, 0)); // draw border for debugging purposes
//        g.drawRect(1, 1, Commons.BOARD_WIDTH-1, Commons.BOARD_HEIGHT-1);
        g.setColor(Color.green);

        GameReadModel game = queryApi.query(new GetGame()).orElseThrow(() -> new IllegalStateException("Game not found."));
        if (game.inGame() || game.replay()) { // check whether player is still alive or game over, or replay

            g.drawLine(0, Constants.GROUND_Y,
                    Constants.BOARD_WIDTH, Constants.GROUND_Y);

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

        List<SpriteReadModel> aliens = queryApi.query(new GetSpriteByEntityName(ALIEN_ENTITY));

        for (SpriteReadModel alien : aliens) {

            if (alien.visible()) { // if RIP, in previous gamecycle, alien will now become invisible
                // if explosionTriggered, first display set explosion image
                Image alienImage = new ImageIcon(getClass().getResource(alien.imagePath())).getImage();
                g.drawImage(alienImage, alien.x(), alien.y(), this);
            }

            if (alien.explosionTriggered()) {
                // Since gamecycle is almost over (update() already ran), alien will become invisible (and game will stop) in the next gamecycle
                commandApi.publish(new DestroySprite(alien.spriteId()));
            }
        }
    }

    private void drawPlayer(Graphics g) {
        SpriteReadModel player = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID))
                .orElseThrow(() -> new IllegalStateException("Player Sprite not found."));
        if (player.visible()) { // if RIP, in previous gamecycle, player will now become invisible
            // if explosionTriggered, first display set explosion image
            Image playerImage = new ImageIcon(getClass().getResource(player.imagePath())).getImage();
            g.drawImage(playerImage, player.x(), player.y(), this);
        }

        if (player.explosionTriggered()) {
            commandApi.publish(new DestroySprite(PLAYER_SPRITE_ID)); // Since gamecycle is almost over (update() already ran), player will become invisible (and game will stop) in the next gamecycle
            commandApi.publish(new StopGame("Game Over"));
        }
    }

    private void drawShot(Graphics g) {
        queryApi.query(new GetSpriteById(LASER_SPRITE_ID)).ifPresent(laser -> {
            if (laser.visible()) {
                Image laserImage = new ImageIcon(getClass().getResource(laser.imagePath())).getImage();
                g.drawImage(laserImage, laser.x(), laser.y(), this);
            }
        });

    }

    private void drawBombing(Graphics g) {

        List<SpriteReadModel> bombs = queryApi.query(new GetSpriteByEntityName(BOMB_ENTITY));

        for (SpriteReadModel bomb : bombs) {
            if (bomb.visible()) {
                Image bombImage = new ImageIcon(getClass().getResource(bomb.imagePath())).getImage();
                g.drawImage(bombImage, bomb.x(), bomb.y(), this);
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
