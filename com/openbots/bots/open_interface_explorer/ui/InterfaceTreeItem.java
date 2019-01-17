package com.openbots.bots.open_interface_explorer.ui;

import com.openbots.bots.open_interface_explorer.OpenInterfaceExplorer;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.entities.attributes.Attribute;
import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition;
import com.runemate.game.api.hybrid.entities.definitions.NpcDefinition;
import com.runemate.game.api.hybrid.entities.status.CombatGauge;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceContainer;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.queries.InterfaceComponentQueryBuilder;
import javafx.scene.control.TreeItem;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Created by SlashnHax on 25/01/2016.
 */
public class InterfaceTreeItem extends TreeItem<String> {
    private InterfaceContainer container;
    private InterfaceComponent component;
    public InterfaceTreeItem(Collection<InterfaceContainer> containers, InterfaceComponentQueryBuilder builder) {
        init(containers, builder);
    }

    public InterfaceTreeItem(InterfaceContainer container) {
        init(container);
    }

    public InterfaceTreeItem(InterfaceComponent component) {
        init(component);
    }

    public void cull(InterfaceComponentQueryBuilder builder) {
        Iterator<TreeItem<String>> itemIterator = getChildren().iterator();
        while (itemIterator.hasNext()) {
            InterfaceTreeItem item = (InterfaceTreeItem) itemIterator.next();
            item.cull(builder);
            if (item.getChildren().isEmpty() && !builder.accepts(item.component)
                    || !builder.grandchildren() && item.component.getParentComponent() != null) {
                itemIterator.remove();
            }
        }
    }

    public void init(Collection<InterfaceContainer> containers, InterfaceComponentQueryBuilder builder) {
        getChildren().clear();
        for (InterfaceContainer c : containers) {
            getChildren().add(new InterfaceTreeItem(c));
        }
        for (TreeItem t : getChildren()) {
            ((InterfaceTreeItem) t).cull(builder);
        }
    }

    public void init(InterfaceContainer container) {
        getChildren().clear();
        setValue(container.toString());
        this.container = container;
        for (InterfaceComponent c : container.getComponents()) {
            getChildren().add(new InterfaceTreeItem(c));
        }
    }

    public void init(InterfaceComponent component) {
        getChildren().clear();
        setValue(component.toString());
        this.component = component;
        for (InterfaceComponent c : component.getChildren())
            getChildren().add(new InterfaceTreeItem(c));
    }


    static String escapeChars(String initialText) {
        StringBuilder builder = new StringBuilder();
        for (char c : initialText.toCharArray()) {
            switch (c) {
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    public String getInformation() {
        Collection<String> lines = new ArrayList<>();
        if (container != null) {
            lines.add("Index: " + container.getIndex());
            lines.add("Components: " + container.getComponents().size());
        } else if (component != null) {
            lines.add("Index: " + component.getIndex());
            lines.add("Components: " + component.getChildren().size());
            lines.add("Bounds: " + getBoundsString(component.getBounds()));
            lines.add("Visible: " + component.isVisible());
            lines.add("Text: " + component.getText());
            lines.add("Escaped text: " + escapeChars(component.getText()));
            lines.add("Type: " + component.getType());
            lines.add("Text Color: " + getColorString(component.getTextColor()));
            lines.add("Content Type: " + component.getType());
            lines.add("Sprite Id: " + component.getSpriteId());
            lines.add("Name (on menu): " + component.getName());
            lines.add("Actions: ");
            lines.addAll(component.getActions().stream().map(s -> "    " + s).collect(Collectors.toList()));
            ItemDefinition item = component.getContainedItem();
            if (item != null) {
                lines.add("");
                lines.add("---- Contained Item ----");
                lines.add("Name: " + item.getName());
                lines.add("Members Only: " + item.isMembersOnly());
                lines.add("Stacks: " + item.stacks());
                lines.add("Unnoted Id: " + item.getUnnotedId());
                lines.add("Noted Id: " + item.getNotedId());
                lines.add("Shop Value: " + item.getShopValue());
                lines.add("Equipment Slot: " + item.getEquipmentSlot());
                lines.add("Ground Actions: ");
                lines.addAll(item.getGroundActions().stream().map(s -> "    " + s).collect(Collectors.toList()));
                lines.add("Inventory Actions: ");
                lines.addAll(item.getInventoryActions().stream().map(s -> "    " + s).collect(Collectors.toList()));
                lines.add("Attributes: ");
                lines.addAll(item.getAttributes().stream().map(a -> "    " + getAttributeString(a)).collect(Collectors.toList()));
            }
            NpcDefinition npc = component.getProjectedNpc();
            if (npc != null) {
                if (npc.getLocalState() != null)
                    npc = npc.getLocalState();
                lines.add("");
                lines.add("---- Projected Npc ----");
                lines.add("Name: " + npc.getName());
                lines.add("Level: " + npc.getLevel());
                lines.add("Appearance: " + npc.getAppearance());
                lines.add("Overhead Icons: ");
                lines.addAll(npc.getOverheadIcons().stream().map(s -> "    " + s.getId()).collect(Collectors.toList()));
                lines.add("Actions: ");
                lines.addAll(npc.getActions().stream().map(s -> "    " + s).collect(Collectors.toList()));
                lines.add("Color Substitutions (rgb): ");
                lines.addAll(npc.getColorSubstitutions().entrySet().stream().map(e -> getColorString(e.getKey()) + " -> " + getColorString(e.getValue())).collect(Collectors.toList()));
                lines.add("Attributes: ");
                lines.addAll(npc.getAttributes().stream().map(a -> "    " + getAttributeString(a)).collect(Collectors.toList()));
            }

            Player player = component.getProjectedPlayer();
            if (player != null) {
                lines.add("");
                lines.add("---- Projected Player ----");
                lines.add("Name: " + player.getName());
                lines.add("Model: " + player.getVisibility() + "% Visible (Hashcode: " + player.getModel().hashCode() + ")");
                Coordinate pos = player.getPosition();
                lines.add("Position: (" + pos.getX() + ", " + pos.getY() + ", " + pos.getPlane() + ")");
                lines.add("Orientation (as angle): " + player.getOrientationAsAngle());
                lines.add("Combat Level: " + player.getCombatLevel());
                lines.add("Animation Id: " + player.getAnimationId());
                lines.add("Stance Id: " + player.getStanceId());
                lines.add("Spot Animation Ids: " + player.getSpotAnimationIds());
                lines.add("Moving: " + player.isMoving());
                CombatGauge hp = player.getHealthGauge();
                lines.add("Health %: " + (hp == null? "n/a (gauge: null)" : hp.getPercent()));
                CombatGauge adren = player.getAdrenalineGauge();
                lines.add("Adrenaline %: " + (adren == null? "n/a (gauge: null)" : adren.getPercent()));
                lines.add("Dialogue: " + player.getDialogue());
                lines.add("Overhead Icons: ");
                lines.addAll(player.getOverheadIcons().stream().map(s -> "    " + s.getId()).collect(Collectors.toList()));
                lines.add("Npc Id: " + player.getNpcTransformationId());
                lines.add("Target: " + player.getTarget());
                lines.add("Familiar: " + player.getFamiliar());
            }
        }
        StringBuilder result = new StringBuilder();
        if (container != null)
            result.append(container);
        else if (component != null)
            result.append(component);
        lines.forEach(s -> result.append('\n').append(s));
        return result.toString();
    }

    public String getBoundsString(Rectangle bounds) {
        if (bounds == null)
            return "null";
        return "(x=" + (int)bounds.getX() + ", y=" + (int)bounds.getY() + ", width=" + (int)bounds.getWidth() + ", height=" + (int)bounds.getHeight() + ")";
    }

    public String getColorString(Color color) {
        if (color == null)
            return "null";
        return "(r=" + color.getRed() + ", g=" + color.getGreen() + ", b=" + color.getBlue() + ")";
    }

    public String getAttributeString(Attribute a) {
        if (a == null)
            return "null";
        return "[id=" + a.getId() + ", value=" + a.getAsInteger() + "]";
    }

    public List<InterfaceComponent> getClicked(final List<InterfaceComponent> list, OpenInterfaceExplorer bot, Point clickPoint) {
        try {
            if (component != null && bot.getPlatform().invokeAndWait(() -> component.contains(clickPoint))) {
                list.add(component);
                Collection<TreeItem<String>> children = new ArrayList<>(getChildren());
                children.forEach(i -> ((InterfaceTreeItem)i).getClicked(list, bot, clickPoint));
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void renderInterface(Graphics2D g) {
        if (component != null) {
            component.render(g);
        }
    }
}
