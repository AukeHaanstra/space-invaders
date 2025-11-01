package nl.pancompany.spaceinvaders;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.spaceinvaders.game.prepare.PrepareGameCommandHandler;
import nl.pancompany.spaceinvaders.player.initializer.PlayerInitializer;

import javax.swing.*;
import java.awt.*;

public class SpaceInvaders extends JFrame  {

    static void main(String[] args) { // 1
        EventStore eventStore = new EventStore();
        EventBus eventBus = eventStore.getEventBus();

        // Command handlers
        PrepareGameCommandHandler prepareGameCommandHandler = new PrepareGameCommandHandler(eventStore);

        // Automations
        PlayerInitializer playerInitializer = new PlayerInitializer(eventStore);

        // Event-handler registrations
        eventBus.registerAsynchronousEventHandler(playerInitializer);

        CommandApi commandApi = CommandApi.builder()
                .prepareGameCommandHandler(prepareGameCommandHandler)
                .build();

        // UI
        Board board = new Board(commandApi);

        EventQueue.invokeLater(() -> {
            var ex = new SpaceInvaders(board);
            ex.setVisible(true);
        });

    }

    public SpaceInvaders(Board board) { // 2
        initUI(board);
    }

    private void initUI(Board board) { // 3
        setTitle("Space Invaders");
        setLayout(new BorderLayout());
        setContentPane(board); // 4
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

}
