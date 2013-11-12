## WorldGuardDistanceMessage
A basic Bukkit plugin that tells players the shortest way out of a WorldGuard region.

### It works like this:
When the plugin is enabled, it reads the config file and loads an internal list of regions to look out for.
When a player attempts to place or break a block, the plugins checks to see if the block location is inside one of the defined regions. If it is, it determines how far the player has to move to get out of the region. This info is then sent to the player.

### Configuration:
Pretty straight-forward. You add worlds as needed, and then regions in each world.
For each region you can set a message and color. If none is provided, a default message will be used and the color will be set to red.

#### Example:
    worlds:
        world:
            spawn:
                message: "You can't build here. Go %info% to leave the protected area."
                color: c
            
With 'spawn' being the actual name of the WorldGuard region.

Use the template tag %info% to insert the part that says "100 blocks north or 50 blocks east". If you don't want to tell the player how far he has to go, simply don't use the %info% tag.

### Dependencies
WorldEdit and WorldGuard of course. Nothing else.
