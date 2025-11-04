package nl.pancompany.spaceinvaders.sprite;

import java.awt.Image;

public class Sprite {

    private boolean visible;
    private Image image;
    private boolean dying;

    int x;
    int y;
    int dx;

    public Sprite() {

        visible = true;
    }

    public void setDying(boolean dying) { // only used with: true, only for player and aliens that explode

        this.dying = dying;
    }

    public void die() {

        visible = false; // if exploded, it already died, so it disappears when we say Destroy
    }

    public void setImage(Image image) {

        this.image = image;
    }

    ///////

    public boolean isVisible() {

        return visible;
    }

    public Image getImage() {

        return image;
    }

    public void setX(int x) {

        this.x = x;
    }

    public void setY(int y) {

        this.y = y;
    }

    public int getY() {

        return y;
    }

    public int getX() {

        return x;
    }

    public boolean isDying() {

        return this.dying;
    }
}
