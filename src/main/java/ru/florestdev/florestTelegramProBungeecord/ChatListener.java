package ru.florestdev.florestTelegramProBungeecord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChatListener implements  Listener {
    public final Plugin plugin;
    public final HttpClient httpClient = HttpClient.newHttpClient();
    private final Configuration config;

    public ChatListener(Plugin plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void SendTelegramFUNCTION(String botToken, String chatId, String message) throws IOException, InterruptedException {
        // Функция для отправки сообщения в тг
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
        String requestBody = String.format("chat_id=%s&text=%s", chatId, message);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "FlorestPlugin")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response;
        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            plugin.getLogger().info("Successful sending.");
        }
        else {
            plugin.getLogger().info("Own bad! We can't send message to Telegram APIs.");
        }
    }

    @EventHandler
    public void onChat(ChatEvent event) throws IOException, InterruptedException {
        if (event.isCancelled()) {
            return;
        }

        // Получаем ProxiedPlayer, если это игрок.
        if (event.getSender() instanceof net.md_5.bungee.api.connection.ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            if (!event.getMessage().startsWith("/")) {
                String token = config.getString("telegram_bot_token");
                String chatID = config.getString("telegram_chat_id");
                String message = config.getString("message_format").replace("{user}", player.getName()).replace("{server_name}", player.getServer().getInfo().getName()).replace("{message}", event.getMessage());
                SendTelegramFUNCTION(token, chatID, message);
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


}
