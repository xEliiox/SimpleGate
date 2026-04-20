package xeliox.simplegate.config;

public enum Permissions {

    ADMIN("simplegate.admin"),
    WORLD_BYPASS("simplegate.worldbypass"),
    FRAME_BYPASS("simplegate.framebypass");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String get() {
        return permission;
    }

    public boolean has(org.bukkit.command.CommandSender sender) {
        return sender.hasPermission(permission);
    }
}