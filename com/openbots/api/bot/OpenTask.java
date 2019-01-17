package com.openbots.api.bot;

import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.script.framework.task.Task;

public abstract class OpenTask extends Task {
    //Store a reference to the current bot
    protected OpenBot bot = (OpenBot) Environment.getBot();
}
