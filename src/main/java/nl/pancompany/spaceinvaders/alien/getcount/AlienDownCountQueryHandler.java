package nl.pancompany.spaceinvaders.alien.getcount;

import nl.pancompany.eventstore.annotation.EventHandler;
import nl.pancompany.eventstore.annotation.ResetHandler;
import nl.pancompany.spaceinvaders.events.OneAlienDown;

// Actually query handler, projector and repository in one.
public class AlienDownCountQueryHandler {

    private int alienDownCount; // repo

    @ResetHandler
    void reset() {
        alienDownCount = 0;
    }

    @EventHandler // projector
    private void update(OneAlienDown oneAlienDown) {
        alienDownCount++;
    }

    public int get(GetAlienDownCount getAlienDownCount) { // query handler
        return alienDownCount;
    }
}
