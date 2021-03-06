package ld;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import ld.Control.*;
import ld.gfx.*;
import ld.ui.*;

import static ld.Game.*;

public class UI implements ApplicationListener{
    public Dialog paused, about;
    public SettingsDialog settings;
    public KeybindDialog keys;

    @Override
    public void init(){
        settings = new SettingsDialog();
        keys = new KeybindDialog();

        DefaultDialog.addCloseButton(settings);
        DefaultDialog.addCloseButton(keys);

        about = new DefaultDialog("About");
        about.cont.add(Game.about);

        paused = new Dialog("Paused");
        paused.cont.defaults().size(200f, 50f);

        paused.cont.button("Resume", () -> {
            control.state = State.playing;
            paused.hide();
        }).row();
        paused.cont.button("Settings", settings::show).row();
        paused.cont.button("Controls", keys::show).row();
        paused.cont.button("Menu", () -> {
            Dialog conf = new Dialog("Confirm");
            conf.cont.add("Are you sure you want to quit?");
            conf.buttons.defaults().size(120f, 50f);
            conf.buttons.button("OK", () -> {
                control.state = State.menu;
                conf.hide();
                paused.hide();
            });

            conf.buttons.button("Cancel", () -> {
                conf.hide();
            });

            conf.show();
        }).row();

        settings.main.sliderPref("sfxvol", "Sound Volume", 100, 0, 100, s -> s + "%");

        //menu
        Core.scene.table(t -> {
            t.visible(Game.control::menu);
            t.center();

            t.table(Tex.button, c -> {
                c.margin(4);
                c.defaults().size(200f, 50f);

                c.button("Play", Game.control::play).row();
                c.button("About", about::show).row();
                c.button("Settings", settings::show).row();
                c.button("Controls", keys::show);
            });
        });

        //playing stuff
        Core.scene.table(t -> {
            t.visible(Game.control::playing);

            t.top().table(Tex.button, bar -> {
                bar.rect((x, y, w, h) -> {
                    float fract = player.smoothHeat;
                    Draw.color(0x0e0d11ff);
                    Fill.crect(x, y, w, h);
                    Tmp.c1.set(0xff9940ff).lerp(Tmp.c2.set(0xfcd357ff), Mathf.absin(Time.time(), 20f, 1f)).lerp(Color.white, Mathf.clamp(player.hitTime));
                    Draw.color(Tmp.c1);
                    Fill.crect(x, y, Mathf.round(w * fract, 4), h);
                    Draw.color();
                }).grow();
            }).size(300f, 30f);
        });
    }

    public void showLoading(Runnable run){
        if(debug){
            run.run();
            return;
        }

        Core.app.post(() -> {
            Table table = Core.scene.table(Tex.window, t -> {
                t.table(Tex.button, g -> {
                    g.add("Generating...");
                });
            });
            table.toFront();
            Core.app.post(() -> Core.app.post(() -> {
                run.run();
                table.remove();
            }));
        });
    }

    public void flash(){
        Core.scene.table(Tex.white, t -> {
            t.touchable(Touchable.disabled);
            t.actions(Actions.fadeOut(2f, Interpolation.fade), Actions.remove());
        });
    }

    @Override
    public void update(){

        if(!control.playing()){
            Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
            Draw.flush();
            Draw.color();
            TextureRegion logo = Core.atlas.find("logo");
            int scale = 4;
            Draw.mixcol(Pal.fire1, 1f);
            Draw.rect(logo, Core.graphics.getWidth()/2f - 8, Core.graphics.getHeight() - logo.getHeight()/2f*scale - 8, logo.getWidth() * scale, logo.getHeight() * scale);

            Draw.mixcol(Pal.fire2, 1f);
            Draw.rect(logo, Core.graphics.getWidth()/2f, Core.graphics.getHeight() - logo.getHeight()/2f*scale, logo.getWidth() * scale, logo.getHeight() * scale);
            Draw.reset();
        }

        Core.scene.act();
        Core.scene.draw();

        if(!control.playing()){
            Draw.color();
            Draw.proj(Core.camera);
            Draw.rect(renderer.buffer);
        }

        Draw.flush();
    }

    @Override
    public void resize(int width, int height){
        Core.scene.resize(width, height);
    }
}
