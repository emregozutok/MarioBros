package com.mre.mariobros.sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mre.mariobros.MarioBros;
import com.mre.mariobros.screens.PlayScreen;

public class Mario extends Sprite {
    public enum State { FALLING, JUMPING, STANDING, RUNNING, GROWING }

    ;
    public State currentState;
    public State previousState;
    public final World world;
    public Body body;

    private TextureRegion marioStand;
    private Animation<TextureRegion> marioRun;
    private TextureRegion marioJump;
    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private Animation<TextureRegion> bigMarioRun;
    private Animation<TextureRegion> growMario;

    private float stateTimer;
    private boolean runningRight;
    private boolean isMarioBig;
    private boolean runGrowAnim;
    private boolean timeToDefineBigMario;

    public Mario(PlayScreen playScreen) {
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

        defineMario();
        setBounds(0, 0, 16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setRegion(marioStand);
    }

    public void update(float dt) {
        if (isMarioBig) {
            setPosition(body.getPosition().x - getWidth() * 0.5f, body.getPosition().y - getHeight() * 0.5f - 6 / MarioBros.PPM);
        } else {
            setPosition(body.getPosition().x - getWidth() * 0.5f, body.getPosition().y - getHeight() * 0.5f);
        }
        setRegion(getFrame(dt));
        if (timeToDefineBigMario) {
            defineBigMario();
        }
    }

    private TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;
        switch (currentState) {
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
        } else if ((body.getLinearVelocity().x > 0 || !runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    private State getState() {
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
        runGrowAnim = true;
        isMarioBig = true;
        setBounds(getX(), getY(), getWidth(), getHeight() * 2);
        MarioBros.manager.get("audio/sounds/powerup.wav", Sound.class).play();
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
        fdef.filter.maskBits = MarioBros.GROUND_BIT | MarioBros.BRICK_BIT | MarioBros.COIN_BIT | MarioBros.OBJECT_BIT | MarioBros.ENEMY_BIT | MarioBros.ENEMY_HEAD_BIT | MarioBros.ITEM_BIT;
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
        fdef.filter.maskBits = MarioBros.GROUND_BIT | MarioBros.BRICK_BIT | MarioBros.COIN_BIT | MarioBros.OBJECT_BIT | MarioBros.ENEMY_BIT | MarioBros.ENEMY_HEAD_BIT | MarioBros.ITEM_BIT;
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

    public boolean isBig() {
        return isMarioBig;
    }
}
