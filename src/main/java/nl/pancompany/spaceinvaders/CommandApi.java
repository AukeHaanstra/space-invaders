package nl.pancompany.spaceinvaders;

import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.RetryPolicy;
import dev.failsafe.function.CheckedRunnable;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.StateManager.StateManagerOptimisticLockingException;
import nl.pancompany.eventstore.data.SequencePosition;
import nl.pancompany.spaceinvaders.game.create.CreateGame;
import nl.pancompany.spaceinvaders.game.create.CreateGameCommandHandler;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycle;
import nl.pancompany.spaceinvaders.game.initiatecycle.InitiateGameCycleCommandHandler;
import nl.pancompany.spaceinvaders.game.resume.ResumeGame;
import nl.pancompany.spaceinvaders.game.resume.ResumeGameCommandHandler;
import nl.pancompany.spaceinvaders.game.stop.StopGame;
import nl.pancompany.spaceinvaders.game.stop.StopGameCommandHandler;
import nl.pancompany.spaceinvaders.laserbeam.cheat.ExterminateAliens;
import nl.pancompany.spaceinvaders.laserbeam.cheat.ExterminateAliensCommandHandler;
import nl.pancompany.spaceinvaders.player.stop.StopPlayer;
import nl.pancompany.spaceinvaders.player.stop.StopPlayerCommandHandler;
import nl.pancompany.spaceinvaders.laserbeam.create.CreateLaserBeam;
import nl.pancompany.spaceinvaders.laserbeam.create.CreateLaserBeamCommandHandler;
import nl.pancompany.spaceinvaders.sprite.changeimage.ChangeSpriteImage;
import nl.pancompany.spaceinvaders.sprite.changeimage.ChangeSpriteImageCommandHandler;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosion;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosionCommandHandler;
import nl.pancompany.spaceinvaders.sprite.move.MoveSprite;
import nl.pancompany.spaceinvaders.sprite.move.MoveSpriteCommandHandler;
import nl.pancompany.spaceinvaders.sprite.destroy.DestroySprite;
import nl.pancompany.spaceinvaders.sprite.destroy.DestroySpriteCommandHandler;
import nl.pancompany.spaceinvaders.sprite.turn.TurnSprite;
import nl.pancompany.spaceinvaders.sprite.turn.TurnSpriteCommandHandler;

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
    private final StopGameCommandHandler stopGameCommandHandler;
    private final ResumeGameCommandHandler resumeGameCommandHandler;
    // Player
    private final TurnSpriteCommandHandler turnSpriteCommandHandler;
    private final StopPlayerCommandHandler stopPlayerCommandHandler;
    // Sprite
    private final ChangeSpriteImageCommandHandler changeSpriteImageCommandHandler;
    private final TriggerSpriteExplosionCommandHandler triggerSpriteExplosionCommandHandler;
    private final DestroySpriteCommandHandler destroySpriteCommandHandler;
    private final MoveSpriteCommandHandler moveSpriteCommandHandler;
    // Laser
    private final CreateLaserBeamCommandHandler createLaserBeamCommandHandler;
    private final ExterminateAliensCommandHandler exterminateAliensCommandHandler;

    private final EventBus eventBus;

    private boolean replay = false;

    public void publish(Object command) {

        if (replay) {
            return; // No command handling when replay has started
        }

        switch (command) {
            case null -> throw new IllegalArgumentException("Null Command");

            // Game
            case CreateGame createGame -> COMMAND_EXECUTOR.accept(() -> createGameCommandHandler.decide(createGame));
            case InitiateGameCycle initiateGameCycle -> COMMAND_EXECUTOR.accept(() -> initiateGameCycleCommandHandler.decide(initiateGameCycle));
            case StopGame stopGame -> COMMAND_EXECUTOR.accept(() -> stopGameCommandHandler.decide(stopGame));
            case ResumeGame resumeGame -> COMMAND_EXECUTOR.accept(() -> resumeGameCommandHandler.decide(resumeGame));
            // Player
            case StopPlayer stopPlayer -> COMMAND_EXECUTOR.accept(() -> stopPlayerCommandHandler.decide(stopPlayer));
            // Sprite
            case TurnSprite turnSprite -> COMMAND_EXECUTOR.accept(() -> turnSpriteCommandHandler.decide(turnSprite));
            case ChangeSpriteImage changeSpriteImage -> COMMAND_EXECUTOR.accept(() -> changeSpriteImageCommandHandler.decide(changeSpriteImage));
            case TriggerSpriteExplosion triggerSpriteExplosion -> COMMAND_EXECUTOR.accept(() -> triggerSpriteExplosionCommandHandler.decide(triggerSpriteExplosion));
            case DestroySprite destroySprite -> COMMAND_EXECUTOR.accept(() -> destroySpriteCommandHandler.decide(destroySprite));
            case MoveSprite moveSprite -> COMMAND_EXECUTOR.accept(() -> moveSpriteCommandHandler.decide(moveSprite));
            // Laser
            case CreateLaserBeam createLaserBeam -> COMMAND_EXECUTOR.accept(() -> createLaserBeamCommandHandler.decide(createLaserBeam));
            case ExterminateAliens exterminateAliens -> COMMAND_EXECUTOR.accept(() -> exterminateAliensCommandHandler.decide(exterminateAliens));

            default -> throw new IllegalArgumentException("Unexpected Command: " + command);
        }
    }

    /**
     * @param endPosition exclusive, start from 0
     */
    public void replay(int endPosition) {
        replay = true;
        eventBus.replay(SequencePosition.of(endPosition));
    }

    public void resume() {
        eventBus.replay();
        replay = false;
    }

}
