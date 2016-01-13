/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import me.ferrybig.javacoding.bukkit.ferryban.Main;
import me.ferrybig.javacoding.bukkit.ferryban.utils.TimeConverter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

/**
 *
 * @author Fernando
 */
public class TempBanIpCommand implements TabExecutor {

	private final Main plugin;

	public TempBanIpCommand(Main plugin) {
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
			sender.sendMessage("Ip banning player: " + player.getName() + " (" + player.getUniqueId() + ")");
			if (player.getFirstPlayed() == 0) {
				sender.sendMessage("Warning: this player has not played before");
			}
			long time = TimeConverter.getLong(args[0]);
			if (time == 0) {
				sender.sendMessage("Warning: time cannot be parsed");
				return true;
			}
			sender.sendMessage("Will be banned for: " + TimeConverter.getMessage(time, 5));
			StringBuilder reason = new StringBuilder();
			for (int i = 2; i < args.length; i++) {
				reason.append(args[i]).append(' ');
			}
			plugin.actOnIp(sender, player.getUniqueId(), reason.toString().trim(), System.currentTimeMillis() + time);
			return true;
		} else {
			try {
				InetAddress ip = InetAddress.getByName(args[0]);
				sender.sendMessage("Ip banning: " + ip.getHostAddress());
				long time = TimeConverter.getLong(args[0]);
				if (time == 0) {
					sender.sendMessage("Warning: time cannot be parsed");
					return true;
				}
				sender.sendMessage("Will be banned for: " + TimeConverter.getMessage(time, 5));
				StringBuilder reason = new StringBuilder();
				for (int i = 2; i < args.length; i++) {
					reason.append(args[i]).append(' ');
				}
				plugin.actOnIp(sender, ip, reason.toString().trim(), System.currentTimeMillis() + time);
			} catch (UnknownHostException ex) {
				sender.sendMessage("Ip address not found: " + ex.getMessage());
			}

		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}

}
