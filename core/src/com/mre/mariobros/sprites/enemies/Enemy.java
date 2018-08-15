package com.mre.mariobros.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.mre.mariobros.screens.PlayScreen;
import com.mre.mariobros.sprites.Mario;

public abstract class Enemy extends Sprite {
    protected final World world;
    protected final PlayScreen screen;

    public Body body;
    public Vector2 velocity;

    public Enemy(PlayScreen screen, float x, float y) {
        this.world = screen.getWorld();
        this.screen = screen;
        setPosition(x, y);
        defineEnemy();
        velocity = new Vector2(-1, -2);
        body.setActive(false);
    }

    protected abstract void defineEnemy();
    public abstract void hitOnHead(Mario userData);
    public abstract void hitByEnemy(Enemy enemy);
    public abstract void update(float dt);
    // public abstract void onEnemyHit(Enemy enemy);

    public void reverseVelocity(boolean x, boolean y) {
        if (x) {
            velocity.x = -velocity.x;
        }
        if (y) {
            velocity.y = -velocity.y;
        }
    }
}
