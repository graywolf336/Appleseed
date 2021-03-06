package redsgreens.Appleseed;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * AppleseedTreeData stores the data about a real tree in game
 *
 * @author redsgreens
 */
public class AppleseedTreeData {

	private AppleseedTreeType treeType;
	private AppleseedLocation location;
	private AppleseedItemStack itemStack;
	private String player;
	private AppleseedCountMode countMode;
	private Integer dropCount;
	private Integer intervalCount;
	private Integer fertilizerCount;
	private AppleseedLocation signLocation;

	private static Random rand = new Random();

	public AppleseedTreeData(Appleseed plugin, AppleseedLocation loc, AppleseedItemStack is, String p) {
		location = new AppleseedLocation(loc.getWorldName(), loc.getX(), loc.getY(), loc.getZ());
		itemStack = is;
		player = p;
		signLocation = null;

		treeType = plugin.getAppleseedConfig().TreeTypes.get(is);

		countMode = treeType.getCountMode();

		Integer fertilizer = treeType.getMaxFertilizer();
		if(fertilizer == -1)
			fertilizerCount = -1;
		else {
			Integer fcMin = (int) (fertilizer - (0.3 * fertilizer));
			Integer fcMax = (int) (fertilizer + (0.3 * fertilizer));
			Integer r = fcMax - fcMin + 1;
			if(r < 1) r = 1;
			fertilizerCount = rand.nextInt(r) + fcMin;
		}

		if(countMode == AppleseedCountMode.Drop || countMode == AppleseedCountMode.Interval)
			ResetDropCount();
		else {
			dropCount = -1;
			intervalCount = -1;
		}
	}

	public AppleseedTreeData(Appleseed plugin, AppleseedLocation loc, AppleseedItemStack is, AppleseedCountMode cm, Integer dc, Integer fc, Integer ic, String p) {
		location = loc;
		itemStack = is;
		player = p;
		dropCount = dc;
		intervalCount = ic;
		fertilizerCount = fc;
		signLocation = null;
		treeType = plugin.getAppleseedConfig().TreeTypes.get(is);
		countMode = cm;
	}

	public AppleseedTreeData(Appleseed plugin, AppleseedLocation loc, AppleseedItemStack is, AppleseedCountMode cm, Integer dc, Integer fc, Integer ic, String p, AppleseedLocation signLoc) {
		location = loc;
		itemStack = is;
		player = p;
		dropCount = dc;
		intervalCount = ic;
		fertilizerCount = fc;
		signLocation = signLoc;
		treeType = plugin.getAppleseedConfig().TreeTypes.get(is);
		countMode = cm;
	}

	// take a hashmap and make a tree from it
	public static AppleseedTreeData LoadFromHash(Appleseed plugin, HashMap<String, Object> loadData) {
		if(!loadData.containsKey("world") || !loadData.containsKey("x") || !loadData.containsKey("y") || !loadData.containsKey("z") || !loadData.containsKey("itemid"))
			return null;

		World world = plugin.getServer().getWorld((String)loadData.get("world"));
		if(world == null)
			return null;

		String player;
		if(loadData.containsKey("player"))
			player = (String)loadData.get("player");
		else
			player = "unknown";

		Integer dc;
		if(loadData.containsKey("dropcount"))
			dc = (Integer)loadData.get("dropcount");
		else
			dc = -1;

		Integer fc;
		if(loadData.containsKey("fertilizercount"))
			fc = (Integer)loadData.get("fertilizercount");
		else
			fc = -1;

		Integer ic;
		if(loadData.containsKey("intervalcount"))
			ic = (Integer)loadData.get("intervalcount");
		else
			ic = -1;

		AppleseedCountMode cm = AppleseedCountMode.Drop;
		if(loadData.containsKey("countmode")) {
			String cmStr = (String)loadData.get("countmode");
			if(cmStr.equalsIgnoreCase("drop"))
				cm = AppleseedCountMode.Drop; 
			else if(cmStr.equalsIgnoreCase("interval"))
				cm = AppleseedCountMode.Interval;
			else if(cmStr.equalsIgnoreCase("infinite"))
				cm = AppleseedCountMode.Infinite;
		}

		AppleseedItemStack iStack;
		if(loadData.containsKey("durability"))
			iStack = new AppleseedItemStack(Material.getMaterial((Integer)loadData.get("itemid")), ((Integer)loadData.get("durability")).shortValue()); 
		else
			iStack = new AppleseedItemStack(Material.getMaterial((Integer)loadData.get("itemid")));

		Boolean sign = false;
		Double signx = null;
		Double signy = null;
		Double signz = null;
		if(loadData.containsKey("sign")) {
			sign = (Boolean)loadData.get("sign");
			if(sign == true) {
				if(loadData.containsKey("signx") && loadData.containsKey("signy") && loadData.containsKey("signz")) {
					signx = (Double)loadData.get("signx");
					signy = (Double)loadData.get("signy");
					signz = (Double)loadData.get("signz");
				} else
					sign = false;
			}
		}

		AppleseedLocation loc = new AppleseedLocation(world.getName(), (Double)loadData.get("x"), (Double)loadData.get("y"), (Double)loadData.get("z"));
		if(sign) {
			AppleseedLocation signLoc = new AppleseedLocation(world.getName(), signx, signy, signz);
			return new AppleseedTreeData(plugin, loc, iStack, cm, dc, fc, ic, player, signLoc);
		} else
			return new AppleseedTreeData(plugin, loc, iStack, cm, dc, fc, ic, player);
	}

	// take a tree location and item and return a hash for saving to disk
	public HashMap<String, Object> MakeHashFromTree() {
		HashMap<String, Object> treeHash = new HashMap<String, Object>();

		treeHash.put("world", location.getWorldName());
		treeHash.put("x", location.getX());
		treeHash.put("y", location.getY());
		treeHash.put("z", location.getZ());

		treeHash.put("itemid", itemStack.getMaterial().getId());
		if(itemStack.getDurability() != 0)
			treeHash.put("durability", itemStack.getDurability());

		treeHash.put("player", player);
		treeHash.put("dropcount", dropCount);
		treeHash.put("intervalcount", intervalCount);
		treeHash.put("countmode", countMode.toString());
		treeHash.put("fertilizercount", fertilizerCount);

		if(signLocation != null) {
			treeHash.put("sign", true);
			treeHash.put("signx", signLocation.getX());
			treeHash.put("signy", signLocation.getY());
			treeHash.put("signz", signLocation.getZ());
		}

		return treeHash;
	}

	public void ResetDropCount() {
		if(countMode == AppleseedCountMode.Drop) {
			Integer drops = treeType.getDropsBeforeFertilizer();
			Integer dcMin = (int) (drops - (0.3 * drops));
			Integer dcMax = (int) (drops + (0.3 * drops));
			Integer r = dcMax - dcMin + 1;
			if(r < 1) r = 1;
			dropCount = rand.nextInt(r) + dcMin;

			intervalCount = -1;
		} else {
			Integer intervals = treeType.getIntervalsBeforeFertilizer();
			Integer icMin = (int) (intervals - (0.3 * intervals));
			Integer icMax = (int) (intervals + (0.3 * intervals));
			Integer r = icMax - icMin + 1;
			if(r < 1) r = 1;
			intervalCount = rand.nextInt(r) + icMin;

			dropCount = -1;
		}
	}

	public String getWorld() {
		return location.getWorldName();
	}

	public AppleseedLocation getLocation() {
		return location;
	}

	public Location getBukkitLocation() {
		return location.getLocation();
	}

	public AppleseedItemStack getItemStack() {
		return itemStack;
	}

	public String getPlayer() {
		return player;
	}

	public boolean isInfinite() {
		if(countMode == AppleseedCountMode.Infinite || treeType.getRequireFertilizer() == false)
			return true;
		else 
			return false;
	}

	public void setInfinite() {
		countMode = AppleseedCountMode.Infinite;
		intervalCount = -1;
		dropCount = -1;
	}

	public boolean decrementCount() {
		if(treeType.getRequireFertilizer() == false || countMode == AppleseedCountMode.Infinite)
			return true;
		else if(countMode == AppleseedCountMode.Drop && dropCount > 0) {
			dropCount--;
			return true;
		} else if(countMode == AppleseedCountMode.Interval && intervalCount > 0) {
			intervalCount--;
			return true;
		}

		return false;
	}

	public boolean Fertilize() {
		if(isInfinite())
			return true;
		else if(fertilizerCount > 0) {
			fertilizerCount--;
			ResetDropCount();
			return true;
		} else if(fertilizerCount == -1) {
			ResetDropCount();
			return true;
		}

		return false;
	}

	public boolean needsFertilizer() {
		if(!isInfinite())
			if((dropCount == 0 || intervalCount == 0) && treeType.getRequireFertilizer())
				return true;

		return false;
	}

	public boolean isAlive() {
		if(isInfinite())
			return true;
		else if(treeType.getRequireFertilizer() == false)
			return true;
		else if(countMode == AppleseedCountMode.Drop && dropCount > 0)
			return true;
		else if(countMode == AppleseedCountMode.Interval && intervalCount > 0)
			return true;
		else if(fertilizerCount > 0 || fertilizerCount == -1)
			return true;

		return false;
	}

	public AppleseedCountMode getCountMode() {
		return countMode;
	}

	public boolean hasSign() {
		return signLocation != null;
	}

	public AppleseedLocation getSign() {
		return signLocation;
	}

	public void setSign(Location loc) {
		if(loc != null)
			signLocation = new AppleseedLocation(loc);
		else
			signLocation = null;
	}
}
