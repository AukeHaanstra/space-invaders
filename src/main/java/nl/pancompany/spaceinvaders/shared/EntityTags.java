package nl.pancompany.spaceinvaders.shared;

import nl.pancompany.eventstore.query.Tag;

public interface EntityTags {

    Tag GAME = Tag.of(Constants.GAME_ENTITY, Constants.GAME_ID);
    Tag PLAYER = Tag.of(Constants.SPRITE_ENTITY, Constants.PLAYER_SPRITE_ID.toString());
    Tag SHOT = Tag.of(Constants.SHOT_ENTITY, Constants.SHOT_SPRITE_ID.toString());

}
