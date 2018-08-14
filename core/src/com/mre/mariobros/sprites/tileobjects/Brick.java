package com.mre.mariobros.sprites.tileobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.mre.mariobros.MarioBros;
import com.mre.mariobros.scenes.Hud;
import com.mre.mariobros.screens.PlayScreen;
import com.mre.mariobros.sprites.Mario;

public class Brick extends InteractiveTileObject {
    public Brick(PlayScreen playScreen, MapObject obj) {
        super(playScreen, obj);
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.BRICK_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        Gdx.app.log("Brick", "Collision");
        if (mario.isBig()) {
            setCategoryFilter(MarioBros.DESTROYED_BIT);
            getCell().setTile(null);
            Hud.addScore(200);

            MarioBros.manager.get("audio/sounds/breakblock.wav", Sound.class).play();
        } else {
            MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
        }
    }
}
