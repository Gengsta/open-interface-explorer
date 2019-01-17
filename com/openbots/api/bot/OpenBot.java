package com.openbots.api.bot;

import com.openbots.api.ui.OpenUi;
import com.runemate.game.api.client.embeddable.EmbeddableUI;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.Item;
import com.runemate.game.api.hybrid.entities.Npc;
import com.runemate.game.api.hybrid.entities.definitions.GameObjectDefinition;
import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition;
import com.runemate.game.api.hybrid.entities.definitions.NpcDefinition;
import com.runemate.game.api.hybrid.region.Region;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.framework.core.LoopingThread;
import com.runemate.game.api.script.framework.task.TaskBot;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class OpenBot extends TaskBot implements EmbeddableUI {
    private String status = "Starting bot";
    private StopWatch runtime = new StopWatch();
    private LoopingThread halfSecondLoopingThread;
    private List<Runnable> loopingThreadRunnables = new CopyOnWriteArrayList<>();
    private Map<Integer, String> npcNames = new HashMap<>();
    private Map<Integer, String> itemNames = new HashMap<>();
    private Map<Integer, String> gameObjectNames = new HashMap<>();
    private SimpleObjectProperty<OpenUi> uiProperty;

    /**
     * The Default constructor. It's advised to set the EmbeddableUI here.
     * Methods requiring metadata can't be used here, as the metadata isn't set yet.
     */
    public OpenBot() {
        //Set the embeddable UI
        //Our embeddable UI will be the same for all bots, and any extra UI components will be added to this via uiController.addTitledPane
        setEmbeddableUI(this);
    }

    public final void onStart(String... args) {
        //Start the looping thread responsible for firing the loopingThreadRunnables.
        //This is useful for updating time related information such as the runtime label, and exp per hour
        try {
            getPlatform().invokeAndWait(() -> {
                halfSecondLoopingThread = new LoopingThread(() -> loopingThreadRunnables.forEach(Runnable::run), 500);
                halfSecondLoopingThread.start();
                runtime.start();
                setLoopDelay(300, 600);
                //Use cache collision flags if on RS3
                if (Environment.isRS3()) {
                    Region.cacheCollisionFlags(true);
                }
                //Run the onStart that the bot has implemented, or the default if none are implemented.
                onStart();
            });
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Error occurred during OnStart");
            stop("error occurred during onStart");
        }
    }

    /**
     * Empty method, provides a replacement for onStart(String... args) as it is taken/reserved by the OpenBot class
     */
    public void onStart() {

    }

    /**
     * Sets the status of the bot, updating the UI's status label as well
     * @param status the new status
     */
    public void setStatus(String status) {
        this.status = status;
        OpenUi ui = botInterfaceProperty().get();
        if (ui != null) {
            ui.setStatus(status);
        }
    }

    /**
     *
     * @return the current status of the bot
     */
    public String getStatus() {
        return status;
    }

    /**
     * Adds a Runnable to the list of Runnables to be executed every half-second by halfSecondLoopingThread
     * @param runnable
     */
    public void addLoopingThreadRunnable(Runnable runnable) {
        loopingThreadRunnables.add(runnable);
    }

    /**
     * Gets the StopWatch used by the bot
     * @return the runtime stopwatch the bot uses
     */
    public StopWatch getRuntime() {
        return runtime;
    }

    /**
     *
     * @return the controller of the UI
     */
    public OpenUi getUi() {
        return botInterfaceProperty().get();
    }

    public boolean isUiLoaded() {
        return getUi() != null;
    }

    public String getNpcName(int id) {
        //Get the name from the map
        String name = npcNames.get(id);
        //If there is no stored name
        if (name == null) {
            //Load the definition
            NpcDefinition def = NpcDefinition.get(id);
            //If the definition is not null
            if (def != null) {
                //Set name to the name in the definition and offer it to the map
                name = def.getName();
                npcNames.put(id, name);
            }
        }
        return name;
    }

    public String getNpcName(Npc npc) {
        return getNpcName(npc.getId());
    }

    public String getItemName(int id) {
        //Get the name from the map
        String name = itemNames.get(id);
        //If there is no stored name
        if (name == null) {
            //Load the definition
            ItemDefinition def = ItemDefinition.get(id);
            //If the definition is not null
            if (def != null) {
                //Set name to the name in the definition and offer it to the map
                name = def.getName();
                itemNames.put(id, name);
            }
        }
        return name;
    }

    public String getItemName(Item item) {
        return getItemName(item.getId());
    }

    public String getGameObjectName(int id) {
        //Get the name from the map
        String name = gameObjectNames.get(id);
        //If there is no stored name
        if (name == null) {
            //Load the definition
            GameObjectDefinition def = GameObjectDefinition.get(id);
            //If the definition is not null
            if (def != null) {
                //Set name to the name in the definition and offer it to the map
                name = def.getName();
                gameObjectNames.put(id, name);
            }
        }
        return name;
    }

    public String getGameObjectName(GameObject gameObject) {
        return getGameObjectName(gameObject.getId());
    }

    @Override
    public ObjectProperty<OpenUi> botInterfaceProperty() {
        if (uiProperty == null) {
            uiProperty = new SimpleObjectProperty<>(new OpenUi(this));
        }
        return uiProperty;
    }
}
