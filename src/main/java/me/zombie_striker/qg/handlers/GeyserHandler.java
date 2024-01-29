package me.zombie_striker.qg.handlers;

import me.zimzaza4.geyserutils.spigot.api.PlayerUtils;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public class GeyserHandler {
    public static boolean isFloodgatePlayer(Player player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }


    public static void shakeCamera(Player player, float recoil, float v, int i) {
        PlayerUtils.shakeCamera(player, recoil, v, i);
    }
}
