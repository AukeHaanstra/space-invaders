package nl.pancompany.spaceinvaders.game.stopper;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;

@RequiredArgsConstructor
public class GameStopper {

    private final EventStore eventStore;

}
