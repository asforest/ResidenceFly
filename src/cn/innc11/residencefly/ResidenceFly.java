package cn.innc11.residencefly;

import java.util.HashSet;

import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceEnterEvent;
import com.bekvon.bukkit.residence.event.ResidenceLeaveEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
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
	
	@Override
	public void onEnable() 
	{
		FlagPermissions.addFlag("fly");
		
		tempWhiteList.clear();
		
		getServer().getPluginManager().registerEvent(ResidenceEnterEvent.class, this, EventPriority.HIGH, x, this);
		getServer().getPluginManager().registerEvent(ResidenceLeaveEvent.class, this, EventPriority.HIGH, x, this);
		getServer().getPluginManager().registerEvent(ResidenceCreationEvent.class, this, EventPriority.HIGH, x, this);
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
						player.setAllowFlight(true);
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
					player.setAllowFlight(false);
				}
			}
			
		}
		
	}
}
