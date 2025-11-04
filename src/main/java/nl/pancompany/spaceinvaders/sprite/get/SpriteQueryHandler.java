package nl.pancompany.spaceinvaders.sprite.get;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class SpriteQueryHandler {

    private final SpriteRepository spriteRepository;

    public Optional<SpriteReadModel> get(GetSpriteById getSpriteById) {
        return spriteRepository.findById(getSpriteById.spriteId());
    }

    public List<SpriteReadModel> get(GetSpriteByEntityName getSpriteByEntityName) {
        return spriteRepository.findAllByEntityName(getSpriteByEntityName.entityName());
    }
}
