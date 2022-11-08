package me.battledash.game.velocity;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;
import me.battledash.game.GameInstance;
import me.battledash.game.ServerInstance;
import me.battledash.game.packets.GameTicketCode;
import me.battledash.game.velocity.redis.ServerCommunicator;
import net.battledash.sider.Sider;
import net.battledash.sider.depot.SiderDepot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Logger;

import static me.battledash.game.util.EnvUtil.getEnvOrDefault;

@Getter
@Plugin(id = "game_manager", name = "GameManager", version = "1.0-SNAPSHOT",
        description = "A MiniGame Manager.", authors = {"BattleDash"})
public class GameManager {

    @Getter
    private static GameManager instance;

    private final ProxyServer server;
    private final Logger logger;

    private Sider sider;
    private ServerCommunicator serverCommunicator;

    @Inject
    GameManager(ProxyServer server, CommandManager commandManager, Logger logger) {
        instance = this;

        this.server = server;
        this.logger = logger;

        logger.info("GameManager initialized");

        // The following code for these commands is a disgusting and temporary mess
        // Please forgive me

        LiteralArgumentBuilder<CommandSource> builder1 = LiteralArgumentBuilder.<CommandSource>literal("games")
                .executes(c -> {
                    c.getSource().sendMessage(Component.text("Registered games:"));
                    SiderDepot<GameInstance> games = sider.createDepot(GameInstance.class, "game_manager_games");
                    for (GameInstance value : games.values()) {
                        c.getSource().sendMessage(
                                Component.text(value.getId().toString() + " : " + new Gson().toJson(value.getMetadata()))
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to join")))
                                        .clickEvent(ClickEvent.runCommand("/join " + value.getId().toString()))
                        );
                    }
                    return 1;
                });

        commandManager.register("games", new BrigadierCommand(builder1));

        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.<CommandSource>literal("join")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("gameId", StringArgumentType.string()).executes(c -> {
                    CommandSource source = c.getSource();
                    if (!(source instanceof Player player)) {
                        source.sendMessage(Component.text("Only players can join games!"));
                        return 1;
                    }

                    SiderDepot<GameInstance> games = sider.createDepot(GameInstance.class, "game_manager_games");
                    GameInstance gameInstance = games.get(StringArgumentType.getString(c, "gameId"));
                    if (gameInstance == null) {
                        player.sendMessage(Component.text("Game does not exist"));
                        return 1;
                    }

                    player.sendMessage(Component.text("Request join for " + gameInstance.getId().toString()));
                    serverCommunicator.sendGameJoinRequest(player, gameInstance, (s, m) -> {
                        player.sendMessage(Component.text("GameTicketCode: " + m.getCode().toString()));
                        if (m.getCode() == GameTicketCode.SUCCESS) {
                            ServerInstance serverInstance = gameInstance.getServerInstance();
                            String host = serverInstance.getHost();
                            int port = serverInstance.getPort();
                            InetSocketAddress address = InetSocketAddress.createUnresolved(host, port);
                            RegisteredServer registeredServer = server.createRawRegisteredServer(new ServerInfo("server", address));
                            player.createConnectionRequest(registeredServer).connect().thenAccept(r -> {
                                if (!r.isSuccessful()) {
                                    player.sendMessage(Component.text("Failed to connect: " +
                                            r.getReasonComponent().orElse(Component.text("Unknown"))));
                                }
                            });
                        }
                    });
                    return 1;
                }));

        commandManager.register("join", new BrigadierCommand(builder));
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        sider = new Sider(
                getEnvOrDefault("REDIS_HOST", "localhost"),
                Integer.parseInt(getEnvOrDefault("REDIS_PORT", "6379"))
        );

        serverCommunicator = new ServerCommunicator(this);
    }

}
