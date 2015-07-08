package com.teozcommunity.teozfrank.duelme.events;

import com.teozcommunity.teozfrank.duelme.main.DuelMe;
import com.teozcommunity.teozfrank.duelme.mysql.FieldName;
import com.teozcommunity.teozfrank.duelme.mysql.MySql;
import com.teozcommunity.teozfrank.duelme.util.DuelManager;
import com.teozcommunity.teozfrank.duelme.util.FileManager;
import com.teozcommunity.teozfrank.duelme.util.SendConsoleMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Created by Frank on 04/05/2015.
 */
public class PlayerDeath implements Listener {

    private DuelMe plugin;

    public PlayerDeath(DuelMe plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();

        DuelManager dm = plugin.getDuelManager();
        FileManager fm = plugin.getFileManager();
        MySql mySql = plugin.getMySql();
        if(dm.isInDuel(playerUUID)){
            dm.addDeadPlayer(playerUUID);

            if(fm.isMySqlEnabled()) {
                mySql.addPlayerKillDeath(playerUUID, playerName, FieldName.DEATH);
            }

            if(e.getEntity().getKiller() instanceof Player){
                final Player killer = e.getEntity().getKiller();
                String killerName = killer.getName();
                double x = -523.5;
                double y = 67.0;
                double z = 179.5;
                final World w = Bukkit.getWorld("world");
                final Location def = new Location(w, x, y, z);
                def.setYaw((float) 90.0);
                def.setPitch((float) 0.0);
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
                {
                  public void run()
                  {
                    killer.teleport(def);
                    killer.sendMessage(ChatColor.GOLD + "You have been teleported to the server spawn");
                  }
                }, 1L);
                if(fm.isMySqlEnabled()) {
                    mySql.addPlayerKillDeath(playerUUID, killerName, FieldName.KILL);
                }

                if(!fm.isDropItemsOnDeathEnabled()) {
                    if(plugin.isDebugEnabled()) {
                        SendConsoleMessage.debug("Item drops disabled, clearing.");
                    }
                    e.setKeepInventory(true);
                    e.getDrops().clear();
                }

                if(!fm.isDeathMessagesEnabled()){
                    e.setDeathMessage("");
                    return;
                }
                e.setDeathMessage(fm.getPrefix() + ChatColor.AQUA + player.getName() + ChatColor.RED + " was killed in a duel by "
                        + ChatColor.AQUA + killer.getName());
            }  else {
                if(!fm.isDeathMessagesEnabled()){
                    e.setDeathMessage("");
                    return;
                }
                e.setDeathMessage(fm.getPrefix() + ChatColor.AQUA + player.getName() + ChatColor.RED + " was killed in a duel!");
            }
            dm.endDuel(player);
        }
    }

}
