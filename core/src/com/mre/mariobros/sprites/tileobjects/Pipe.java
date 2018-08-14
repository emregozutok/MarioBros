package com.mre.mariobros.sprites.tileobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.mre.mariobros.MarioBros;
import com.mre.mariobros.screens.PlayScreen;
import com.mre.mariobros.sprites.Mario;

public class Pipe extends InteractiveTileObject {
    public Pipe(PlayScreen playScreen, MapObject obj) {
        super(playScreen, obj);
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.OBJECT_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        Gdx.app.log("Pipe", "Collision");
    }
}
