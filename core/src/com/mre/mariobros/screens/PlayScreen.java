package com.mre.mariobros.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mre.mariobros.MarioBros;
import com.mre.mariobros.scenes.Hud;
import com.mre.mariobros.sprites.Mario;

public class PlayScreen implements Screen {

    private final MarioBros game;
    private final OrthographicCamera gameCam;
    private final Viewport gamePort;
    private final Hud hud;

    private final TmxMapLoader mapLoader;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;

    private final World world;
    private final Box2DDebugRenderer b2dr;
    private final Mario player;

    public PlayScreen(MarioBros game) {
        this.game = game;
        this.gameCam = new OrthographicCamera();
        this.gamePort = new FitViewport(MarioBros.V_WIDTH / MarioBros.PPM, MarioBros.V_HEIGHT / MarioBros.PPM, gameCam);
        this.hud = new Hud(game.batch);
        this.mapLoader = new TmxMapLoader();
        this.map = mapLoader.load("level1.tmx");
        this.renderer = new OrthogonalTiledMapRenderer(this.map, 1 / MarioBros.PPM);
        gameCam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);
        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        // ground
        for (MapObject obj : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / MarioBros.PPM, rect.getHeight() / 2 / MarioBros.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
        }

        // pipes
        for (MapObject obj : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / MarioBros.PPM, rect.getHeight() / 2 / MarioBros.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
        }

        // bricks
        for (MapObject obj : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / MarioBros.PPM, rect.getHeight() / 2 / MarioBros.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
        }

        // coins
        for (MapObject obj : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fdef.shape = shape;
            body.createFixture(fdef);
        }

        player = new Mario(world);
    }
    
    @Override
    public void show() {
        
    }

    private void handleInput(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            player.body.applyLinearImpulse(new Vector2(0, 4f), player.body.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.body.getLinearVelocity().x <= 2) {
            player.body.applyLinearImpulse(new Vector2(0.1f, 0), player.body.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.body.getLinearVelocity().x >= -2) {
            player.body.applyLinearImpulse(new Vector2(-0.1f, 0), player.body.getWorldCenter(), true);
        }
    }

    public void update(float dt) {
        handleInput(dt);

        world.step(1 / 60f, 6, 2);
        gameCam.position.x = player.body.getPosition().x;

        gameCam.update();
        renderer.setView(gameCam);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        b2dr.render(world, gameCam.combined);

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);

        hud.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
