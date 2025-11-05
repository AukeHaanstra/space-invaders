package nl.pancompany.spaceinvaders;

import lombok.Getter;
import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.spaceinvaders.alien.dropper.AliensDropper;
import nl.pancompany.spaceinvaders.alien.getcount.AlienDownCountQueryHandler;
import nl.pancompany.spaceinvaders.alien.mover.AliensMover;
import nl.pancompany.spaceinvaders.bomb.dropper.BombsDropper;
import nl.pancompany.spaceinvaders.game.create.CreateGameCommandHandler;
import nl.pancompany.spaceinvaders.game.get.GameQueryHandler;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycleCommandHandler;
import nl.pancompany.spaceinvaders.game.resume.ResumeGameCommandHandler;
import nl.pancompany.spaceinvaders.game.stop.StopGameCommandHandler;
import nl.pancompany.spaceinvaders.laserbeam.shooter.LaserBeamShooter;
import nl.pancompany.spaceinvaders.player.mover.PlayerMover;
import nl.pancompany.spaceinvaders.player.stop.StopPlayerCommandHandler;
import nl.pancompany.spaceinvaders.laserbeam.create.CreateLaserBeamCommandHandler;
import nl.pancompany.spaceinvaders.sprite.changeimage.ChangeSpriteImageCommandHandler;
import nl.pancompany.spaceinvaders.sprite.creator.SpriteCreator;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosionCommandHandler;
import nl.pancompany.spaceinvaders.sprite.get.SpriteProjector;
import nl.pancompany.spaceinvaders.sprite.get.SpriteQueryHandler;
import nl.pancompany.spaceinvaders.sprite.get.SpriteRepository;
import nl.pancompany.spaceinvaders.sprite.move.MoveSpriteCommandHandler;
import nl.pancompany.spaceinvaders.sprite.destroy.DestroySpriteCommandHandler;
import nl.pancompany.spaceinvaders.sprite.turn.TurnSpriteCommandHandler;

import javax.swing.*;
import java.awt.*;

@Getter
public class SpaceInvaders extends JFrame  {

    CommandApi commandApi;
    QueryApi queryApi;

    static void main(String[] args) { // 1
        EventQueue.invokeLater(() -> {
            var ex = new SpaceInvaders(new EventStore(), true);
            ex.setVisible(true);
        });
    }

    public SpaceInvaders(EventStore eventStore, boolean initUi) { // 2
        injectDependencies(eventStore);
        if (initUi) {
            initUI();
        }
    }

    private void injectDependencies(EventStore eventStore) {
        EventBus eventBus = eventStore.getEventBus();

        // Command handlers
        // Game
        CreateGameCommandHandler createGameCommandHandler = new CreateGameCommandHandler(eventStore);
        InitiateGameCycleCommandHandler initiateGameCycleCommandHandler = new InitiateGameCycleCommandHandler(eventStore);
        StopGameCommandHandler stopGameCommandHandler = new StopGameCommandHandler(eventStore);
        ResumeGameCommandHandler resumeGameCommandHandler = new ResumeGameCommandHandler(eventStore);
        // Player
        TurnSpriteCommandHandler turnSpriteCommandHandler = new TurnSpriteCommandHandler(eventStore);
        StopPlayerCommandHandler stopPlayerCommandHandler = new StopPlayerCommandHandler(eventStore);
        // Sprite
        ChangeSpriteImageCommandHandler changeSpriteImageCommandHandler = new ChangeSpriteImageCommandHandler(eventStore);
        TriggerSpriteExplosionCommandHandler triggerSpriteExplosionCommandHandler = new TriggerSpriteExplosionCommandHandler(eventStore);
        DestroySpriteCommandHandler destroySpriteCommandHandler = new DestroySpriteCommandHandler(eventStore);
        MoveSpriteCommandHandler moveSpriteCommandHandler = new MoveSpriteCommandHandler(eventStore);
        // Shot
        CreateLaserBeamCommandHandler createLaserBeamCommandHandler = new CreateLaserBeamCommandHandler(eventStore);

        commandApi = CommandApi.builder()
                .eventBus(eventBus)
                // Game
                .createGameCommandHandler(createGameCommandHandler)
                .initiateGameCycleCommandHandler(initiateGameCycleCommandHandler)
                .stopGameCommandHandler(stopGameCommandHandler)
                .resumeGameCommandHandler(resumeGameCommandHandler)
                // Player
                .turnSpriteCommandHandler(turnSpriteCommandHandler)
                .stopPlayerCommandHandler(stopPlayerCommandHandler)
                // Sprite
                .changeSpriteImageCommandHandler(changeSpriteImageCommandHandler)
                .triggerSpriteExplosionCommandHandler(triggerSpriteExplosionCommandHandler)
                .destroySpriteCommandHandler(destroySpriteCommandHandler)
                .moveSpriteCommandHandler(moveSpriteCommandHandler) // TODO: Check whether used!
                // Laser
                .createLaserBeamCommandHandler(createLaserBeamCommandHandler)
                // Shot
                .build();

        // Query handlers, projectors and repositories
        // Game (live model)
        GameQueryHandler gameQueryHandler = new GameQueryHandler();
        // Sprite
        SpriteRepository spriteRepository = new SpriteRepository();
        SpriteProjector spriteProjector = new SpriteProjector(spriteRepository);
        SpriteQueryHandler spriteQueryHandler = new SpriteQueryHandler(spriteRepository);
        // Alien
        AlienDownCountQueryHandler alienDownCountQueryHandler = new AlienDownCountQueryHandler();
        queryApi = QueryApi.builder()
                .eventStore(eventStore)
                // Sprite
                .spriteQueryHandler(spriteQueryHandler)
                .gameQueryHandler(gameQueryHandler)
                .alienDownCountQueryHandler(alienDownCountQueryHandler)
                .build();

        // Query event-handler registrations
        eventBus.registerAsynchronousEventHandler(spriteProjector);
        eventBus.registerAsynchronousEventHandler(gameQueryHandler);
        eventBus.registerAsynchronousEventHandler(alienDownCountQueryHandler);

        // Automations
        // Player
        SpriteCreator spriteCreator = new SpriteCreator(eventStore);
        PlayerMover playerMover = new PlayerMover(eventStore);
        // Alien
        AliensMover aliensMover = new AliensMover(eventStore);
        AliensDropper aliensDropper = new AliensDropper(eventStore);
        // Bomb
        BombsDropper bombsDropper = new BombsDropper(eventStore);
        // Laser
        LaserBeamShooter laserBeamShooter = new LaserBeamShooter(eventStore);

        // Automation event-handler registrations
        // Player
        eventBus.registerAsynchronousEventHandler(spriteCreator);
        eventBus.registerAsynchronousEventHandler(playerMover);
        // Alien
        eventBus.registerAsynchronousEventHandler(aliensMover);
        eventBus.registerAsynchronousEventHandler(aliensDropper);
        // Bomb
        eventBus.registerAsynchronousEventHandler(bombsDropper);
        // Laser
        eventBus.registerAsynchronousEventHandler(laserBeamShooter);
    }

    private void initUI() { // 3
        setTitle("Space Invaders");
        setLayout(new BorderLayout());
        Board board = new Board(commandApi, queryApi);
        setContentPane(board); // 4
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

}
