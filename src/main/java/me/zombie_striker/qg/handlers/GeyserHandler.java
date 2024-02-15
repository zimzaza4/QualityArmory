package me.zombie_striker.qg.handlers;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import me.zimzaza4.geyserutils.common.animation.Animation;
import me.zimzaza4.geyserutils.spigot.api.PlayerUtils;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.api.QualityArmory;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GeyserHandler implements Listener {
    public static boolean isFloodgatePlayer(Player player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }


    public static void shakeCamera(Player player, float recoil, float v, int i) {
        PlayerUtils.shakeCamera(player, recoil, v, i);
    }

    public static void playAnimation(Player player, Entity e, String animation, float blendOutTime) {
        PlayerUtils.playEntityAnimation(player, Animation.builder().animation(animation).blendOutTime(blendOutTime).build(), e);
    }

    public static void playAnimation(Player player, Entity e, Animation animation, float blendOutTime) {
        PlayerUtils.playEntityAnimation(player, animation, e);
    }

    public static void initGunAnimation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isFloodgatePlayer(p)) {
                        for (Player other : Bukkit.getOnlinePlayers()) {
                            if (p == other) {
                                continue;
                            }
                            boolean isIronSight = QualityArmory.isIronSights(other.getItemInHand());
                            ItemStack gunItem = isIronSight ? other.getInventory().getItemInOffHand() : other.getInventory().getItemInMainHand();

                            Gun gun = QualityArmory.getGun(gunItem);
                            if (gun == null) {
                                return;
                            }
                            CompletableFuture.runAsync(() -> playAnimation(p, other, Animation.builder().animation("animation.player.crossbow_hold").nextState("animation.player.crossbow_hold").build(), 100));
                        }
                    }
                }
            }
        }.runTaskTimer(QAMain.getInstance(), 40, 40);
    }

    @EventHandler
    public void onToggleItem(PlayerItemHeldEvent event) {

        ItemStack oldItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
        ItemStack newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());

        boolean toGun;
        // Gun gun = QualityArmory.getGun(oldItem);
        Gun gun = QualityArmory.getGun(newItem);
        if (gun != null) {
            toGun = true;
        } else {
            toGun = false;
        }


        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isFloodgatePlayer(p)) {

                        if (p == event.getPlayer()) {
                            continue;
                        }
                        if (toGun) {
                            playAnimation(p, event.getPlayer(), Animation.builder().animation("animation.player.crossbow_hold").nextState("animation.player.crossbow_hold").build(), 100);
                        } else {
                            playAnimation(p, event.getPlayer(), "animation.player.holding", 1);
                        }

                    }
                }
            }
        }.runTaskLaterAsynchronously(QAMain.getInstance(), 7);
    }
}
