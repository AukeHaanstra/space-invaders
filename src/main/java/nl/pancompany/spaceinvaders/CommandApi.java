package nl.pancompany.spaceinvaders;

import lombok.Builder;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.game.create.PrepareGameCommandHandler;

@Builder
public class CommandApi {

    private final PrepareGameCommandHandler prepareGameCommandHandler;

    public void publish(Object command) {
        switch (command) {
            case null -> throw new IllegalStateException("Null Command");
            case CreateGame createGame -> prepareGameCommandHandler.handle(createGame);
            default -> throw new IllegalStateException("Unexpected Command: " + command);
        }

    }
}
