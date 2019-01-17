package com.openbots.api.game.hybrid;

import com.runemate.game.api.hybrid.entities.LocatableEntity;
import com.runemate.game.api.hybrid.input.Mouse;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.local.hud.Menu;
import com.runemate.game.api.hybrid.local.hud.MenuItem;
import com.runemate.game.api.hybrid.location.navigation.basic.BresenhamPath;
import com.runemate.game.api.hybrid.util.calculations.Distance;
import com.runemate.game.api.hybrid.util.calculations.Random;

public class Interaction {
    public static boolean interact(LocatableEntity entity, String action, String name) {
        MenuItem item;
        if (Menu.isOpen()) {
            if ((item = Menu.getItem(action, name)) != null) {
                item.click();
            } else {
                Menu.close();
            }
        } else {
            if (entity.isVisible()) {
                return entity.interact(action, name) && Mouse.wasClickSuccessful(entity);
            } else if (Distance.to(entity) > Random.nextInt(5, 15)) {
                BresenhamPath p = BresenhamPath.buildTo(entity);
                if (p != null) {
                    p.step();
                }
            } else {
                Camera.concurrentlyTurnTo(entity);
            }
        }
        return false;
    }
}
