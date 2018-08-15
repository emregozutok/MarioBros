package com.mre.mariobros.sprites.tileobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.mre.mariobros.MarioBros;
import com.mre.mariobros.scenes.Hud;
import com.mre.mariobros.screens.PlayScreen;
import com.mre.mariobros.sprites.Mario;
import com.mre.mariobros.sprites.items.ItemDef;
import com.mre.mariobros.sprites.items.Mushroom;

public class Coin extends InteractiveTileObject {
    private static TiledMapTileSet tileSet;
    private static final int BLANK_COIN = 28;
    public Coin(PlayScreen playScreen, MapObject obj) {
        super(playScreen, obj);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.COIN_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        Gdx.app.log("Coin", "Collision");
        if (getCell().getTile().getId() == BLANK_COIN) {
            MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
        } else {
            getCell().setTile(tileSet.getTile(BLANK_COIN));
            if (obj.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBros.PPM), Mushroom.class));
                MarioBros.manager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            } else {
                MarioBros.manager.get("audio/sounds/coin.wav", Sound.class).play();
            }
            Hud.addScore(100);
        }
    }
}
