package nl.pancompany.spaceinvaders;

import lombok.Builder;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.game.create.CreateGameCommandHandler;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycleCommandHandler;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayer;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayerCommandHandler;

@Builder
public class CommandApi {

    private final CreateGameCommandHandler createGameCommandHandler;
    private final TurnPlayerCommandHandler turnPlayerCommandHandler;
    private final InitiateGameCycleCommandHandler initiateGameCycleCommandHandler;

    public void publish(Object command) {
        switch (command) {
            case null -> throw new IllegalStateException("Null Command");
            case CreateGame createGame -> createGameCommandHandler.decide(createGame);
            case TurnPlayer turnPlayer -> turnPlayerCommandHandler.decide(turnPlayer);
            case InitiateGameCycle initiateGameCycle -> initiateGameCycleCommandHandler.decide(initiateGameCycle);
            default -> throw new IllegalStateException("Unexpected Command: " + command);
        }

    }
}
