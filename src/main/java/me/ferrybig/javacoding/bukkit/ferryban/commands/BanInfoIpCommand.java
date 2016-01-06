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
import java.util.Set;
import java.util.TreeSet;
import me.ferrybig.javacoding.bukkit.ferryban.BanInfo;
import me.ferrybig.javacoding.bukkit.ferryban.Main;
import static me.ferrybig.javacoding.bukkit.ferryban.Main.CONSOLE;
import me.ferrybig.javacoding.bukkit.ferryban.utils.TimeConverter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 *
 * @author Fernando
 */
public class BanInfoIpCommand implements TabExecutor{
	private final Main plugin;

	public BanInfoIpCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 1) {
			return false;
		}
		if(!command.testPermission(sender))
			return true;
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
		BanInfo info = this.plugin.playerBans.get(ip);
		if(info == null) {
			sender.sendMessage("Ip not banned!");
		} else {
			sender.sendMessage("Ip is banned!");
			sender.sendMessage("Banner: " + (info.getBanner().equals(CONSOLE) ? "Console" : this.plugin.getServer().getOfflinePlayer(info.getBanner()).getName()));
			sender.sendMessage("Banned until: " + Main.DATE_FORMATTER.format(new Date(info.getUntil())));
			sender.sendMessage("Time remaining: " + TimeConverter.getMessage(info.getUntil() - System.currentTimeMillis(),2));
			sender.sendMessage("Reason: "+ info.getReason());
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		Set<String> l = new TreeSet<>();
		for(Player addr : this.plugin.getServer().getOnlinePlayers()) {
			l.add(addr.getAddress().getAddress().getHostAddress());
		}
		return new ArrayList<>(l);
	}
}