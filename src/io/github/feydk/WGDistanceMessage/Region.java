package io.github.feydk.WGDistanceMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Region
{
	public String world;
	public String name;
	public String message;
	public ChatColor color;
	public ProtectedRegion wgRegion;

	public Region(String world, String name, String color, String message)
	{
		this.world = world;
		this.name = name;
		this.message = message;

		// Default message if none is provided.
		if(this.message == null || this.message.isEmpty())
			this.message = "This is a protected area where you can't build or break blocks.";

		// Default color if none is provided or is invalid.
		try
		{
			this.color = ChatColor.getByChar(color);
		}
		catch(Exception ex)
		{
			this.color = ChatColor.RED;
		}

		RegionManager manager = WGBukkit.getRegionManager(Bukkit.getWorld(this.world));
        this.wgRegion = manager.getRegion(this.name);
	}
}