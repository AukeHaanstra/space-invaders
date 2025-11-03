package nl.pancompany.spaceinvaders.test.sprite;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteMoved;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;
import nl.pancompany.spaceinvaders.sprite.move.MoveSprite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.shared.Constants.SPRITE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class MoveSpriteTest {

    EventStore eventStore;
    CommandApi commandApi;
    EventBus eventBus;

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        commandApi = spaceInvaders.getCommandApi();
        eventBus = eventStore.getEventBus();
    }

    @Test
    void given__whenMoveSprite_thenIllegalState() {
        assertThatThrownBy(() -> commandApi.publish(new MoveSprite(SpriteId.random(), 4, 2))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenSpriteCreated_whenMoveSprite_thenSpriteMoved() {
        SpriteId spriteId = SpriteId.random();
        Tag spriteTag = Tag.of(SPRITE_ENTITY, spriteId.toString());
        SpriteCreated spriteCreated = new SpriteCreated(spriteId, "path", 0, 0, 0, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, spriteTag));

        commandApi.publish(new MoveSprite(spriteId, 4, 2));

        Query query = Query.of(spriteTag, Type.of(SpriteMoved.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(SpriteMoved.class)).isEqualTo(new SpriteMoved(spriteId, 4, 2));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

}
