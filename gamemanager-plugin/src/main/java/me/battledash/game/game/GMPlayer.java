package me.battledash.game.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public class GMPlayer {

    private final Player player;

    @Setter
    private int teamId = 0;

    @Setter
    private boolean neutral = true;

}
