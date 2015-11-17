/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import me.ferrybig.javacoding.bukkit.ferryban.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 *
 * @author Fernando
 */
public class KickIpCommand implements TabExecutor {

	private final Main plugin;

	public KickIpCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		if (!command.testPermission(sender)) {
			return true;
		}
		if (args[0].matches("[a-zA-Z0-9_-]{1,16}")) {
			OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[0]);
			sender.sendMessage("Ip kicking player: " + player.getName() + " (" + player.getUniqueId() + ")");
			if (player.getFirstPlayed() == 0) {
				sender.sendMessage("Warning: this player has not played before");
			}
			StringBuilder reason = new StringBuilder();
			for (int i = 1; i < args.length; i++) {
				reason.append(args[i]).append(' ');
			}
			plugin.actOnIp(sender, player.getUniqueId(), reason.toString().trim(), 0);
		} else {
			try {
				InetAddress ip = InetAddress.getByName(args[0]);
				sender.sendMessage("Ip kicking: " + ip.getHostAddress());
				StringBuilder reason = new StringBuilder();
				for (int i = 1; i < args.length; i++) {
					reason.append(args[i]).append(' ');
				}
				plugin.actOnIp(sender, ip, reason.toString().trim(), 0);
			} catch (UnknownHostException ex) {
				sender.sendMessage("Ip address not found: " + ex.getMessage());
			}

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
