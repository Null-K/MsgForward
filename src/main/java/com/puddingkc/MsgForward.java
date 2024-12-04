package com.puddingkc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.puddingkc.commands.MainCommands;
import com.puddingkc.utils.ChatPacketListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MsgForward extends JavaPlugin {

    private boolean debug;

    private final List<Pattern> regexPatterns = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private List<String> commands = new ArrayList<>();

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        Objects.requireNonNull(getCommand("msgforward")).setExecutor(new MainCommands(this));
        Objects.requireNonNull(getCommand("msgforward")).setTabCompleter(new MainCommands(this));

        PacketEvents.getAPI().getEventManager().registerListener(
                new ChatPacketListener(this), PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public void loadConfig() {
        reloadConfig();
        debug = getConfig().getBoolean("debug", false);

        messages.clear();
        regexPatterns.clear();

        List<String> rawMessages = getConfig().getStringList("messages");
        for (String message : rawMessages) {
            message = message.trim();
            if (message.startsWith("#")) {
                try {
                    Pattern pattern = Pattern.compile(message.substring(1).trim());
                    regexPatterns.add(pattern);
                } catch (Exception e) {
                    getLogger().warning("无效的正则表达式: " + message.substring(1).trim());
                }
            } else {
                messages.add(message.replace("&", "§"));
            }
        }

        commands = getConfig().getStringList("commands");
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getCommands() {
        return commands;
    }

    public boolean isDebug() {
        return debug;
    }

    public List<Pattern> getRegexPatterns() {
        return regexPatterns;
    }
}