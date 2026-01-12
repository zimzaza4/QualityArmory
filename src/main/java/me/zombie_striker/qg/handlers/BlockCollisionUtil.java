package me.zombie_striker.qg.handlers;

import me.zombie_striker.qg.QAMain;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;

import java.util.HashMap;

public class BlockCollisionUtil {

	private static final HashMap<Material,Double> customBlockHeights = new HashMap<>();

	static{
		for(Material m : Material.values()){
			if(m.name().endsWith("_WALL"))
				customBlockHeights.put(m,1.5);
			if(m.name().endsWith("_FENCE_GATE")||m.name().endsWith("_FENCE"))
				customBlockHeights.put(m,1.5);
			if(m.name().endsWith("_BED"))
				customBlockHeights.put(m,0.5);
			if(m.name().endsWith("_SLAB")||m.name().endsWith("_FENCE"))
				customBlockHeights.put(m,0.5);
			if(m.name().endsWith("DAYLIGHT_DETECTOR"))
				customBlockHeights.put(m,0.4);
			if(m.name().endsWith("CARPET"))
				customBlockHeights.put(m,0.1);
			if(m.name().endsWith("TRAPDOOR"))
				customBlockHeights.put(m,0.2);
		}
	}

	public static double getHeight(Block b){
		Material type = b.getType();
		if (b.getBlockData() instanceof Slab slab) {
			if (slab.getType() == Slab.Type.BOTTOM)
				return 0.5;
			if (slab.getType() == Slab.Type.TOP || slab.getType() == Slab.Type.DOUBLE)
				return 1;
		}
		if(customBlockHeights.containsKey(type))
			return customBlockHeights.get(type);
		return type.isSolid()?1:0;
	}

	public static boolean isSolidAt(Block b, Location loc){
		if(b.getLocation().getY()+getHeight(b)>loc.getY())
			return true;
		Block temp = b.getRelative(0,-1,0);
		if(temp.getLocation().getY()+getHeight(temp)>loc.getY())
			return true;
		return false;
	}


	public static boolean isSolid(Block b, Location l) {
		if(b.getType().name().equals("SNOW"))
			return false;
		if(b.getType().name().contains("SIGN"))
			return false;
		if (b.getType().name().endsWith("CARPET")) {
			return false;
		}
		if (b.getType() == Material.WATER) {
			if (QAMain.blockbullet_water)
				return true;
		}
		if (b.getType().name().contains("LEAVE")) {
			if (QAMain.blockbullet_leaves)
				return true;
		}
		if (b.getBlockData() instanceof Slab slab) {
			if (!QAMain.blockbullet_halfslabs && ((l.getY() - l.getBlockY() > 0.5 && slab.getType() == Slab.Type.BOTTOM)
					|| (l.getY() - l.getBlockY() <= 0.5 && slab.getType() == Slab.Type.TOP)))
				return false;
			return true;
		}
		if (b.getType().name().contains("BED_") || b.getType().name().contains("_BED")
				|| b.getType().name().contains("DAYLIGHT_DETECTOR")) {
			if (!QAMain.blockbullet_halfslabs && (l.getY() - l.getBlockY() > 0.5))
				return false;
			return true;
		}
		if (b.getType().name().contains("DOOR")) {
			if (QAMain.blockbullet_door)
				return true;
			return false;
		}
		if (b.getType().name().contains("GLASS")) {
			if (QAMain.blockbullet_glass)
				return true;
			return false;
		}

		if (b.getType().name().contains("STAIR")) {
			return true;
		}

		if (b.getType().name().endsWith("FERN")) {
			return false;
		}
		if (b.getType().isOccluding()) {
			return true;
		}
		return false;
	}
}
