package me.battledash.game.packets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameTicketCode {
    SUCCESS(false, true),
    SUCCESS_NO_CONNECT(false, false),
    GAME_FULL(true, false),
    ALREADY_CONNECTED(true, false),
    ALREADY_ISSUED(true, false),
    INVALID_GAME(true, false),
    INVALID_POOL(true, false),
    INVALID_TOKEN(true, false);

    private final boolean error;
    private final boolean shouldSendToServer;
}