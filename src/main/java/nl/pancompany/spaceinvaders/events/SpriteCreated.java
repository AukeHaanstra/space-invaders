package nl.pancompany.spaceinvaders.events;

public record SpriteCreated() {

    private final String imagePath;
    private final int startX;
    private final int startY;
}
