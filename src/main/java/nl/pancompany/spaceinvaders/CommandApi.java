package nl.pancompany.spaceinvaders;

import lombok.Builder;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.game.create.PrepareGameCommandHandler;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayer;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayerCommandHandler;

@Builder
public class CommandApi {

    private final PrepareGameCommandHandler prepareGameCommandHandler;
    private final TurnPlayerCommandHandler turnPlayerCommandHandler;

    public void publish(Object command) {
        switch (command) {
            case null -> throw new IllegalStateException("Null Command");
            case CreateGame createGame -> prepareGameCommandHandler.handle(createGame);
            case TurnPlayer turnPlayer -> turnPlayerCommandHandler.handle(turnPlayer);
            default -> throw new IllegalStateException("Unexpected Command: " + command);
        }

    }
}
