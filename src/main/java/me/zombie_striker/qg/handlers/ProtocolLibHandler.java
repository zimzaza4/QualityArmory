package me.zombie_striker.qg.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.cryptomorin.xseries.ReflectionUtils;
import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.zombie_striker.qg.guns.Gun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;

import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.api.QualityArmory;

public class ProtocolLibHandler {

	private static ProtocolManager protocolManager;

	private static Object enumArgumentAnchor_EYES = null;
	private static Class<?> class_ArgumentAnchor = null;
	// org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
	private static Class nbtFactClass = null;
	private static Method nbtFactmethod = null;

	public static void initRemoveArmswing() {
		if (protocolManager == null)
			protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(
				new PacketAdapter(QAMain.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION) {

					@SuppressWarnings("deprecation")
					public void onPacketReceiving(PacketEvent event) {
						final Player player = event.getPlayer();

						boolean tempplayer =  false;
						try{
							player.getVehicle();
						}catch (UnsupportedOperationException e){
							tempplayer=true;
						}
						if(tempplayer)
							return;

						if (event.getPacketType() == PacketType.Play.Client.ARM_ANIMATION
								&& player.getVehicle() != null) {
							try {

								byte state = event.getPacket().getBytes().readSafely(0);
								int entityID = event.getPacket().getIntegers().readSafely(0);
								Player targ = null;
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (p.getEntityId() == entityID) {
										targ = p;
										break;
									}
								}
								if (targ == null) {
									Bukkit.broadcastMessage("The ID for the entity is incorrect");
									return;
								}
								if (state == 0) {
									if (QualityArmory.isGun(targ.getItemInHand())
											|| QualityArmory.isIronSights(targ.getItemInHand())) {
										event.setCancelled(true);
									}
								}
							} catch (Error | Exception e) {
							}
						}
					}
				});

	}

	public static void initAimBow() {
		if (protocolManager == null)
			protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(
				new PacketAdapter(QAMain.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT) {
					@Override
					public void onPacketSending(PacketEvent event) {
						final Player sender = event.getPlayer();
						int id = (int) event.getPacket().getModifier().read(0);
						List<Pair<EnumWrappers.ItemSlot, ItemStack>> items = event.getPacket().getSlotStackPairLists().readSafely(0);
						if ((id) == sender.getEntityId()) {
							return;
						}
						if (items == null) {
							return;
						}
						Player who = null;
						for (Player player : sender.getWorld().getPlayers()) {
							if (player.getEntityId() == id) {
								who = player;
								break;
							}
						}
						if (who == null)
							return;
						boolean isIronSight = QualityArmory.isIronSights(who.getItemInHand());
						System.out.println("|||111|||" +isIronSight);
						ItemStack gunItem = null;
						for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : items) {
							if (isIronSight) {
								if (pair.getFirst() == EnumWrappers.ItemSlot.OFFHAND) {
									gunItem = pair.getSecond().clone();
								}
							} else {
								if (pair.getFirst() == EnumWrappers.ItemSlot.MAINHAND) {
									gunItem = pair.getSecond().clone();
									break;
								}
							}
						}
                        Gun gun = QualityArmory.getGun(gunItem);

						if (gunItem == null) {
							if (isIronSight) event.setCancelled(true);
							return;
						}
						if (gun == null || !gun.hasBetterAimingAnimations()) {
							return;
						}
						gunItem.setType(Material.CROSSBOW);
						NBTItem nbtItem = new NBTItem(gunItem);
						nbtItem.setBoolean("Charged", true);
						nbtItem.applyNBT(gunItem);

						Pair<EnumWrappers.ItemSlot, ItemStack> needDelete = null;

						for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : items) {
							if (isIronSight) {
								if (pair.getFirst() == EnumWrappers.ItemSlot.OFFHAND) {
									needDelete = pair;
									break;
								}
							}
							if (pair.getFirst() == EnumWrappers.ItemSlot.MAINHAND) {
								pair.setSecond(gunItem);
							}
						}

						items.remove(needDelete);
						event.getPacket().getSlotStackPairLists().writeSafely(0, items);
					}
				});

	}

	private static Object getCraftItemStack(ItemStack is) throws NoSuchMethodException {
		if (nbtFactClass == null) {
			nbtFactClass = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
			Class[] c = new Class[1];
			c[0] = ItemStack.class;
			nbtFactmethod = nbtFactClass.getMethod("asNMSCopy", c);
		}
		try {
			return nbtFactmethod.invoke(nbtFactClass, is);
		} catch (InvocationTargetException | IllegalAccessException e) {
			return null;
		}
	}


	public static void sendYawChange(Player player, Vector newDirection) {
		if (protocolManager == null)
			protocolManager = ProtocolLibrary.getProtocolManager();
		final PacketContainer yawpack = protocolManager.createPacket(PacketType.Play.Server.LOOK_AT, false);
		if (enumArgumentAnchor_EYES == null) {
			class_ArgumentAnchor = ReflectionUtils.getNMSClass("commands.arguments", "ArgumentAnchor$Anchor");
			enumArgumentAnchor_EYES = ReflectionsUtil.getEnumConstant(class_ArgumentAnchor, "EYES");
		}
		yawpack.getModifier().write(4, enumArgumentAnchor_EYES);
		yawpack.getDoubles().write(0, player.getEyeLocation().getX() + newDirection.getX());
		yawpack.getDoubles().write(1, player.getEyeLocation().getY() + newDirection.getY());
		yawpack.getDoubles().write(2, player.getEyeLocation().getZ() + newDirection.getZ());
		yawpack.getBooleans().write(0, false);
		protocolManager.sendServerPacket(player, yawpack);
	}
}
