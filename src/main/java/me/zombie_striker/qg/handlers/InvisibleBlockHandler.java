package me.zombie_striker.qg.handlers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.api.QualityArmory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class InvisibleBlockHandler implements Listener {

    private final Map<UUID, Location> fakeBlock = new HashMap<>();

    private final Map<UUID, Set<Location>> layingFakeBlocks = new HashMap<>();


    private final Map<UUID, Double> moveSpeed = new ConcurrentHashMap<>();

    private final ConcurrentMap<UUID, RayTraceResult> rayTraceResults = new ConcurrentHashMap<>();

    private final Set<UUID> layingPlayers = new HashSet<>();

    private final Cache<Player, Boolean> sneakCache = CacheBuilder.newBuilder().expireAfterWrite(500, TimeUnit.MICROSECONDS).build();

    public void init(QAMain main) {

        Bukkit.getPluginManager().registerEvents(this, main);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (QAMain.SWAP_TO_LMB_SHOOT) {
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    /*
                    Set<Location> fakeBlocks;
                    if (layingPlayers.contains(player.getUniqueId())) {
                        fakeBlocks = layingFakeBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

                        Location loc = player.getLocation();
                        fakeBlocks.clear();
                        loc.add(1,1,1);
                        for (int x = 0; x < 2;x++) {
                            for (int y = 0; y < 2;y++) {
                                Location blockLoc = loc.getBlock().getLocation();
                                fakeBlocks.add(blockLoc);
                            }
                        }

                    } else {

                    }


                     */


                    // Gun Block

                    int fakeBlockDistance = 2;

                    if (moveSpeed.containsKey(player.getUniqueId())) {
                        double speed = moveSpeed.get(player.getUniqueId());
                        if (speed > 1.26) {
                            fakeBlockDistance = 4;
                        } else if (speed > 1.07) {
                            fakeBlockDistance = 3;
                        }
                    }

                    Block targetblock = player.getTargetBlock(null,fakeBlockDistance);

                    if (!(QualityArmory.isGun(player.getInventory().getItemInMainHand()) || QualityArmory.isIronSights(player.getInventory().getItemInMainHand()))) {

                        if (fakeBlock.containsKey(player.getUniqueId())) {
                            Location prev = fakeBlock.remove(player.getUniqueId());
                            player.sendBlockChange(prev, prev.getBlock().getBlockData());
                        }
                    }else {
                        if (targetblock.getType() != Material.AIR) {
                            Location prev = fakeBlock.get(player.getUniqueId());
                            player.sendBlockChange(prev, prev.getBlock().getBlockData());
                            return;
                        }
                        if (fakeBlock.containsKey(player.getUniqueId())) {
                            Location prev = fakeBlock.get(player.getUniqueId());
                            if (targetblock.getLocation() != prev) {
                                player.sendBlockChange(prev, prev.getBlock().getBlockData());
                            } else {
                                return;
                            }
                        }
                        fakeBlock.put(player.getUniqueId(), targetblock.getLocation());
                        player.sendBlockChange(targetblock.getLocation(), Material.BARRIER.createBlockData());
                    }
                }
            }
        }.runTaskTimerAsynchronously(main,2,1);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        Location to = event.getTo().clone();
        to.setY(0);
        Location loc = event.getPlayer().getLocation();
        loc.setY(0);
        double distance = to.distance(loc.subtract(loc.getDirection()));
        moveSpeed.put(event.getPlayer().getUniqueId(), distance);

    }

    /*
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (event.getPlayer().isSneaking()) {
            return;
        }
        if (sneakCache.getIfPresent(event.getPlayer()) != null) {
            layingPlayers.add(event.getPlayer().getUniqueId());
        } else {
            sneakCache.put(event.getPlayer(), true);
        }
    }

     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        moveSpeed.remove(event.getPlayer().getUniqueId());
        fakeBlock.remove(event.getPlayer().getUniqueId());
        rayTraceResults.remove(event.getPlayer().getUniqueId());
    }
}
