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
import nl.pancompany.spaceinvaders.events.SpriteExplosionTriggered;
import nl.pancompany.spaceinvaders.events.SpriteRestsInPeace;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.shared.ids.SpriteId;
import nl.pancompany.spaceinvaders.sprite.explode.TriggerSpriteExplosion;
import nl.pancompany.spaceinvaders.sprite.restinpeace.RestInPeaceSprite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nl.pancompany.spaceinvaders.Constants.SPRITE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

public class RestInPeaceSpriteTest {

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
    void given__whenRestInPeaceSprite_thenIllegalState() {
        assertThatThrownBy(() -> commandApi.publish(new RestInPeaceSprite(SpriteId.random())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenPlayerCreated_whenRestInPeaceSprite_thenSpriteRestsInPeace() {
        SpriteId spriteId = SpriteId.random();
        Tag spriteTag = Tag.of(SPRITE_ENTITY, spriteId.toString());
        SpriteCreated spriteCreated = new SpriteCreated(spriteId, "path", 0, 0, 1, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, spriteTag));

        commandApi.publish(new RestInPeaceSprite(spriteId));

        Query query = Query.of(spriteTag, Type.of(SpriteRestsInPeace.class));
        await().untilAsserted(() -> assertThat(eventStore.read(query)).hasSize(1));
        List<SequencedEvent> events = eventStore.read(query);
        assertThat(events.getFirst().payload(SpriteRestsInPeace.class)).isEqualTo(
                new SpriteRestsInPeace(spriteId));
        assertThat(eventBus.hasLoggedExceptions()).isFalse();
    }

}
