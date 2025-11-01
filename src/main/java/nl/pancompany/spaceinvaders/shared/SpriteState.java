package nl.pancompany.spaceinvaders.shared;

import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.spaceinvaders.events.SpriteCreated;

public class SpriteState {

    private boolean visible;
    private String imagePath;
    private boolean dying;

    private int x;
    private int y;
    private int dx;

    @EventSourced
    private void handle(SpriteCreated spriteCreated) {
        visible = true;
    }
}
