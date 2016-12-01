package gui.view.battle.handler;

import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import pokemon.Stat;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

public class MessageState implements VisualStateHandler {

    @Override
    public void set(BattleView view) {}

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x3), 0, 439, null);

        g.setColor(Color.BLACK);
        DrawUtils.setFont(g, 30);

        DrawUtils.drawWrappedText(g, view.message, 30, 490, 720);

        if (view.state == VisualState.STAT_GAIN) {
            g.drawImage(tiles.getTile(0x5), 0, 280, null);
            g.setColor(Color.BLACK);
            for (int i = 0; i < Stat.NUM_STATS; i++)
            {
                DrawUtils.setFont(g, 16);
                g.drawString(Stat.getStat(i, false).getName(), 25, 314 + i*21);

                DrawUtils.drawRightAlignedString(g, (view.statGains[i] < 0 ? "" : " + ") + view.statGains[i], 206, 314 + i*21);
                DrawUtils.drawRightAlignedString(g, view.newStats[i] + "", 247, 314 + i*21);
            }
        }
    }

    @Override
    public void update(BattleView view) {
        boolean pressed = false;
        InputControl input = InputControl.instance();

        // Consume input for mouse clicks and spacebars
        if (input.consumeIfMouseDown()) {
            pressed = true;
        }

        if (input.consumeIfDown(ControlKey.SPACE)) {
            pressed = true;
        }

        // Don't go to the next message if an animation is playing
        if (pressed && view.message != null && !view.playerAnimation.isAnimationPlaying() && !view.enemyAnimation.isAnimationPlaying()) {
            if (view.state == VisualState.STAT_GAIN) view.setVisualState(VisualState.MESSAGE);
            view.cycleMessage(false);
        }
    }
}
