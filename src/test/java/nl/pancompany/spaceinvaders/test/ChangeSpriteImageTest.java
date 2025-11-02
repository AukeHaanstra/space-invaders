package nl.pancompany.spaceinvaders.test;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.data.SequencedEvent;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Tag;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.CommandApi;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteImageChanged;
import nl.pancompany.spaceinvaders.events.SpriteTurned;
import nl.pancompany.spaceinvaders.player.turn.TurnPlayer;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;
import nl.pancompany.spaceinvaders.sprite.changeimage.ChangeSpriteImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.Constants.*;
import static nl.pancompany.spaceinvaders.shared.Direction.RIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class ChangeSpriteImageTest {

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
    void given__whenTurnPlayer_thenIllegalState() {
        assertThatThrownBy(() -> commandApi.publish(new ChangeSpriteImage(SpriteId.random(), "path")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenPlayerCreated_whenChangeSpriteImage_thenSpriteImageChanged() {
        SpriteId spriteId = SpriteId.random();
        Tag spriteTag = Tag.of(SPRITE_ENTITY, spriteId.toString());
        SpriteCreated spriteCreated = new SpriteCreated(spriteId, "path1", 0, 0, 1, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, spriteTag));

        commandApi.publish(new ChangeSpriteImage(spriteId, "path2"));

        Query query = Query.of(spriteTag, Type.of(SpriteImageChanged.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(SpriteImageChanged.class)).isEqualTo(
                new SpriteImageChanged(spriteId, "path2"));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

}
