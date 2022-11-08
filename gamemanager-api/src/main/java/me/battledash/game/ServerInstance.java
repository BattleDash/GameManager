package me.battledash.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServerInstance {

    private final String host;
    private final int port;
    private final String siderId;

}
