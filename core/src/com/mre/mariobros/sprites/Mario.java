package com.mre.mariobros.sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mre.mariobros.MarioBros;
import com.mre.mariobros.screens.PlayScreen;
import com.mre.mariobros.sprites.enemies.Enemy;
import com.mre.mariobros.sprites.enemies.Turtle;
import com.mre.mariobros.sprites.other.FireBall;

public class Mario extends Sprite {

    public enum State {FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD}
    public State currentState;
    public State previousState;

    public final World world;
    public Body body;

    private TextureRegion marioStand;
    private Animation<TextureRegion> marioRun;
    private TextureRegion marioJump;
    private TextureRegion marioDead;
    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private Animation<TextureRegion> bigMarioRun;
    private Animation<TextureRegion> growMario;

    private float stateTimer;
    private boolean runningRight;
    private boolean isMarioBig;
    private boolean runGrowAnim;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;
    private boolean isMarioDead;

    private PlayScreen screen;

    private Array<FireBall> fireBalls;

    public Mario(PlayScreen playScreen) {
        this.screen = playScreen;
        this.world = playScreen.getWorld();
        this.currentState = State.STANDING;
        this.previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(playScreen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);

        frames.clear();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(playScreen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        }
        bigMarioRun = new Animation(0.1f, frames);

        frames.clear();
        frames.add(new TextureRegion(playScreen.getAtlas().findRegion("big_mario"), 15 * 16, 0, 16, 32));
        frames.add(new TextureRegion(playScreen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(playScreen.getAtlas().findRegion("big_mario"), 15 * 16, 0, 16, 32));
        frames.add(new TextureRegion(playScreen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        growMario = new Animation<TextureRegion>(0.2f, frames);

        marioJump = new TextureRegion(playScreen.getAtlas().findRegion("little_mario"), 5 * 16, 0, 16, 16);
        bigMarioJump = new TextureRegion(playScreen.getAtlas().findRegion("big_mario"), 5 * 16, 0, 16, 32);

        marioStand = new TextureRegion(playScreen.getAtlas().findRegion("little_mario"), 0, 0, 16, 16);
        bigMarioStand = new TextureRegion(playScreen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32);

        marioDead = new TextureRegion(playScreen.getAtlas().findRegion("little_mario"), 6 * 16, 0, 16, 16);

        defineMario();
        setBounds(0, 0, 16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setRegion(marioStand);

        fireBalls = new Array<FireBall>();
    }

    public void update(float dt) {
        // time is up : too late mario dies T_T
        // the !isDead() method is used to prevent multiple invocation
        // of "die music" and jumping
        // there is probably better ways to do that but it works for now
        if (screen.getHud().isTimeUp() && !isDead()) {
            die();
        }

        //update our sprite to correspond with the position of our Box2D body
        if (isMarioBig) {
            setPosition(body.getPosition().x - getWidth() * 0.5f, body.getPosition().y - getHeight() * 0.5f - 6 / MarioBros.PPM);
        } else {
            setPosition(body.getPosition().x - getWidth() * 0.5f, body.getPosition().y - getHeight() * 0.5f);
        }
        setRegion(getFrame(dt));
        if (timeToDefineBigMario) {
            defineBigMario();
        } else if (timeToRedefineMario) {
            redefineMario();
        }

        for (FireBall fb : fireBalls) {
            fb.update(dt);
            if (fb.isDestroyed()) {
                fireBalls.removeValue(fb, true);
            }
        }
    }

    private TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;
        switch (currentState) {
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer);
                if (growMario.isAnimationFinished(stateTimer)) {
                    runGrowAnim = false;
                }
                break;
            case JUMPING:
                region = isMarioBig ? bigMarioJump : marioJump;
                break;
            case RUNNING:
                region = isMarioBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = isMarioBig ? bigMarioStand : marioStand;
        }
        if ((body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        } else if ((body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    private State getState() {
        if (isMarioDead) {
            return State.DEAD;
        }
        if (runGrowAnim) {
            return State.GROWING;
        }
        if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        }
        if (body.getLinearVelocity().y < 0) {
            return State.FALLING;
        }
        if (body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        }
        return State.STANDING;
    }

    public void grow() {
        if (!isBig()) {
            runGrowAnim = true;
            isMarioBig = true;
            timeToDefineBigMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() * 2);
            MarioBros.manager.get("audio/sounds/powerup.wav", Sound.class).play();
        }
    }

    public void die() {
        if (!isDead()) {
            MarioBros.manager.get("audio/music/mario_music.ogg", Music.class).stop();
            MarioBros.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
            isMarioDead = true;
            Filter filter = new Filter();
            filter.maskBits = MarioBros.NOTHING_BIT;
            for (Fixture fix : body.getFixtureList()) {
                fix.setFilterData(filter);
            }
            body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
        }
    }

    public void jump(){
        if ( currentState != State.JUMPING ) {
            body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
            currentState = State.JUMPING;
        }
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Turtle) {
            Turtle turtle = (Turtle) enemy;
            if (turtle.currentState == Turtle.State.STANDING_SHELL) {
                turtle.kick(getX() <= turtle.getX() ? Turtle.KICK_RIGHT_SPEED : Turtle.KICK_LEFT_SPEED);
            }
        } else {
            if (isMarioBig) {
                isMarioBig = false;
                timeToRedefineMario = true;
                setBounds(getX(), getY(), getWidth(), getHeight() * 0.5f);
                MarioBros.manager.get("audio/sounds/powerdown.wav", Sound.class).play();
            } else {
                die();
            }
        }
    }

    public void redefineMario() {
        Vector2 currentPos = body.getPosition();
        // destroy little mario
        world.destroyBody(body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(currentPos);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;
        fdef.shape = shape;
        body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        body.createFixture(fdef).setUserData(this);

        timeToRedefineMario = false;
    }

    private void defineMario() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(32 / MarioBros.PPM, 32 / MarioBros.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;
        fdef.shape = shape;
        body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        body.createFixture(fdef).setUserData(this);
    }

    private void defineBigMario() {
        Vector2 currentPos = body.getPosition();
        // destroy little mario
        world.destroyBody(body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(currentPos.add(0, 10 / MarioBros.PPM));
        bdef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fdef.filter.categoryBits = MarioBros.MARIO_BIT;
        fdef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;
        fdef.shape = shape;
        body.createFixture(fdef).setUserData(this);

        shape.setPosition(new Vector2(0, -14 / MarioBros.PPM));
        body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fdef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;

        body.createFixture(fdef).setUserData(this);

        timeToDefineBigMario = false;
    }

    public void fire() {
        fireBalls.add(new FireBall(screen, body.getPosition().x, body.getPosition().y, runningRight));
    }

    public void draw(Batch batch) {
        super.draw(batch);
        for (FireBall fb : fireBalls) {
            fb.draw(batch);
        }
    }

    public boolean isBig() {
        return isMarioBig;
    }

    public boolean isDead() {
        return isMarioDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }
}
