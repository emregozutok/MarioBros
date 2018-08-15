package com.mre.mariobros.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.mre.mariobros.MarioBros;
import com.mre.mariobros.screens.PlayScreen;
import com.mre.mariobros.sprites.Mario;

public class Turtle extends Enemy {
    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;
    public enum State { WALKING, STANDING_SHELL, MOVING_SHELL }

    public State currentState;
    public State previousState;

    private float stateTime;
    private Animation<TextureRegion> walk;
    private Array<TextureRegion> frames;
    private TextureRegion shell;
    // private float deadRotationDegrees;

    private boolean setToDestroy;
    private boolean destroyed;

    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 0, 0, 16, 24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 16, 0, 16, 24));
        walk = new Animation<TextureRegion>(0.2f, frames);
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"), 4 * 16, 0, 16, 24);
        currentState = previousState = State.WALKING;
        // deadRotationDegrees = 0;

        setBounds(getX(), getY(), 16 / MarioBros.PPM, 24 / MarioBros.PPM);
    }


    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.ENEMY_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.MARIO_BIT;
        fdef.shape = shape;
        body.createFixture(fdef).setUserData(this);

        PolygonShape head = new PolygonShape();
        Vector2[] vertice = new Vector2[4];
        vertice[0] = new Vector2(-5, 8).scl(1 / MarioBros.PPM);
        vertice[1] = new Vector2(5, 8).scl(1 / MarioBros.PPM);
        vertice[2] = new Vector2(-3, 3).scl(1 / MarioBros.PPM);
        vertice[3] = new Vector2(3, 3).scl(1 / MarioBros.PPM);
        head.set(vertice);
        fdef.shape = head;
        fdef.restitution = 1.8f;
        fdef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        body.createFixture(fdef).setUserData(this);
    }

    public TextureRegion getFrame(float dt) {
        TextureRegion region;
        switch (currentState) {
            case STANDING_SHELL:
            case MOVING_SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walk.getKeyFrame(stateTime, true);
                break;
        }
        if ((velocity.x > 0 && !region.isFlipX()) || (velocity.x < 0 && region.isFlipX())) {
            region.flip(true, false);
        }
        stateTime = currentState == previousState ? stateTime + dt : 0;
        previousState = currentState;
        return region;
    }

    @Override
    public void update(float dt) {
        setRegion(getFrame(dt));
        if (currentState == State.STANDING_SHELL && stateTime > 5) {
            currentState = State.WALKING;
            velocity.x = 1;
        }

        setPosition(body.getPosition().x - getWidth() * 0.5f, body.getPosition().y - 8 / MarioBros.PPM);
        body.setLinearVelocity(velocity);
        /*
        if (currentState == State.DEAD) {
            deadRotationDegrees += 3;
            rotate(deadRotationDegrees);
            if (stateTime > 5 && !destroyed) {
                world.destroyBody(body);
                destroyed = true;
            }
        } else {
            body.setLinearVelocity(velocity);
        }
        */
    }

    @Override
    public void hitOnHead(Mario mario) {
        /*
        if (currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        } else {
            kick(mario.getX() <= getX() ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED);
        }
        */
        if(currentState == State.STANDING_SHELL) {
            if(mario.body.getPosition().x > body.getPosition().x)
                velocity.x = -2;
            else
                velocity.x = 2;
            currentState = State.MOVING_SHELL;
        }
        else {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        }
    }

    @Override
    public void hitByEnemy(Enemy enemy) {
        reverseVelocity(true, false);
    }

    public void kick(int speed) {
        velocity.x = speed;
        currentState = State.MOVING_SHELL;
    }

    /*
    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Turtle) {
            Turtle turtle = (Turtle) enemy;
            if (turtle.currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL) {
                killed();
            } else if (currentState == State.MOVING_SHELL && turtle.currentState == State.WALKING) {
                return;
            } else {
                reverseVelocity(true, false);
            }
        } else if (currentState != State.MOVING_SHELL) {
            reverseVelocity(true, false);
        }
    }

    public void draw(Batch batch) {
        if (!destroyed) {
            super.draw(batch);
        }
    }

    public void killed() {
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;
        for (Fixture fix: body.getFixtureList()) {
            fix.setFilterData(filter);
        }
        body.applyLinearImpulse(new Vector2(0, 5f), body.getWorldCenter(), true);
    }

    public State getCurrentState() {
        return currentState;
    }
    */
}
