package com.puddingkc.commands;

import com.puddingkc.MsgForward;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommands implements CommandExecutor, TabCompleter {

    private final MsgForward plugin;
    public MainCommands(MsgForward plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender.hasPermission("msgforward.admin")) {
            if (args.length >= 1) {
                switch (args[0]) {
                    case "reload":
                        reloadMainConfig(sender);
                        break;
                    case "test":
                        if (args.length >= 2) {
                            String testText = args[1].replaceAll("&", "§");
                            if (testText(testText)) {
                                sendMessage(sender,"文本 " + testText + "§f 符合规则");
                            } else {
                                sendMessage(sender,"文本 " + testText + "§f 不符合规则");
                            }
                        } else {
                            sendHelp(sender);
                        }
                        break;
                    default:
                        sendHelp(sender);
                        break;
                }
                return true;
            }
            sendHelp(sender);
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!sender.hasPermission("msgforward.admin")) {
            return suggestions;
        }

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("reload", "test"));
        }

        return suggestions;
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage("§7[MsgForward] §f" + message);
    }

    private boolean testText(String text) {
        if (plugin.getMessages().stream().anyMatch(text::contains)) {
            return true;
        }
        return plugin.getRegexPatterns().stream().anyMatch(pattern -> pattern.matcher(text).matches());
    }

    private void sendHelp(CommandSender sender) {
        sendMessage(sender, "指令帮助");
        sender.sendMessage("§f/mf reload §8- §7重新加载配置文件");
        sender.sendMessage("§f/mf test <文本> §8- §7测试指定文本是否符合规则");
    }

    private void reloadMainConfig(CommandSender sender) {
        plugin.loadConfig();
        sendMessage(sender,"配置文件重载完成");
    }

}
