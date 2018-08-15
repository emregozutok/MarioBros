package com.mre.mariobros.tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mre.mariobros.MarioBros;
import com.mre.mariobros.screens.PlayScreen;
import com.mre.mariobros.sprites.enemies.Enemy;
import com.mre.mariobros.sprites.enemies.Goomba;
import com.mre.mariobros.sprites.enemies.Turtle;
import com.mre.mariobros.sprites.tileobjects.Brick;
import com.mre.mariobros.sprites.tileobjects.Coin;
import com.mre.mariobros.sprites.tileobjects.Pipe;

public class B2WorldCreator {
    private Array<Goomba> goombas;
    private Array<Turtle> turtles;

    public B2WorldCreator(PlayScreen playScreen) {
        World world = playScreen.getWorld();
        TiledMap map = playScreen.getMap();
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
            new Pipe(playScreen, obj);
        }

        // bricks
        for (MapObject obj : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            new Brick(playScreen, obj);
        }

        // coins
        for (MapObject obj : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            new Coin(playScreen, obj);
        }

        // goombas
        goombas = new Array<Goomba>();
        for (MapObject obj : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            goombas.add(new Goomba(playScreen, rect.x / MarioBros.PPM, rect.y / MarioBros.PPM));
        }

        // turtles
        turtles = new Array<Turtle>();
        for (MapObject obj : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            turtles.add(new Turtle(playScreen, rect.x / MarioBros.PPM, rect.y / MarioBros.PPM));
        }
    }

    public Array<Goomba> getGoombas() {
        return goombas;
    }

    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(turtles);
        return enemies;
    }
}
