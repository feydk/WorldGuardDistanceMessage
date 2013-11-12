package io.github.feydk.WGDistanceMessage;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import com.sk89q.worldguard.bukkit.WGBukkit;
import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
import com.sk89q.worldedit.BlockVector;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public final class WGDistanceMessage extends JavaPlugin implements Listener
{
	private ArrayList<Region> regions;

	@Override
	public void onEnable()
	{
		this.saveDefaultConfig();

		Set<String> worldNames = getConfig().getConfigurationSection("worlds").getKeys(false);

		if(worldNames != null)
		{
			regions = new ArrayList<Region>();

			for(String worldName : worldNames)
			{
				Set<String> regionNames = getConfig().getConfigurationSection("worlds." + worldName).getKeys(false);

				for(String regionName : regionNames)
				{
					Region region = new Region(worldName, regionName, getConfig().getString("worlds." + worldName + "." + regionName + ".color"), getConfig().getString("worlds." + worldName + "." + regionName + ".message"));

					if(region.wgRegion != null)
					{
						regions.add(region);
						getLogger().info("Added WorldGuard region '" + regionName + "' in world '" + worldName + "'.");
					}
					else
					{
						getLogger().warning("WorldGuard region '" + regionName + "' not found in world '" + worldName + "'.");
					}
				}
			}
		}

		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		Location location = event.getBlockPlaced().getLocation();
		Region region = getMatchedRegion(player, location);

		if(region != null && !WGBukkit.getPlugin().canBuild(player, location))
		{
			handleEvent(player, region);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		Location location = event.getBlock().getLocation();
		Region region = getMatchedRegion(player, location);

		if(region != null && !WGBukkit.getPlugin().canBuild(player, location))
		{
			handleEvent(player, region);
			event.setCancelled(true);
		}
	}

	private Region getMatchedRegion(Player player, Location blockLocation)
	{
		for(Region region : this.regions)
		{
			if(region.world.compareTo(player.getWorld().getName()) == 0)
			{
				if(region.wgRegion.contains(toVector(blockLocation)))
				{
					return region;
				}
			}
		}

		return null;
	}

	private void handleEvent(Player player, Region region)
	{
		// Is the player also inside region?
		if(region.wgRegion.contains(toVector(player.getLocation())))
		{
			String msg = region.message;

			// Only bother with all this if the message contains %info%.
			if(msg.contains("%info%"))
			{
				int we_distance = 0;
				int ns_distance = 0;
				String we_direction = "";
				String ns_direction = "";

				BlockVector minimumPoint = region.wgRegion.getMinimumPoint();
				BlockVector maximumPoint = region.wgRegion.getMaximumPoint();

				//player.sendMessage("Region: " + minimumPoint.getX() + ", " + minimumPoint.getZ() + " - " + maximumPoint.getX() + ", " + maximumPoint.getZ());

				double min_x = minimumPoint.getX();
				double max_x = maximumPoint.getX();
				double min_z = minimumPoint.getZ();
				double max_z = maximumPoint.getZ();
				double player_x = Math.round(player.getLocation().getX());
				double player_z = Math.round(player.getLocation().getZ());

				// Player is closest to min_x (west).
				if((player_x - min_x) < (max_x - player_x))
				{
					we_direction = "west";
					we_distance = (int)(player_x - (min_x + 1));
				}
				// Player is closest to max_x (east).
				else if((max_x - player_x) < (player_x - min_x))
				{
					we_direction = "east";
					we_distance = (int)((max_x + 1) - player_x);
				}

				// If we don't have a direction, it's most likely because we're right in the middle, so just pick a random direction.
				if(we_direction == "")
				{
					String[] strings = {"west", "east"};
					Random r = new Random();

					we_direction = strings[r.nextInt(2)];
				}

				// Player is closest to min_z (north).
				if((player_z - min_z) < (max_z - player_z))
				{
					ns_direction = "north";
					ns_distance = (int)(player_z - (min_z + 1));
				}
				// Player is closest to max_z (south).
				else if((max_z - player_z) < (player_z - min_z))
				{
					ns_direction = "south";
					ns_distance = (int)((max_z + 1) - player_z);
				}

				// If we don't have a direction, it's most likely because we're right in the middle, so just pick a random direction.
				if(ns_direction == "")
				{
					String[] strings = {"north", "south"};
					Random r = new Random();

					ns_direction = strings[r.nextInt(2)];
				}

				// Tidy up distances a bit. Makes no sense to output 0 or negative values.
				if(ns_distance == 0)
					ns_distance = 1;

				if(we_distance == 0)
					we_distance = 1;

				if(ns_distance < 0)
					ns_distance = 0;

				if(we_distance < 0)
					we_distance = 0;

				String info = "";

				if(ns_distance > 0 && we_distance > 0)
				{
					info += ns_distance + " block";

					if(ns_distance > 1)
						info += "s";

					info += " " + ns_direction + " or " + we_distance + " block";

					if(we_distance > 1)
						info += "s";

					info += " " + we_direction;
				}
				else if(ns_distance > 0)
				{
					info += ns_distance + " block";

					if(ns_distance > 1)
						info += "s";

					info += " " + ns_direction;
				}
				else if(we_distance > 0)
				{
					info += we_distance + " block";

					if(we_distance > 1)
						info += "s";

					info += " " + we_direction;
				}

				msg = msg.replaceAll("%info%", info);
			}

			player.sendMessage(region.color + msg);
		}
	}
}