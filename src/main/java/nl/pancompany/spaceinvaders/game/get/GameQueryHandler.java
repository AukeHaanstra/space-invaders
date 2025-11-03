package nl.pancompany.spaceinvaders.game.get;

import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.spaceinvaders.events.GameCreated;
import nl.pancompany.spaceinvaders.events.GameStopped;

import java.util.Optional;

public class GameQueryHandler {

    private GameReadModel gameReadModel;

    @EventHandler
    private void update(GameCreated gameCreated) {
        gameReadModel = new GameReadModel(true, Optional.empty());
    }

    @EventHandler
    private void update(GameStopped gameCreated) {
        gameReadModel = new GameReadModel(false, Optional.of(gameCreated.message()));
    }

    public Optional<GameReadModel> get() {
        return Optional.ofNullable(gameReadModel);
    }
}
