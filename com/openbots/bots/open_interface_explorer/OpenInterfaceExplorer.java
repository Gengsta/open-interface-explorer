package com.openbots.bots.open_interface_explorer;

import com.openbots.api.bot.OpenBot;
import com.openbots.api.ui.overlay.GameOverlay;
import com.openbots.bots.open_interface_explorer.ui.InterfaceExplorerController;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.util.Resources;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class OpenInterfaceExplorer extends OpenBot {
    private GameOverlay overlay;
    private InterfaceExplorerController interfaceExplorerUi;

    public void onStart() {
        overlay = new GameOverlay();
        overlay.setVisible(true);
        FXMLLoader loader = new FXMLLoader();
        Future<InputStream> uiFuture = getPlatform().invokeLater(() -> Resources.getAsStream("com/openbots/bots/open_interface_explorer/ui/interface_explorer_ui.fxml"));
        loader.setController(interfaceExplorerUi = new InterfaceExplorerController(this));
        Platform.runLater(() -> {
            try {
                getUi().addTitledPane(loader.load(uiFuture.get()));
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        overlay.addPaintListener(interfaceExplorerUi);
        GameEvents.Universal.INTERFACE_CLOSER.disable();
        GameEvents.Universal.LOBBY_HANDLER.disable();
        GameEvents.Universal.LOGIN_HANDLER.disable();
    }

    public void onStop() {
        overlay.dispose();
    }

    public GameOverlay getOverlay() {
        return overlay;
    }
}
