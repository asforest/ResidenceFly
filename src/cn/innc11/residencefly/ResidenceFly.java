package cn.innc11.residencefly;

import java.util.HashMap;
import java.util.HashSet;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Server;
import cn.nukkit.entity.data.EntityData;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.level.Location;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceEnterEvent;
import com.bekvon.bukkit.residence.event.ResidenceLeaveEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

import cn.nukkit.Player;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.EventExecutor;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.EventException;

@SuppressWarnings("deprecation")
public class ResidenceFly extends PluginBase implements Listener 
{
	PlayerResidenceEnterEvent x = new PlayerResidenceEnterEvent();
	
	HashSet<String> tempWhiteList = new HashSet<String>();

	final String[] directions = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖", "▲", "▼"}; // up . down
	
	@Override
	public void onEnable() 
	{
		FlagPermissions.addFlag("fly");
		
		tempWhiteList.clear();

		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvent(ResidenceEnterEvent.class, this, EventPriority.HIGH, x, this);
		getServer().getPluginManager().registerEvent(ResidenceLeaveEvent.class, this, EventPriority.HIGH, x, this);
		getServer().getPluginManager().registerEvent(ResidenceCreationEvent.class, this, EventPriority.HIGH, x, this);
		getServer().getPluginManager().registerEvent(ResidenceDeleteEvent.class, this, EventPriority.HIGH, x, this);
	}

	float getDistance(ClaimedResidence res, Location loc)
	{
		return 0;
	}

	int dirRotate(int dircetion, int rotate)
	{
		return (dircetion + rotate) % 8;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e)
	{
		Player player = e.getPlayer();
		ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if(Residence.getPermsByLocForPlayer(player.getLocation(), player).has("fly", false) && !(player.isOnGround() || player.isOnLadder()))
		{
			//Server.getInstance().broadcastMessage(res.getAreaArray().length+"");
			CuboidArea area = res.getAreaArray()[0];
			double lowX = area.getLowLoc().x;
			double lowY = area.getLowLoc().y;
			double lowZ = area.getLowLoc().z;
			double highX = area.getHighLoc().x;
			double highY = area.getHighLoc().y;
			double highZ = area.getHighLoc().z;

			double playerX = player.x;
			double playerY = player.y;
			double playerZ = player.z;

			double lowXdiff = Math.abs(lowX-playerX);
			double lowYdiff = Math.abs(lowY-playerY);
			double lowZdiff = Math.abs(lowZ-playerZ);
			double highXdiff = Math.abs(highX-playerX);
			double highYdiff = Math.abs(highY-playerY);
			double highZdiff = Math.abs(highZ-playerZ);

			double nearerXdiff = Math.min(lowXdiff, highXdiff);
			double nearerYdiff = Math.min(lowYdiff, highYdiff);
			double nearerZdiff = Math.min(lowZdiff, highZdiff);

			boolean isLowXnear = lowXdiff < highXdiff;
			boolean isLowYnear = lowYdiff < highYdiff;
			boolean isLowZnear = lowZdiff < highZdiff;
			int nearestAxis = -1; // x y z

			if(nearerXdiff < Math.min(nearerYdiff, nearerZdiff))
				nearestAxis = 0;
			if(nearerYdiff < Math.min(nearerXdiff, nearerZdiff))
				nearestAxis = 1;
			if(nearerZdiff < Math.min(nearerXdiff, nearerYdiff))
				nearestAxis = 2;

			double playerYaw = player.getYaw();

			String ico = "+";

			int dir = 0;

			if(playerYaw>(360-22.5) || playerYaw<(22.5+45*0)) dir = 0; else
			if(playerYaw>(22.5+45*0) && playerYaw<(22.5+45*1)) dir = 7; else
			if(playerYaw>(22.5+45*1) && playerYaw<(22.5+45*2)) dir = 6; else
			if(playerYaw>(22.5+45*2) && playerYaw<(22.5+45*3)) dir = 5; else
			if(playerYaw>(22.5+45*3) && playerYaw<(22.5+45*4)) dir = 4; else
			if(playerYaw>(22.5+45*4) && playerYaw<(22.5+45*5)) dir = 3; else
			if(playerYaw>(22.5+45*5) && playerYaw<(22.5+45*6)) dir = 2; else
			if(playerYaw>(22.5+45*6) && playerYaw<(22.5+45*7)) dir = 1;

			if(nearestAxis == 0) // x
				dir = dirRotate(dir, isLowXnear? 2:6);

			if(nearestAxis == 1) // y
				dir = isLowYnear? 9:8;

			if(nearestAxis == 2) // 2
				dir = dirRotate(dir, isLowZnear? 4:0);

			ico = directions[dir];

			double nearestDistance = Math.min(nearerXdiff, Math.min(nearerYdiff, nearerZdiff));
/*
			player.sendPopup(String.format("nX: %.0f, nY: %.0f, nZ: %.0f   yaw:%.1f   %s%.1f     %d",
					isLowXnear? -nearerXdiff:nearerXdiff,
					isLowYnear? -nearerYdiff:nearerYdiff,
					isLowZnear? -nearerZdiff:nearerZdiff,
					playerYaw,
					ico,
					nearestDistance,
					nearestAxis
					));

*/
			if(nearestDistance < 15)
				player.sendPopup(String.format("%s%.1f", ico, nearestDistance));
		}
	}
	
	public static class PlayerResidenceEnterEvent implements EventExecutor
	{
		@Override
		public void execute(Listener listener, Event e) throws EventException 
		{
			if(e instanceof ResidenceCreationEvent && listener instanceof ResidenceFly)
			{
				ClaimedResidence cResidence = ((ResidenceCreationEvent)e).getResidence();
				cResidence.getPermissions().setPlayerFlag(cResidence.getOwner(), "fly", FlagState.TRUE);
			}

			if(e instanceof ResidenceEnterEvent && listener instanceof ResidenceFly)
			{
				Player player = ((ResidenceEnterEvent) e).getPlayer();
				
				if(player.getAllowFlight())
				{
					((ResidenceFly)listener).tempWhiteList.add(player.getName());
				} else {
					if(((ResidenceEnterEvent) e).getResidence().getPermissions().playerHas(player.getName(), "fly", false))
					{
						player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, true);
						player.getAdventureSettings().update();
					}
				}
				
			}
			
			if(e instanceof ResidenceDeleteEvent && listener instanceof ResidenceFly)
			{
				for(Player player : ((ResidenceDeleteEvent) e).getResidence().getPlayersInResidence())
				{
					if(!((ResidenceFly) listener).tempWhiteList.contains(player.getName()))
					{
						player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, false);
						player.getAdventureSettings().set(AdventureSettings.Type.FLYING, false);
						player.getAdventureSettings().update();
					}
				}
			}
			
			if(e instanceof ResidenceLeaveEvent && listener instanceof ResidenceFly)
			{
				Player player = ((ResidenceLeaveEvent) e).getPlayer();
				
				if(((ResidenceFly)listener).tempWhiteList.contains(player.getName()))
				{
					((ResidenceFly)listener).tempWhiteList.remove(player.getName());
				} else {
					player.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT, false);
					player.getAdventureSettings().set(AdventureSettings.Type.FLYING, false);
					player.getAdventureSettings().update();
				}
			}
			
		}
		
	}
}
