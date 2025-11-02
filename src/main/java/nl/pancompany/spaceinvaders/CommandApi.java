package nl.pancompany.spaceinvaders;

import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RetryPolicy;
import dev.failsafe.function.CheckedRunnable;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import nl.pancompany.eventstore.StateManager.StateManagerOptimisticLockingException;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.game.create.CreateGameCommandHandler;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycleCommandHandler;
import nl.pancompany.spaceinvaders.player.stop.StopPlayer;
import nl.pancompany.spaceinvaders.player.stop.StopPlayerCommandHandler;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayer;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayerCommandHandler;

import java.util.function.Consumer;

@Slf4j
@Builder
public class CommandApi {

    private static final RetryPolicy<Object> RETRY_POLICY = RetryPolicy.builder()
            .handle(StateManagerOptimisticLockingException.class)
            .withMaxRetries(10)
            .onFailedAttempt(event -> log.info("Command execution failed", event.getLastException()))
            .onRetry(event -> log.info("Command execution failure #{}. Retrying.", event.getAttemptCount()))
            .onRetriesExceeded(event -> log.warn("Failed to execute command. Max retries exceeded."))
            .onAbort(event -> log.warn("Command execution aborted.", event.getException()))
            .build();
    private static final FailsafeExecutor<Object> COMMAND_RETRY_EXECUTOR = Failsafe.with(RETRY_POLICY);
    public static final Consumer<CheckedRunnable> COMMAND_EXECUTOR = COMMAND_RETRY_EXECUTOR::run;

    private final CreateGameCommandHandler createGameCommandHandler;
    private final TurnPlayerCommandHandler turnPlayerCommandHandler;
    private final InitiateGameCycleCommandHandler initiateGameCycleCommandHandler;
    private final StopPlayerCommandHandler stopPlayerCommandHandler;

    public void publish(Object command) {
        switch (command) {
            case null -> throw new IllegalArgumentException("Null Command");
            // Game
            case CreateGame createGame -> COMMAND_EXECUTOR.accept(() ->createGameCommandHandler.decide(createGame));
            case InitiateGameCycle initiateGameCycle -> COMMAND_EXECUTOR.accept(() -> initiateGameCycleCommandHandler.decide(initiateGameCycle));
            // Player
            case TurnPlayer turnPlayer -> COMMAND_EXECUTOR.accept(() -> turnPlayerCommandHandler.decide(turnPlayer));
            case StopPlayer stopPlayer -> COMMAND_EXECUTOR.accept(() -> stopPlayerCommandHandler.decide(stopPlayer));

            default -> throw new IllegalArgumentException("Unexpected Command: " + command);
        }

    }
}
