package xeliox.simplegate.config;

import api.xeliox.colorapi.ColorAPI;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Arrays;
import java.util.List;


public enum Messages {
    PREFIX("Messages.prefix", "&8[&dSimpleGate&8] &r"),
    GATE_CREATED("Messages.gate-created", "&aA door named &c\"{0}\" &aformed in front of you."),
    GATE_DESTROYED("Messages.gate-destroyed", "&cThis door has been destroyed."),
    GATE_LIMIT_REACHED("Messages.gate-limit-reached", "&cYou have reached the maximum number of doors you can create with the same name \"{0}\""),
    FRAME_BIG("Messages.frame-invalid-big", "&cThere is no frame for the door or it is too big."),
    NO_PERMISSION("Messages.no-permission", "&cYou do not have permission to perform this action."),
    INVALID_LOCATION("Messages.invalid-location", "&cThe specified location is invalid."),
    TELEPORT_SUCCESS("Messages.teleport-success", "&aTeleported to location {0}."),
    WORLD_DISABLED("Messages.world-disabled", "&cGates are disabled in this world."),
    GATE_DESTINATION_NOT_FOUND("Messages.gate-destination-not-found", "&cThe destination gate was not found."),
    ITEM_RENAMED_TO_CREATE("Messages.item-renamed-to-create", "&aYou must name the {0} before creating a door with it."),
    FRAME_BLOCK_REQUIRED("Messages.portal-blocks-not-required", "&cThe frame must contain {0}."),
    CONFIG_RELOAD("Messages.config-reload", "&aConfiguration reloaded successfully."),
    PLAYER_ONLY("Messages.player-only", "&cOnly players can perform this command."),
    GATE_OVERLAP("Messages.gate-overlap", "&cThis gate overlaps with another gate called {0}."),
    CONFIG_RELOAD_ERROR("Messages.config-reload-error", "&cError reloading configuration: {0}"),
    USAGE("Usage: /simplegate reload", Arrays.asList("&e/simplegate reload &7- Reload the configuration", "&e/simplegate version &7- Show the version"));


    public final String path;
    public final String defaultMessage;
    private String message;

    Messages(String path, String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }

    Messages(String path, List<String> defaultMessage) {
        this.path = path;
        this.defaultMessage = String.join("\n", defaultMessage);
    }
    public void loadMessage(YamlFile config) {
        this.message = ColorAPI.translate(config.getString(this.path, this.defaultMessage));
    }

    public String getMessage() {
        return message;
    }
}