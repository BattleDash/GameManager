package me.battledash.game.util;

public class EnvUtil {

    public static String getEnvOrDefault(String env, String def) {
        return System.getenv().getOrDefault(env, def);
    }

}
