package com.puddingkc.utils;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.puddingkc.MsgForward;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatPacketListener implements PacketListener {

    private final MsgForward plugin;
    public ChatPacketListener(MsgForward plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
            WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(event);

            Component component = packet.getMessage();
            String message = LegacyComponentSerializer.legacySection().serialize(component);

            if (plugin.getMessages().stream().anyMatch(message::contains)) {
                handleMessage(event, message);
                return;
            }

            if (plugin.getRegexPatterns().stream().anyMatch(pattern -> pattern.matcher(message).matches())) {
                handleMessage(event, message);
                return;
            }

            sendDebugMessage("Triggered message event: " + message);
        }
    }

    private void sendDebugMessage(String message) {
        if (plugin.isDebug()) {
            plugin.getLogger().info(message);
        }
    }

    private void handleMessage(PacketSendEvent event, String message) {
        Player player = event.getPlayer();
        if (player != null) {
            runCommand(message, player);
        }
        sendDebugMessage("Message intercepted: " + message);
        event.setCancelled(true);
    }

    private void runCommand(String message, Player player) {
        List<String> commands = plugin.getCommands();
        if (commands != null && !commands.isEmpty()) {
            for (String command : commands) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), replacePlaceholder(command, message, player));
                    sendDebugMessage("Executing command: " + replacePlaceholder(command, message, player));
                });
            }
        }
    }

    private String replacePlaceholder(String command, String message, Player player) {
        return command
                .replace("{message}", message)
                .replace("{player}", player.getName())
                .replace("&", "§");
    }
}
