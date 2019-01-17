package com.openbots.api.bot.common_tasks;

import com.openbots.api.bot.OpenTask;
import com.runemate.game.api.hybrid.local.hud.interfaces.Health;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceWindows;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import com.runemate.game.api.hybrid.util.calculations.Random;
import com.runemate.game.api.script.Execution;

public class FoodEater extends OpenTask {

    private int triggerPercent = 50;
    private SpriteItem food;

    @Override
    public boolean validate() {
        //If our current hp is less than the set trigger percent and we have food
        return Health.getCurrentPercent() < triggerPercent && (food = Inventory.newQuery().actions("Eat").results().first()) != null;
    }

    @Override
    public void execute() {
        if (!InterfaceWindows.getInventory().isOpen()) {
            //If the inventory isn't open, open it
            bot.setStatus("Opening Inventory");
            InterfaceWindows.getInventory().open();
        } else {
            //If inventory is open, eat the food
            String name = bot.getItemName(food);
            bot.setStatus("Eating " + name);
            if (food.interact("Eat", name)) {
                bot.setStatus("Waiting for food to be eaten");
                if (Execution.delayUntil(() -> !food.isValid(), (int) Random.nextGaussian(600, 2000, 1500))) {
                    bot.setStatus("Food eaten successfully");
                } else {
                    bot.setStatus("Food took too long to invalidate");
                }
            }
        }
    }

    public void setTriggerPercent(int newValue) {
        triggerPercent = newValue;
    }
}
