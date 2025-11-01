package nl.pancompany.spaceinvaders;

import lombok.Getter;
import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.spaceinvaders.game.create.PrepareGameCommandHandler;
import nl.pancompany.spaceinvaders.player.creator.PlayerCreator;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayerCommandHandler;

import javax.swing.*;
import java.awt.*;

@Getter
public class SpaceInvaders extends JFrame  {

    CommandApi commandApi;

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
        PrepareGameCommandHandler prepareGameCommandHandler = new PrepareGameCommandHandler(eventStore);
        TurnPlayerCommandHandler turnPlayerCommandHandler = new TurnPlayerCommandHandler(eventStore);
        commandApi = CommandApi.builder()
                .prepareGameCommandHandler(prepareGameCommandHandler)
                .turnPlayerCommandHandler(turnPlayerCommandHandler)
                .build();

        // Query handlers

        // Automations
        PlayerCreator playerCreator = new PlayerCreator(eventStore);

        // Event-handler registrations
        eventBus.registerAsynchronousEventHandler(playerCreator);
    }

    private void initUI() { // 3
        setTitle("Space Invaders");
        setLayout(new BorderLayout());
        Board board = new Board(commandApi);
        setContentPane(board); // 4
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

}
