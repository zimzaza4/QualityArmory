package me.zombie_striker.qg.handlers;


import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerAbilities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.google.common.cache.*;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.api.QualityArmory;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PacketEventsHandler {

    private static Random random = new Random();
    private static PacketEventsAPI<?> packetEventsAPI = PacketEvents.getAPI();
    public static Cache<Player, Float> pitchCache = CacheBuilder.newBuilder()
            .expireAfterAccess(310, TimeUnit.MILLISECONDS)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();

    public static Map<Player, Float> zoomData = new HashMap<>();

    public static void init() {
        packetEventsAPI.getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketSend(PacketSendEvent event) {
                Player player = event.getPlayer();
                if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                    WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(event);
                    if (packet.getEntityId() == player.getEntityId()) return;
                    if (QAMain.hasGeyser && GeyserHandler.isFloodgatePlayer(player)) {
                       return;
                    }
                    boolean isIronSight = false;

                    @Nullable Entity target = SpigotConversionUtil.getEntityById(player.getWorld(), packet.getEntityId());
                    if (target instanceof LivingEntity e && e.getEquipment() != null) {
                        QualityArmory.isIronSights(e.getEquipment().getItemInMainHand());
                    }
                    ItemStack gunItem = null;
                    for (Equipment equipment : packet.getEquipment()) {
                        ItemStack item = SpigotConversionUtil.toBukkitItemStack(equipment.getItem());
                        Gun gun = QualityArmory.getGun(item);
                        if (gun == null) continue;
                        if (isIronSight) {
                            if (equipment.getSlot() == EquipmentSlot.OFF_HAND) {
                                gunItem = item;
                                break;
                            }
                        } else {
                            if (equipment.getSlot() == EquipmentSlot.MAIN_HAND) {
                                gunItem = item;
                                break;
                            }
                        }
                    }
                    if (gunItem == null) {
                        return;
                    }
                    if (QAMain.hasViaVersion && com.viaversion.viaversion.api.Via.getAPI()
                                .getPlayerProtocolVersion(player.getUniqueId()).getVersion() < ProtocolVersion.v1_13_1.getVersion()) {
                        // HUH
                    } else {
                        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
                        CrossbowMeta meta = (CrossbowMeta) crossbow.getItemMeta();
                        meta.setCustomModelData(gunItem.getItemMeta().getCustomModelData());
                        meta.setChargedProjectiles(Collections.singletonList(new ItemStack(Material.ARROW)));
                        crossbow.setItemMeta(meta);
                        gunItem = crossbow;

                    }
                    boolean set = false;
                    for (Equipment equipment : packet.getEquipment()) {
                        if (isIronSight) {
                           if (equipment.getSlot() == EquipmentSlot.OFF_HAND) {
                               equipment.setItem(com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY);
                           }
                        }
                        if (equipment.getSlot() == EquipmentSlot.MAIN_HAND) {
                            set = true;
                            equipment.setItem(SpigotConversionUtil.fromBukkitItemStack(gunItem));
                        }
                    }
                    if (!set) {
                        packet.getEquipment().add(new Equipment(EquipmentSlot.MAIN_HAND, SpigotConversionUtil.fromBukkitItemStack(gunItem)));
                    }
                }
                /*
                if (event.getPacketType() == PacketType.Play.Server.PLAYER_ABILITIES) {
                    if (zoomData.containsKey(player)) {
                        WrapperPlayServerPlayerAbilities wrapper = new WrapperPlayServerPlayerAbilities(event);
                        wrapper.setFOVModifier(zoomData.get(player));
                        wrapper.write();
                    }
                }

                 */
            }
        }, PacketListenerPriority.HIGH);

    }

    public static void sendYawChange(Player player, double deltaYaw) {
        Location newLoc = player.getLocation();
        newLoc.setPitch((float) (newLoc.getPitch() + deltaYaw));
        int teleportId = random.nextInt() | Integer.MIN_VALUE;

        RelativeFlag flags = RelativeFlag.X.or(RelativeFlag.Y).or(RelativeFlag.Z).or(RelativeFlag.ROTATE_DELTA).or(RelativeFlag.DELTA_X).or(RelativeFlag.DELTA_Y).or(RelativeFlag.DELTA_Z).or(RelativeFlag.PITCH).or(RelativeFlag.YAW);
        WrapperPlayServerPlayerPositionAndLook packet = new WrapperPlayServerPlayerPositionAndLook(0,0,0,0, (float) deltaYaw, flags.getMask(),teleportId, false);
        PacketEvents.getAPI().getPlayerManager().getUser(player).sendPacket(packet);
        /*
        if (packetEventsAPI.getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_3)) {
            player.teleport(newLoc, TeleportFlag.Relative.VELOCITY_ROTATION, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y, TeleportFlag.Relative.VELOCITY_Z);
        } else {
            player.teleport(newLoc, TeleportFlag.Relative.X, TeleportFlag.Relative.Y, TeleportFlag.Relative.Z, TeleportFlag.Relative.YAW, TeleportFlag.Relative.PITCH);
        }

         */
    }

    public static void setZoom(Player player, float zoom) {
        zoomData.put(player, zoom);
        refreshAbilities(player);
    }

    public static void resetZoom(Player player) {
        zoomData.remove(player);
        refreshAbilities(player);
    }

    public static float getScope(double level) {
        level = level / 2;
        if (level < 1.0) {
            level = 1.0;
        }
        if (level > 10.0) {
            level = 10.0;
        }
        return (float) (1.0 / (20.0 / level - 10.0));
    }

    private static void refreshAbilities(Player player) {
        WrapperPlayServerPlayerAbilities abilities = new WrapperPlayServerPlayerAbilities(
                player.isInvulnerable(),
                player.isFlying(),
                player.getAllowFlight(),
                player.getGameMode() == GameMode.CREATIVE,
                player.getFlySpeed(),
                player.getWalkSpeed() / 2 // divide by 2, since spigot multiplies this by 2
        );

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, abilities);
    }

    public static void addRecoilWithPaperTeleport(Player player, Gun g, boolean useHighRecoil) {
        sendYawChange(player, -g.getRecoil() * 0.26);
        /*
        new BukkitRunnable() {
            double deltaY = g.getRecoil();
            @Override
            public void run() {
                double currentDy = 1;
                if (deltaY <= 1) {
                    currentDy = deltaY;
                    deltaY = 0;
                } else {
                    deltaY -= 1;
                }
                sendYawChange(player, -currentDy);
                if (deltaY <= 0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(QAMain.getInstance(), 0, 0);

         */
    }


}
