package nl.pancompany.spaceinvaders.game.get;

import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.ResetHandler;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameResumed;
import nl.pancompany.spaceinvaders.events.GameStopped;

import java.util.Optional;

// Actually query handler, projector and repository in one.
public class GameQueryHandler {

    private GameReadModel gameReadModel; // repo

    @ResetHandler
    void reset() {
        gameReadModel = new GameReadModel(false, true, Optional.empty());
    }

    @EventHandler // projector
    private void update(GameCreated gameCreated) {
        gameReadModel = new GameReadModel(true, false, Optional.empty());
    }

    @EventHandler
    private void update(GameStopped gameCreated) {
        gameReadModel = new GameReadModel(false, false, Optional.of(gameCreated.message()));
    }

    @EventHandler
    private void update(GameResumed gameResumed) {
        gameReadModel = new GameReadModel(true, false, Optional.empty());
    }

    public Optional<GameReadModel> get(GetGame getGame) { // query handler
        return Optional.ofNullable(gameReadModel);
    }
}
