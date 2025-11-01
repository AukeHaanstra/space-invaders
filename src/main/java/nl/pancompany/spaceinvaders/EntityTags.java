package nl.pancompany.spaceinvaders;

import nl.pancompany.eventstore.query.Tag;

public interface EntityTags {

    Tag GAME = Tag.of(Constants.GAME_ENTITY, Constants.GAME_ID);
    Tag PLAYER = Tag.of(Constants.PLAYER_ENTITY, Constants.PLAYER_ID);
    Tag SHOT = Tag.of(Constants.SHOT_ENTITY, Constants.SHOT_ID);

}
