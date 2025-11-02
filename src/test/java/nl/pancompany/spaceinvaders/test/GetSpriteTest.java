package nl.pancompany.spaceinvaders.test;

import nl.pancompany.eventstore.EventBus;
import nl.pancompany.eventstore.EventStore;
import nl.pancompany.eventstore.data.Event;
import nl.pancompany.eventstore.query.Query;
import nl.pancompany.eventstore.query.Type;
import nl.pancompany.spaceinvaders.EntityTags;
import nl.pancompany.spaceinvaders.QueryApi;
import nl.pancompany.spaceinvaders.SpaceInvaders;
import nl.pancompany.spaceinvaders.events.SpriteCreated;
import nl.pancompany.spaceinvaders.events.SpriteTurned;
import nl.pancompany.spaceinvaders.shared.Direction;
import nl.pancompany.spaceinvaders.sprite.get.GetSpriteById;
import nl.pancompany.spaceinvaders.sprite.get.SpriteReadModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static nl.pancompany.spaceinvaders.Constants.*;
import static nl.pancompany.spaceinvaders.EntityTags.PLAYER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class GetSpriteTest {

    EventStore eventStore;
    QueryApi queryApi;
    EventBus eventBus;
    Query playerQuery = Query.taggedWith(PLAYER).build();

    @BeforeEach
    void setUp() {
        SpaceInvaders spaceInvaders = new SpaceInvaders(eventStore = new EventStore(), false);
        queryApi = spaceInvaders.getQueryApi();
        eventBus = eventStore.getEventBus();
    }

    @Test
    void givenNoSpriteCreated_thenNoReadModelFound() {
        assertThat(queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID))).isEmpty();
    }

    @Test
    void givenSpriteCreated_thenReadModelFound() {
        SpriteCreated spriteCreated = new SpriteCreated(PLAYER_SPRITE_ID, PLAYER_IMAGE_PATH, PLAYER_START_X,
                PLAYER_START_Y, PLAYER_SPEED, Direction.NONE);
        eventStore.append(Event.of(spriteCreated, PLAYER));
        await().untilAsserted(() -> assertThat(eventStore.read(playerQuery)).hasSize(1));

        Optional<SpriteReadModel> readModel = queryApi.query(new GetSpriteById(PLAYER_SPRITE_ID));

        assertThat(readModel).isPresent();
        assertThat(readModel.get().spriteId()).isEqualTo(PLAYER_SPRITE_ID);
        assertThat(readModel.get().imagePath()).isEqualTo(PLAYER_IMAGE_PATH);
        assertThat(readModel.get().x()).isEqualTo(PLAYER_START_X);
        assertThat(readModel.get().y()).isEqualTo(PLAYER_START_Y);
        assertThat(readModel.get().speed()).isEqualTo(PLAYER_SPEED);
        assertThat(readModel.get().direction()).isEqualTo(Direction.NONE);
    }

}
