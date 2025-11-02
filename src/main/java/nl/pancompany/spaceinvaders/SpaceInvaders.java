package nl.pancompany.spaceinvaders;

import lombok.Getter;
import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.spaceinvaders.game.create.CreateGameCommandHandler;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycleCommandHandler;
import nl.pancompany.spaceinvaders.player.creator.PlayerCreator;
import nl.pancompany.spaceinvaders.player.mover.PlayerMover;
import nl.pancompany.spaceinvaders.player.stop.StopPlayerCommandHandler;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayerCommandHandler;
import nl.pancompany.spaceinvaders.sprite.Sprite;
import nl.pancompany.spaceinvaders.sprite.get.SpriteProjector;
import nl.pancompany.spaceinvaders.sprite.get.SpriteQueryHandler;
import nl.pancompany.spaceinvaders.sprite.get.SpriteRepository;

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
        CreateGameCommandHandler createGameCommandHandler = new CreateGameCommandHandler(eventStore);
        TurnPlayerCommandHandler turnPlayerCommandHandler = new TurnPlayerCommandHandler(eventStore);
        InitiateGameCycleCommandHandler initiateGameCycleCommandHandler = new InitiateGameCycleCommandHandler(eventStore);
        StopPlayerCommandHandler stopPlayerCommandHandler = new StopPlayerCommandHandler(eventStore);
        commandApi = CommandApi.builder()
                // Game
                .createGameCommandHandler(createGameCommandHandler)
                .initiateGameCycleCommandHandler(initiateGameCycleCommandHandler)
                // Player
                .turnPlayerCommandHandler(turnPlayerCommandHandler)
                .stopPlayerCommandHandler(stopPlayerCommandHandler)
                .build();

        // Query handlers, projectors and repositories
        // Sprite
        SpriteRepository spriteRepository = new SpriteRepository();
        SpriteProjector spriteProjector = new SpriteProjector(spriteRepository);
        SpriteQueryHandler spriteQueryHandler = new SpriteQueryHandler(spriteRepository);
        queryApi = QueryApi.builder()
                // Sprite
                .spriteQueryHandler(spriteQueryHandler)
                .build();

        // Query event-handler registrations
        eventBus.registerAsynchronousEventHandler(spriteProjector);

        // Automations
        PlayerCreator playerCreator = new PlayerCreator(eventStore);
        PlayerMover playerMover = new PlayerMover(eventStore);

        // Automation event-handler registrations
        eventBus.registerAsynchronousEventHandler(playerCreator);
        eventBus.registerAsynchronousEventHandler(playerMover);
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
