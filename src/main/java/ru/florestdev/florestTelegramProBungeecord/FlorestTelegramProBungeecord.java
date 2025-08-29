package ru.florestdev.florestTelegramProBungeecord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

public final class FlorestTelegramProBungeecord extends Plugin {

    public final HttpClient httpClient = HttpClient.newHttpClient();

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
            getLogger().info("Successful sending.");
        }
        else {
            getLogger().info("Own bad! We can't send message to Telegram APIs.");
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("FTP for Bungeecord already started.");
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            // Если нет, копируем его из ресурсов JAR
            try (InputStream is = getResourceAsStream("config.yml")) { // config.yml должен быть в корне ресурсов JAR (src/main/resources/config.yml)
                Files.copy(is, configFile.toPath());
                getLogger().info("Default config.yml created.");
            } catch (IOException e) {
                getLogger().severe("Failed to create default config.yml: " + e.getMessage());
            }
        }


        Configuration config = null;
        try {
           config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            // ...
        }

        if (config == null) {
            // ...
        } else {
            getProxy().getPluginManager().registerListener(this, new ChatListener(this, config));
            if (config.getBoolean("enable_players_tracking")) {
                getProxy().getPluginManager().registerListener(this, new PlayerJoinQuitListener(this, config));
            }

            try {
                String token = config.getString("telegram_bot_token");
                String chatID = config.getString("telegram_chat_id");
                String message = config.getString("bungee_on_message");
                SendTelegramFUNCTION(token, chatID, message);
            } catch (IOException | InterruptedException e) {
                // ...
            }
        }

    }

    @Override
    public void onDisable() {
        getLogger().info("FTP plugin was disabled. Bye.");
        Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load("config.yml");
        ProxyServer proxy = ProxyServer.getInstance();
        int online = proxy.getOnlineCount();
        try {
            String token = config.getString("telegram_bot_token");
            String chatID = config.getString("telegram_chat_id");
            String message = config.getString("bungee_off_message").replace("{online}", String.valueOf(online));
            SendTelegramFUNCTION(token, chatID, message);
        } catch (IOException | InterruptedException e) {
            // ...
        }
    }
}
