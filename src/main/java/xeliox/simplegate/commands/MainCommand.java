package xeliox.simplegate.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import xeliox.simplegate.SimpleGate;
import xeliox.simplegate.config.Messages;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final SimpleGate plugin;

    public MainCommand(SimpleGate plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Messages.USAGE.getMessage());
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload":
                if (!sender.hasPermission("simplegate.admin")) {
                    sender.sendMessage(Messages.PREFIX.getMessage() + Messages.NO_PERMISSION.getMessage());
                    return true;
                }
                try {
                    plugin.getConfigManager().reloadConfig();
                    sender.sendMessage(Messages.PREFIX.getMessage() + Messages.CONFIG_RELOAD.getMessage());
                } catch (IOException e) {
                    sender.sendMessage(Messages.PREFIX.getMessage() + Messages.CONFIG_RELOAD_ERROR.getMessage().replace("{0}", e.getMessage()));
                }
                return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
