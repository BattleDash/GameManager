package me.battledash.game.game;

import lombok.Data;
import me.battledash.game.packets.PlayerLoginState;

import java.util.UUID;

@Data
public class JoinTicket {

    private final UUID id = UUID.randomUUID();
    private final UUID playerId;
    private final long issuedAt;
    private final long expiresAt;
    private final PlayerLoginState state;

}
