package me.battledash.game.game;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.battledash.game.GameInstance;
import me.battledash.game.GameManager;
import me.battledash.game.packets.PlayerLoginState;
import me.battledash.game.packets.GameTicketCode;
import me.battledash.game.pool.GamePool;
import me.battledash.game.state.BasePhase;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public abstract class Game {

    private static final String DISCONNECT_REASON_GAME_KILLED = "The game you were connected to was killed (%s)";
    private static final int JOIN_TICKET_TIMEOUT = 10_000;

    private final Plugin plugin;
    private final GamePool pool;
    private final UUID id = UUID.randomUUID();

    private final Set<GMPlayer> players = new HashSet<>();
    private final Cache<UUID, JoinTicket> joinTickets = CacheBuilder
            .newBuilder()
            .expireAfterWrite(JOIN_TICKET_TIMEOUT, TimeUnit.MILLISECONDS)
            .build();

    @Setter
    private int maxPlayers = 8;

    @Setter
    private int teamSize = 2;

    {
        GameManager instance = GameManager.getInstance();
        instance.getGamesDepot().put(id.toString(), new GameInstance(id,
                instance.getServerInstance(), Map.of("type", "SKYBLOCK")));
    }

    public abstract BasePhase createMainPhase();

    public GameTicketCode requestJoinStatus(UUID playerId) {
        if (!isAcceptingPlayers()) {
            return GameTicketCode.GAME_FULL;
        }

        for (Player bukkitPlayer : getBukkitPlayers()) {
            if (bukkitPlayer.getUniqueId().equals(playerId)) {
                return GameTicketCode.ALREADY_CONNECTED;
            }
        }

        return GameTicketCode.SUCCESS;
    }

    public JoinTicket issueJoinTicket(UUID playerId, PlayerLoginState state) {
        for (UUID joinTicket : joinTickets.asMap().keySet()) {
            if (joinTicket.equals(playerId)) {
                throw new IllegalArgumentException("This player has already been issued a ticket!");
            }
        }

        long issuedAt = Instant.now().toEpochMilli();
        JoinTicket ticket = new JoinTicket(playerId, issuedAt, issuedAt + JOIN_TICKET_TIMEOUT, state);
        joinTickets.put(playerId, ticket);
        return ticket;
    }

    public JoinTicket useJoinTicket(UUID playerId) {
        JoinTicket ticket = joinTickets.getIfPresent(playerId);
        if (ticket == null) {
            return null;
        }

        joinTickets.invalidate(playerId);
        return ticket;
    }

    protected boolean isTeamSuitable(GMPlayer player, PlayerLoginState state, int teamId) {
        long playersOnTeam = players.stream().filter(p -> p.getTeamId() == teamId).count();
        return playersOnTeam < teamSize;
    }

    protected int assignPlayerTeam(GMPlayer player, PlayerLoginState state) {
        for (int team = 0, maxTeams = maxPlayers / teamSize; team < maxTeams; team++) {
            if (isTeamSuitable(player, state, team)) {
                return team;
            }
        }
        return 0;
    }

    public void applyPlayerState(GMPlayer player, PlayerLoginState state) {
        player.setTeamId(assignPlayerTeam(player, state));
    }

    public void addPlayer(Player player, PlayerLoginState state) {
        GMPlayer gmPlayer = new GMPlayer(player);

        applyPlayerState(gmPlayer, state);

        players.add(gmPlayer);
    }

    public void removePlayer(Player player) {
        players.removeIf(p -> p.getPlayer() == player);
    }

    public Collection<GMPlayer> getPlayers() {
        return players;
    }

    public Collection<GMPlayer> getNeutralPlayers() {
        return players.stream().filter(GMPlayer::isNeutral).collect(Collectors.toSet());
    }

    public Collection<? extends Player> getBukkitPlayers() {
        return players.stream().map(GMPlayer::getPlayer).collect(Collectors.toSet());
    }

    public void kill(String reason) {
        for (GMPlayer player : players) {
            player.getPlayer().kickPlayer(String.format(DISCONNECT_REASON_GAME_KILLED, reason));
        }

        cleanup();
    }

    public void cleanup() {
        GameManager.getInstance().getGamesDepot().remove(id.toString());

        players.clear();
    }

    public boolean isAcceptingPlayers() {
        return getPlayers().size() < maxPlayers;
    }

}
