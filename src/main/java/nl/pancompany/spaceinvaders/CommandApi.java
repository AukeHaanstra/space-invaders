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
import nl.pancompany.spaceinvaders.sprite.turn.TurnSprite;
import nl.pancompany.spaceinvaders.sprite.turn.TurnSpriteCommandHandler;
import nl.pancompany.spaceinvaders.sprite.changeimage.ChangeSpriteImage;
import nl.pancompany.spaceinvaders.sprite.changeimage.ChangeSpriteImageCommandHandler;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosion;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosionCommandHandler;
import nl.pancompany.spaceinvaders.sprite.restinpeace.RestInPeaceSprite;
import nl.pancompany.spaceinvaders.sprite.restinpeace.RestInPeaceSpriteCommandHandler;

import java.util.function.Consumer;

@Slf4j
@Builder
public class CommandApi {

    private static final RetryPolicy<Object> RETRY_POLICY = RetryPolicy.builder()
            .handle(StateManagerOptimisticLockingException.class)
            .withMaxRetries(10)
            .onFailedAttempt(event -> log.info("Command execution failed:", event.getLastException()))
            .onRetry(event -> log.info("Command execution failure #{}. Retrying.", event.getAttemptCount()))
            .onRetriesExceeded(event -> log.warn("Failed to execute command. Max retries exceeded."))
            .abortOn(IllegalStateException.class)
            .onAbort(event -> log.warn("Command execution aborted:", event.getException()))
            .build();
    private static final FailsafeExecutor<Object> COMMAND_RETRY_EXECUTOR = Failsafe.with(RETRY_POLICY);
    public static final Consumer<CheckedRunnable> COMMAND_EXECUTOR = COMMAND_RETRY_EXECUTOR::run;

    // Game
    private final CreateGameCommandHandler createGameCommandHandler;
    private final InitiateGameCycleCommandHandler initiateGameCycleCommandHandler;
    // Player
    private final TurnSpriteCommandHandler turnSpriteCommandHandler;
    private final StopPlayerCommandHandler stopPlayerCommandHandler;
    // Sprite
    private final ChangeSpriteImageCommandHandler changeSpriteImageCommandHandler;
    private final TriggerSpriteExplosionCommandHandler triggerSpriteExplosionCommandHandler;
    private final RestInPeaceSpriteCommandHandler restInPeaceSpriteCommandHandler;

    public void publish(Object command) {
        switch (command) {
            case null -> throw new IllegalArgumentException("Null Command");
            // Game
            case CreateGame createGame -> COMMAND_EXECUTOR.accept(() ->createGameCommandHandler.decide(createGame));
            case InitiateGameCycle initiateGameCycle -> COMMAND_EXECUTOR.accept(() -> initiateGameCycleCommandHandler.decide(initiateGameCycle));
            // Player
            case TurnSprite turnSprite -> COMMAND_EXECUTOR.accept(() -> turnSpriteCommandHandler.decide(turnSprite));
            case StopPlayer stopPlayer -> COMMAND_EXECUTOR.accept(() -> stopPlayerCommandHandler.decide(stopPlayer));
            // Sprite
            case ChangeSpriteImage changeSpriteImage -> COMMAND_EXECUTOR.accept(() -> changeSpriteImageCommandHandler.decide(changeSpriteImage));
            case TriggerSpriteExplosion triggerSpriteExplosion -> COMMAND_EXECUTOR.accept(() -> triggerSpriteExplosionCommandHandler.decide(triggerSpriteExplosion));
            case RestInPeaceSprite restInPeaceSprite -> COMMAND_EXECUTOR.accept(() -> restInPeaceSpriteCommandHandler.decide(restInPeaceSprite));

            default -> throw new IllegalArgumentException("Unexpected Command: " + command);
        }

    }
}
