package com.openbots.bots.open_interface_explorer.ui;

import com.openbots.api.ui.overlay.PaintListener;
import com.openbots.bots.open_interface_explorer.OpenInterfaceExplorer;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceContainer;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceContainers;
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces;
import com.runemate.game.api.hybrid.queries.InterfaceComponentQueryBuilder;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by SlashnHax on 25/01/2016.
 */
public class InterfaceExplorerController implements Initializable, PaintListener, MouseListener {
    @FXML
    private TreeView<String> interfacesTreeView;
    @FXML
    private TextArea informationTextArea;
    @FXML
    private TextArea queryText;
    @FXML
    private Button clearFilters;
    @FXML
    private ListView<Integer> containerList;
    @FXML
    private TextField containerText;
    @FXML
    private Button containerAdd;
    @FXML
    private Button containerRemove;
    @FXML
    private RadioButton visibilityNone;
    @FXML
    private ToggleGroup visibiltyFilterGroup;
    @FXML
    private RadioButton visibilityVisible;
    @FXML
    private RadioButton visibilityInvisible;
    @FXML
    private RadioButton textContains;
    @FXML
    private ToggleGroup textFilterGroup;
    @FXML
    private RadioButton textEquals;
    @FXML
    private ListView<String> textList;
    @FXML
    private TextField textText;
    @FXML
    private Button textAdd;
    @FXML
    private Button textRemove;
    @FXML
    private ListView<Color> colorList;
    @FXML
    private Spinner<Integer> colorRed;
    @FXML
    private Spinner<Integer> colorGreen;
    @FXML
    private Spinner<Integer> colorBlue;
    @FXML
    private Button colorAdd;
    @FXML
    private Button colorRemove;
    @FXML
    private ListView<String> actionList;
    @FXML
    private TextField actionText;
    @FXML
    private Button addAction;
    @FXML
    private Button removeAction;
    @FXML
    private ListView<Integer> spriteList;
    @FXML
    private TextField spriteText;
    @FXML
    private Button spriteAdd;
    @FXML
    private Button spriteRemove;
    @FXML
    private ListView<Integer> heightList;
    @FXML
    private TextField heightText;
    @FXML
    private Button heightAdd;
    @FXML
    private Button heightRemove;
    @FXML
    private ListView<Integer> widthList;
    @FXML
    private TextField widthText;
    @FXML
    private Button widthAdd;
    @FXML
    private Button widthRemove;
    @FXML
    private CheckBox grandchildrenCheckBox;
    @FXML
    private ListView<String> itemNamesList;
    @FXML
    private TextField itemNameText;
    @FXML
    private Button itemNameAdd;
    @FXML
    private Button itemNameRemove;
    @FXML
    private ListView<InterfaceComponent.Type> typeList;
    @FXML
    private ComboBox<InterfaceComponent.Type> typeBox;
    @FXML
    private Button typeAdd;
    @FXML
    private Button typeRemove;
    @FXML
    private VBox handlerBox;

    private InterfaceTreeItem treeRoot;
    private InterfaceTreeItem selected;
    private List<InterfaceComponent> clicked = new ArrayList<>();
    private StringProperty queryString = new SimpleStringProperty();
    private ObjectProperty<InterfaceComponentQueryBuilder> queryBuilder = new SimpleObjectProperty<>(Interfaces.newQuery());
    private final OpenInterfaceExplorer bot;
    private ObservableList<InterfaceComponent.Type> types = FXCollections.observableArrayList(InterfaceComponent.Type.values());

    public InterfaceExplorerController(OpenInterfaceExplorer bot) {
        this.bot = bot;
        bot.getOverlay().addMouseListener(this);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        bot.getPlatform().invokeLater(() -> {
            final List<InterfaceContainer> containers = InterfaceContainers.getLoaded();
            treeRoot = new InterfaceTreeItem(containers, queryBuilder.get());
            Platform.runLater(() -> interfacesTreeView.setRoot(treeRoot));
        });
//        Future<List<InterfaceContainer>> loadedContainers = bot.getPlatform().invokeLater(() -> InterfaceContainers.getLoaded());
//        try {
//
//            interfacesTreeView.setRoot((treeRoot = new InterfaceTreeItem(loadedContainers.get(), queryBuilder.get())));
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
        interfacesTreeView.setShowRoot(false);
        typeBox.setItems(types);
        addListenersAndActions();
        queryText.textProperty().bindBidirectional(queryString);
        colorRed.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 255));
        colorBlue.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 255));
        colorGreen.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 255, 255));
    }

    public void addListenersAndActions() {
        interfacesTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selected = (InterfaceTreeItem) newValue;
                try {
                    informationTextArea.setText(bot.getPlatform().invokeAndWait(selected::getInformation));
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                selected = null;
            }
        });

        clearFilters.setOnAction(event -> {
            containerList.getItems().clear();
            visibiltyFilterGroup.selectToggle(visibilityNone);
            textFilterGroup.selectToggle(textEquals);
            textList.getItems().clear();
            colorList.getItems().clear();
            actionList.getItems().clear();
            spriteList.getItems().clear();
            heightList.getItems().clear();
            widthList.getItems().clear();
            typeList.getItems().clear();
            grandchildrenCheckBox.setSelected(true);
            itemNamesList.getItems().clear();
        });

        containerList.getItems().addListener((ListChangeListener<? super Integer>) c -> {
            c.next();
            generateQuery();
        });
        containerAdd.setOnAction(event -> {
            try {
                containerList.getItems().add(Integer.parseInt(containerText.getText()));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input (" + containerText.getText() + "). Must be a number.");
            }
        });
        containerRemove.setOnAction(event -> containerList.getItems().remove(containerList.getSelectionModel().getSelectedItem()));

        visibiltyFilterGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> generateQuery());
        textFilterGroup.selectedToggleProperty().addListener(((observable, oldValue, newValue) -> generateQuery()));
        textList.getItems().addListener((ListChangeListener<? super String>) c -> {
            c.next();
            generateQuery();
        });
        textAdd.setOnAction(event -> textList.getItems().add(textText.getText()));
        textRemove.setOnAction(event -> textList.getItems().remove(textList.getSelectionModel().getSelectedItem()));

        colorList.getItems().addListener((ListChangeListener<? super Color>) c -> {
            c.next();
            generateQuery();
        });
        colorAdd.setOnAction(event -> colorList.getItems().add(new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue())));
        colorRemove.setOnAction(event -> colorList.getItems().remove(colorList.getSelectionModel().getSelectedItem()));

        actionList.getItems().addListener((ListChangeListener<? super String>) c -> {
            c.next();
            generateQuery();
        });
        addAction.setOnAction(event -> actionList.getItems().add(actionText.getText()));
        removeAction.setOnAction(event -> actionList.getItems().remove(actionList.getSelectionModel().getSelectedItem()));

        spriteList.getItems().addListener((ListChangeListener<? super Integer>) c -> {
            c.next();
            generateQuery();
        });
        spriteAdd.setOnAction(event -> {
            try {
                spriteList.getItems().add(Integer.parseInt(spriteText.getText()));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input (" + spriteText.getText() + "). Must be a number.");
            }
        });
        spriteRemove.setOnAction(event -> spriteList.getItems().remove(spriteList.getSelectionModel().getSelectedItem()));

        heightList.getItems().addListener((ListChangeListener<? super Integer>) c -> {
            c.next();
            generateQuery();
        });
        heightAdd.setOnAction(event -> {
            try {
                heightList.getItems().add(Integer.parseInt(heightText.getText()));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input (" + heightText.getText() + "). Must be a number.");
            }
        });
        heightRemove.setOnAction(event -> heightList.getItems().remove(heightList.getSelectionModel().getSelectedItem()));

        widthList.getItems().addListener((ListChangeListener<? super Integer>) c -> {
            c.next();
            generateQuery();
        });
        widthAdd.setOnAction(event -> {
            try {
                widthList.getItems().add(Integer.parseInt(widthText.getText()));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input (" + widthText.getText() + "). Must be a number.");
            }
        });
        widthRemove.setOnAction(event -> widthList.getItems().remove(widthList.getSelectionModel().getSelectedItem()));

        grandchildrenCheckBox.setOnAction(event -> generateQuery());

        itemNamesList.getItems().addListener((ListChangeListener<String>) c -> {
            c.next();
            generateQuery();
        });

        itemNameAdd.setOnAction(event -> itemNamesList.getItems().add(itemNameText.getText()));
        itemNameRemove.setOnAction(event -> itemNamesList.getItems().remove(itemNamesList.getSelectionModel().getSelectedItem()));

        typeBox.getSelectionModel().selectFirst();
        typeList.getItems().addListener((ListChangeListener<InterfaceComponent.Type>) c -> {
            c.next();
            generateQuery();
        });

        typeAdd.setOnAction(event -> typeList.getItems().add(typeBox.getValue()));
        typeRemove.setOnAction(event -> typeList.getItems().remove(typeList.getSelectionModel().getSelectedItem()));

        handlerBox.setSpacing(3);
        boolean rs3 = false;
        try {
            rs3 = bot.getPlatform().invokeAndWait(Environment::isRS3);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        Arrays.stream(rs3 ? GameEvents.RS3.values() : GameEvents.OSRS.values()).forEach(handler -> {
            boolean enabled = false;
            try {
                enabled = bot.getPlatform().invokeAndWait(() -> handler.isEnabled());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            CheckBox checkbox = new CheckBox(handler.name());
            checkbox.setSelected(enabled);
            checkbox.selectedProperty().addListener((o1, o2, newval) -> bot.getPlatform().invokeLater(() -> {
                if(newval) {
                    handler.enable();
                } else {
                    handler.disable();
                }
            }));
            handlerBox.getChildren().add(checkbox);
        });
    }

    public void generateQuery() {
        StringBuilder qStr = new StringBuilder("Interfaces.newQuery()");
        InterfaceComponentQueryBuilder qBuilder = Interfaces.newQuery();
        if (!containerList.getItems().isEmpty()) {
            qStr.append(".containers(");
            int[] containers = new int[containerList.getItems().size()];
            for (int i = 0; i < containers.length; i++) {
                containers[i] = containerList.getItems().get(i);
                if (i != 0)
                    qStr.append(", ");
                qStr.append(containers[i]);
            }
            qBuilder.containers(containers);
            qStr.append(")");
        }

        if (visibilityVisible.isSelected()) {
            qBuilder.visible();
            qStr.append(".visible()");
        } else if (visibilityInvisible.isSelected()) {
            qBuilder.invisible();
            qStr.append(".invisible()");
        }

        if (!textList.getItems().isEmpty()) {
            String[] texts = new String[textList.getItems().size()];
            for (int i = 0; i < texts.length; i++)
                texts[i] = textList.getItems().get(i);
            if (textContains.isSelected()) {
                qBuilder.textContains(texts);
                qStr.append(".textContains(");
            } else if (textEquals.isSelected()) {
                qBuilder.texts(texts);
                qStr.append(".texts(");
            }
            for (int i = 0; i < texts.length; i++) {
                if (i != 0)
                    qStr.append(", ");
                qStr.append('"').append(texts[i]).append('"');
            }
            qStr.append(")");
        }

        if (!colorList.getItems().isEmpty()) {
            Color[] colors = new Color[colorList.getItems().size()];
            qStr.append(".textColors(");
            for (int i = 0; i < colors.length; i++) {
                Color c = colorList.getItems().get(i);
                if (i != 0)
                    qStr.append(", ");
                colors[i] = c;
                qStr.append("new Color(").append(c.getRed()).append(", ").append(c.getGreen()).append(", ").append(c.getBlue()).append(")");
            }
            qBuilder.textColors(colors);
            qStr.append(")");
        }

        if (!actionList.getItems().isEmpty()) {
            qStr.append(".actions(");
            String[] actions = new String[actionList.getItems().size()];
            for (int i = 0; i < actions.length; i++) {
                if (i != 0)
                    qStr.append(", ");
                actions[i] = actionList.getItems().get(i);
                qStr.append('"').append(actions[i]).append('"');
            }
            qBuilder.actions(actions);
            qStr.append(")");
        }

        if (!spriteList.getItems().isEmpty()) {
            qStr.append(".sprites(");
            int[] sprites = new int[spriteList.getItems().size()];
            for (int i = 0; i < sprites.length; i++) {
                sprites[i] = spriteList.getItems().get(i);
                if (i != 0)
                    qStr.append(", ");
                qStr.append(sprites[i]);
            }
            qBuilder.sprites(sprites);
            qStr.append(")");
        }

        if (!heightList.getItems().isEmpty()) {
            qStr.append(".heights(");
            int[] heights = new int[heightList.getItems().size()];
            for (int i = 0; i < heights.length; i++) {
                heights[i] = heightList.getItems().get(i);
                if (i != 0)
                    qStr.append(", ");
                qStr.append(heights[i]);
            }
            qBuilder.heights(heights);
            qStr.append(")");
        }

        if (!widthList.getItems().isEmpty()) {
            qStr.append(".widths(");
            int[] widths = new int[widthList.getItems().size()];
            for (int i = 0; i < widths.length; i++) {
                widths[i] = widthList.getItems().get(i);
                if (i != 0)
                    qStr.append(", ");
                qStr.append(widths[i]);
            }
            qBuilder.widths(widths);
            qStr.append(")");
        }


        if (!grandchildrenCheckBox.isSelected()) {
            qStr.append(".grandchildren(false)");
            qBuilder.grandchildren(false);
        }

        if (!itemNamesList.getItems().isEmpty()) {
            String[] names = itemNamesList.getItems().toArray(new String[itemNamesList.getItems().size()]);
            qStr.append(".containedItem(ItemDefinition.getNamePredicate(");
            for (int i = 0; i < names.length; i++) {
                if (i != 0) {
                    qStr.append(", ");
                }
                qStr.append('\"').append(names[i]).append("\"");
            }
            qStr.append("))");
            qBuilder.containedItem(ItemDefinition.getNamePredicate(names));
        }

        if (!typeList.getItems().isEmpty()) {
            InterfaceComponent.Type[] types = typeList.getItems().toArray(new InterfaceComponent.Type[typeList.getItems().size()]);
            qStr.append(".types(InterfaceComponent.Type.");
            for (int i = 0; i < types.length; i++) {
                if (i != 0) {
                    qStr.append(", InterfaceComponent.Type.");
                }
                qStr.append(types[i]);
            }
            qStr.append(")");
            qBuilder.types(types);
        }

        qStr.append(".results();");

        queryString.set(qStr.toString());
        queryBuilder.set(qBuilder);
        applyFilters();
    }

    public void applyFilters() {
        System.out.println("Applying filters");
        try {
            Collection<InterfaceContainer> containers = bot.getPlatform().invokeAndWait(() -> InterfaceContainers.getLoaded());
            if (!containerList.getItems().isEmpty()) {
                containers.removeIf(i -> !containerList.getItems().contains(i.getIndex()));
            }
            bot.getPlatform().invokeAndWait(() -> treeRoot.init(containers, queryBuilder.get()));
            Iterator<TreeItem<String>> rootIter = treeRoot.getChildren().iterator();
            while (rootIter.hasNext()) {
                InterfaceTreeItem next = (InterfaceTreeItem) rootIter.next();
                if (next.getChildren().isEmpty()) {
                    rootIter.remove();
                }
            }
            System.out.println(treeRoot.getChildren());
            System.out.println("Filters applied");
            interfacesTreeView.setRoot(treeRoot);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //Search for interfaces containing the mouse click
        if (treeRoot != null) {
            System.out.println("Searching clicked");
            clicked = treeRoot.getClicked(new ArrayList<>(), bot, e.getPoint());
            System.out.println("Found clicked: " + clicked);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void onPaint(Graphics2D g) {
        if (selected != null) {
            g.setColor(Color.WHITE);
            selected.renderInterface(g);
        }
        if (!clicked.isEmpty()) {
            g.setColor(Color.RED);
            clicked.forEach(c -> {
                Rectangle bounds = c.getBounds();
                if (bounds != null) {
                    g.drawString(c.toString(), bounds.x, bounds.y);
                    g.draw(bounds);
                }
            });
        }
    }
}
