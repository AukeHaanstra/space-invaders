package nl.pancompany.spaceinvaders.sprite.move;

import lombok.RequiredArgsConstructor;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.StateManager;
import nl.pancompany.eventstore.annotation.EventSourced;
import nl.pancompany.eventstore.annotation.StateCreator;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Tags;
import nl.pancompany.eventstore.query.Types;
import nl.pancompany.spaceinvaders.shared.EntityTags;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteMoved;
import nl.pancompany.spaceinvaders.events.SpriteDestroyed;
import nl.pancompany.spaceinvaders.events.SpriteTurned;
import nl.pancompany.spaceinvaders.shared.Direction;

import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;

@RequiredArgsConstructor
public class MoveSpriteCommandHandler {

    private final EventStore eventStore;

    public void decide(MoveSprite moveSprite) {

    }

}
