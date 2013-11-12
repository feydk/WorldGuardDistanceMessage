package io.github.feydk.WGDistanceMessage;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.WGBukkit;
import static com.sk89q.worldguard.bukkit.BukkitUtil.*;
import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import com.sk89q.worldedit.BlockVector;

public final class WGDistanceMessage extends JavaPlugin implements Listener
{
	private ChatColor color;
	private String region;

	@Override
	public void onEnable()
	{
		getLogger().info("WGDistanceMessage loaded");

		this.saveDefaultConfig();

		try
		{
			String config_color = getConfig().getString("color");
			this.color = ChatColor.getByChar(config_color);
		}
		catch(Exception ex)
		{
			this.color = ChatColor.RED;
		}

		this.region = getConfig().getString("region");

		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(event.isCancelled())
		{
			HandleEvent(event.getPlayer(), event.getBlockPlaced().getLocation());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled())
		{
			HandleEvent(event.getPlayer(), event.getBlock().getLocation());
		}
	}

	private void HandleEvent(Player player, Location blockLocation)
	{
		RegionManager manager = WGBukkit.getRegionManager(player.getWorld());
		ProtectedRegion region = manager.getRegion(this.region);

		// If placed block is inside region..
		if(region.contains(toVector(blockLocation)))
		{
			// Is the player also inside region?
			if(region.contains(toVector(player.getLocation())))
			{
				int we_distance = 100;
				int ns_distance = 130;
				String we_direction = "west";
				String ns_direction = "north";

				BlockVector minimumPoint = region.getMinimumPoint();
				BlockVector maximumPoint = region.getMaximumPoint();

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

				if(ns_distance == 0)
					ns_distance = 1;

				if(we_distance == 0)
					we_distance = 1;

				if(ns_distance < 0)
					ns_distance = 0;

				if(we_distance < 0)
					we_distance = 0;

				String msg = "This is a protected area where you can't build or break blocks. You need to travel ";

				if(ns_distance > 0 && we_distance > 0)
				{
					msg += ns_distance + " block";

					if(ns_distance > 1)
						msg += "s";

					msg += " " + ns_direction + " or " + we_distance + " block";

					if(we_distance > 1)
						msg += "s";

					msg += " " + we_direction;
				}
				else if(ns_distance > 0)
				{
					msg += ns_distance + " block";

					if(ns_distance > 1)
						msg += "s";

					msg += " " + ns_direction;
				}
				else if(we_distance > 0)
				{
					msg += we_distance + " block";

					if(we_distance > 1)
						msg += "s";

					msg += " " + we_direction;
				}

				msg += " to leave the protected area.";

				player.sendMessage(this.color + msg);
			}
		}
	}
}