package xeliox.simplegate.config;

import api.xeliox.colorapi.ColorAPI;
import xeliox.simplegate.config.core.MessagesConfig;

public enum Messages {
    PREFIX("Messages.Prefix", "&8[&dSimpleGate&8] &r"),
    GATE_CREATED("Messages.GateCreated", "&aA door named &c\"{0}\" &aformed in front of you."),
    GATE_DESTROYED("Messages.GateDestroyed", "&cThis door has been destroyed."),
    GATE_LIMIT_REACHED("Messages.GateLimitReached", "&cYou have reached the maximum number of doors you can create with the same name \"{0}\""),
    FRAME_BIG("Messages.FrameInvalidBig", "&cThere is no frame for the door or it is too big."),
    NO_PERMISSION("Messages.NoPermission", "&cYou do not have permission to perform this action."),
    INVALID_LOCATION("Messages.InvalidLocation", "&cThe specified location is invalid."),
    TELEPORT_SUCCESS("Messages.TeleportSuccess", "&aTeleported to location {0}."),
    WORLD_DISABLED("Messages.WorldDisabled", "&cGates are disabled in this world."),
    GATE_DESTINATION_NOT_FOUND("Messages.GateDestinationNotFound", "&cThe destination gate was not found."),
    ITEM_RENAMED_TO_CREATE("Messages.ItemRenamedToCreate", "&aYou must name the {0} before creating a door with it."),
    FRAME_BLOCK_REQUIRED("Messages.PortalBlocksNotRequired", "&cThe frame must contain {0}."),
    CONFIG_RELOAD("Messages.ConfigReloaded", "&aConfiguration reloaded successfully."),
    PLAYER_ONLY("Messages.PlayerOnly", "&cOnly players can perform this command."),
    GATE_OVERLAP("Messages.GateOverlap", "&cThis gate overlaps with another gate called {0}."),
    CONFIG_RELOAD_ERROR("Messages.ConfigReloadError", "&cError reloading configuration: {0}");

    public final String path;
    public final String defaultMessage;
    private String message;

    Messages(String path, String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }

    public void loadMessage(MessagesConfig config) {
        this.message = ColorAPI.translate(config.getString(this.path, this.defaultMessage));
    }

    public String getMessage() {
        return message;
    }
}
