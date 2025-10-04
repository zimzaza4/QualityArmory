package me.zombie_striker.qg.miscitems;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import me.zombie_striker.qg.hooks.protection.ProtectionHandler;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.zombie_striker.qg.QAMain;
import me.zombie_striker.customitemmanager.MaterialStorage;
import me.zombie_striker.qg.guns.utils.WeaponSounds;
import org.bukkit.util.Vector;

public class SmokeGrenades extends Grenade {

	public SmokeGrenades(ItemStack[] ingg, double cost, double damage, double explosionreadius, String name,
			String displayname, List<String> lore, MaterialStorage ms) {
		super(ingg, cost, damage, explosionreadius, name, displayname, lore, ms);
	}

	@Override
	public boolean onPull(Player thrower, ItemStack usedItem) {
		if(!QAMain.autoarm)
		if (throwItems.containsKey(thrower)) {
			thrower.sendMessage(QAMain.prefix + QAMain.S_GRENADE_PALREADYPULLPIN);
			thrower.playSound(thrower.getLocation(), WeaponSounds.RELOAD_BULLET.getSoundName(), 1, 1);
			return true;
		}
		thrower.getWorld().playSound(thrower.getLocation(), WeaponSounds.RELOAD_MAG_IN.getSoundName(), 2, 1);
		final ThrowableHolder h = new ThrowableHolder(thrower.getUniqueId(), thrower, this);
		h.setTimer(new BukkitRunnable() {


			int k = 0;
			@Override
			public void run() {
				k++;
				Bukkit.getScheduler().runTaskAsynchronously(QAMain.getInstance(), () -> {
					Set<Location> locations = selectLocations(h.getHolder().getLocation(), 4);

					for (Location selectLocation : locations) {
						selectLocation = selectLocation.clone();
						if (XParticle.CAMPFIRE_COSY_SMOKE.get() != null) {

							ParticleDisplay.of(XParticle.CAMPFIRE_SIGNAL_SMOKE)
									.offset(0.5, 0.5, 0.5)
									.particleDirection(new Vector(0, 0, 0))
									.withLocation(selectLocation)
									.spawn();
						} else {
							selectLocation.getWorld().spawnParticle(XParticle.SMOKE.get(), selectLocation, 0);
						}
					}
				});
				if (k == 1) {
					if (h.getHolder() instanceof Player) {
						QAMain.DEBUG("Blinded player");
						// ((LivingEntity) h.getHolder())
						//		.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 2));
						removeGrenade(((Player) h.getHolder()));
					}
				} else if (k == 80) {
					if (h.getHolder() instanceof Item) {
						Grenade.getGrenades().remove(h.getHolder());
						h.getHolder().remove();
					}
					throwItems.remove(h.getHolder());
					this.cancel();
				} else {
					/*
					for(Entity e : h.getHolder().getNearbyEntities(radius, radius, radius))
						if(e instanceof LivingEntity) {
							QAMain.DEBUG("Blinding to "+e.getName());
							try {
								if (ProtectionHandler.canPvp(e.getLocation())) {
									//((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 2));
								}
							}catch (Error error){
								//((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 10, 2));
							}
						}

					 */
				}
			}
		}.runTaskTimer(QAMain.getInstance(), 5 * 20, 5));
		throwItems.put(thrower, h);
		return true;

	}

	public static Set<Location> selectLocations(Location location, int radius) {
		Set<Location> locations = new HashSet<>();
		int bx = location.getBlockX();
        int by = location.getBlockY();
        int bz = location.getBlockZ();

        for (int x = bx - radius; x <= bx + radius; x++) {
            for (int y = by - radius; y <= by + radius; y++) {
                for (int z = bz - radius; z <= bz + radius; z++) {
                    double distance = ((bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y));
                    if (distance < radius * radius && (distance < (radius - 1) * (radius - 1))) {
                        locations.add(new Location(location.getWorld(), x, y, z));
                    }
                }
            }
        }
		return locations;
	}
}
