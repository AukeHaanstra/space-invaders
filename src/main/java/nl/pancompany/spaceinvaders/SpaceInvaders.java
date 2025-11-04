package nl.pancompany.spaceinvaders;

import lombok.Getter;
import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.spaceinvaders.alien.dropper.AlienDropper;
import nl.pancompany.spaceinvaders.alien.mover.AliensMover;
import nl.pancompany.spaceinvaders.game.create.CreateGameCommandHandler;
import nl.pancompany.spaceinvaders.game.get.GameQueryHandler;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycleCommandHandler;
import nl.pancompany.spaceinvaders.game.stopper.GameStopper;
import nl.pancompany.spaceinvaders.player.mover.PlayerMover;
import nl.pancompany.spaceinvaders.player.stop.StopPlayerCommandHandler;
import nl.pancompany.spaceinvaders.sprite.changeimage.ChangeSpriteImageCommandHandler;
import nl.pancompany.spaceinvaders.sprite.creator.SpriteCreator;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosionCommandHandler;
import nl.pancompany.spaceinvaders.sprite.get.SpriteProjector;
import nl.pancompany.spaceinvaders.sprite.get.SpriteQueryHandler;
import nl.pancompany.spaceinvaders.sprite.get.SpriteRepository;
import nl.pancompany.spaceinvaders.sprite.move.MoveSpriteCommandHandler;
import nl.pancompany.spaceinvaders.sprite.restinpeace.RestInPeaceSpriteCommandHandler;
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
        GameStopper gameStopper = new GameStopper(eventStore); // TODO: Remove when only automation
        // Player
        TurnSpriteCommandHandler turnSpriteCommandHandler = new TurnSpriteCommandHandler(eventStore);
        StopPlayerCommandHandler stopPlayerCommandHandler = new StopPlayerCommandHandler(eventStore);
        // Sprite
        ChangeSpriteImageCommandHandler changeSpriteImageCommandHandler = new ChangeSpriteImageCommandHandler(eventStore);
        TriggerSpriteExplosionCommandHandler triggerSpriteExplosionCommandHandler = new TriggerSpriteExplosionCommandHandler(eventStore);
        RestInPeaceSpriteCommandHandler restInPeaceSpriteCommandHandler = new RestInPeaceSpriteCommandHandler(eventStore);
        MoveSpriteCommandHandler moveSpriteCommandHandler = new MoveSpriteCommandHandler(eventStore);

        commandApi = CommandApi.builder()
                // Game
                .createGameCommandHandler(createGameCommandHandler)
                .initiateGameCycleCommandHandler(initiateGameCycleCommandHandler)
                .gameStopper(gameStopper)
                // Player
                .turnSpriteCommandHandler(turnSpriteCommandHandler)
                .stopPlayerCommandHandler(stopPlayerCommandHandler)
                // Sprite
                .changeSpriteImageCommandHandler(changeSpriteImageCommandHandler)
                .triggerSpriteExplosionCommandHandler(triggerSpriteExplosionCommandHandler)
                .restInPeaceSpriteCommandHandler(restInPeaceSpriteCommandHandler)
                .moveSpriteCommandHandler(moveSpriteCommandHandler) // TODO: Check whether used!
                .build();

        // Query handlers, projectors and repositories
        // Game (live model)
        GameQueryHandler gameQueryHandler = new GameQueryHandler();
        // Sprite
        SpriteRepository spriteRepository = new SpriteRepository();
        SpriteProjector spriteProjector = new SpriteProjector(spriteRepository);
        SpriteQueryHandler spriteQueryHandler = new SpriteQueryHandler(spriteRepository);
        queryApi = QueryApi.builder()
                // Sprite
                .spriteQueryHandler(spriteQueryHandler)
                .gameQueryHandler(gameQueryHandler)
                .build();

        // Query event-handler registrations
        eventBus.registerAsynchronousEventHandler(spriteProjector);
        eventBus.registerAsynchronousEventHandler(gameQueryHandler);

        // Automations
        // Game
        // TODO: Add GameStopper (see above)
        // Player
        SpriteCreator spriteCreator = new SpriteCreator(eventStore);
        PlayerMover playerMover = new PlayerMover(eventStore);
        // Alien
        // TODO: Automation alienMover seem to cause high cpu
        AliensMover aliensMover = new AliensMover(eventStore);
        AlienDropper alienDropper = new AlienDropper(eventStore);

        // Automation event-handler registrations
        // Game
        eventBus.registerAsynchronousEventHandler(gameStopper);
        // Player
        eventBus.registerAsynchronousEventHandler(spriteCreator);
        eventBus.registerAsynchronousEventHandler(playerMover);
        // Alien
        eventBus.registerAsynchronousEventHandler(aliensMover);
        eventBus.registerAsynchronousEventHandler(alienDropper);
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
