package com.openbots.api.bot.common_tasks;

import com.openbots.api.bot.OpenTask;
import com.runemate.game.api.hybrid.local.hud.interfaces.ChatDialog;

public class ChatDialogContinuer extends OpenTask {
    private ChatDialog.Continue continueButton;

    @Override
    public boolean validate() {
        //Get the continue button
        return (continueButton = ChatDialog.getContinue()) != null;
    }

    @Override
    public void execute() {
        bot.setStatus("Continuing dialog");
        //press the continue button
        continueButton.select();
    }
}
