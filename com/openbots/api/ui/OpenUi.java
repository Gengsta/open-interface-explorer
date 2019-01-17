package com.openbots.api.ui;

import com.openbots.api.bot.OpenBot;
import com.openbots.api.resources.ResourceUtils;
import com.runemate.game.api.hybrid.util.Resources;
import com.runemate.game.api.script.data.ScriptMetaData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class OpenUi extends VBox implements Initializable {
    @FXML
    private Label runtimeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Accordion contentAccordian;

    private final OpenBot bot;
    public OpenUi(OpenBot bot) {
        this.bot = bot;
        Future<InputStream> fxmlStream = bot.getPlatform().invokeLater(() -> Resources.getAsStream("com/openbots/api/ui/open_ui.fxml"));

        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load(fxmlStream.get());
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        bot.getPlatform().invokeLater(() -> {
            final File opena = ResourceUtils.writeToFile("com/openbots/api/ui/opena.css");
            if (opena != null) {
                opena.deleteOnExit();
                final String uri = opena.toURI().toString();
                Platform.runLater(() -> getStylesheets().add(uri));
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            //Get the bot's metadata
            ScriptMetaData data = bot.getPlatform().invokeAndWait(bot::getMetaData);
            //Set the labels to match the metadata
            nameLabel.setText(data.getName());
            versionLabel.setText("Version: " + data.getVersion());
            authorLabel.setText("By: " + data.getAuthor());
            statusLabel.setText(bot.getStatus());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        //Add a runnable to update the runtime label to the bot
        bot.addLoopingThreadRunnable(() -> Platform.runLater(() -> runtimeLabel.setText(bot.getRuntime().getRuntimeAsString())));
    }

    public void addTitledPane(TitledPane pane) {
        Platform.runLater(() -> contentAccordian.getPanes().add(pane));
    }

    public void selectTitledPane(TitledPane pane) {
        Platform.runLater(() -> contentAccordian.setExpandedPane(pane));
    }

    public void addAndSelectTitledPane(TitledPane pane) {
        Platform.runLater(() -> {
            contentAccordian.getPanes().add(pane);
            contentAccordian.setExpandedPane(pane);
        });
    }

    public void setStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }
}
