package me.battledash.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class GameInstance {

    private final UUID id;
    private final ServerInstance serverInstance;
    private final Map<String, String> metadata;

}
