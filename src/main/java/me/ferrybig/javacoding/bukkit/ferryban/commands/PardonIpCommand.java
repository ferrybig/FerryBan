/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import me.ferrybig.javacoding.bukkit.ferryban.BanInfo;
import me.ferrybig.javacoding.bukkit.ferryban.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

/**
 *
 * @author Fernando
 */
public class PardonIpCommand implements TabExecutor{
	private final Main plugin;

	public PardonIpCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 1) {
			return false;
		}
		InetAddress ip;
		if(args[0].matches("[a-zA-Z0-9_-]{1,16}"))
		{
			OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[0]);
			sender.sendMessage("Ip banning player: " + player.getName() + " ("+player.getUniqueId() + ")");
			if(player.getFirstPlayed() == 0) {
				sender.sendMessage("Warning: this player has not played before");
			}
			ip = plugin.playerToIp.get(player.getUniqueId());
			if(ip == null) {
				sender.sendMessage("Ip address not found");
				return true;
			}
		} else {
			try {
				ip = InetAddress.getByName(args[0]);
			} catch (UnknownHostException ex) {
				sender.sendMessage("Ip address not found: " + ex.getMessage());
				return true;
			}
		}
		if(this.plugin.playerBans.remove(ip) == null) {
			sender.sendMessage("Ip not banned!");
		} else {
			sender.sendMessage("Ip is unbanned!");
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> l = new ArrayList<>();
		for(InetAddress addr : this.plugin.ipBans.keySet()) {
			l.add(addr.getHostAddress());
		}
		return l;
	}
	
	
	
	
}