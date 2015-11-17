/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.commands;

import me.ferrybig.javacoding.bukkit.ferryban.Main;
import me.ferrybig.javacoding.bukkit.ferryban.utils.TimeConverter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Fernando
 */
public class TempBanCommand implements CommandExecutor{
	private final Main plugin;

	public TempBanCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2) {
			return false;
		}
		if(!command.testPermission(sender))
			return true;
		OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[0]);
		if(player == null) {
			sender.sendMessage("Player not found!");
			return true;
		}
		sender.sendMessage("Banning player: " + player.getName() + " ("+player.getUniqueId() + ")");
		if(player.getFirstPlayed() == 0) {
			sender.sendMessage("Warning: this player has not played before");
		}
		long time = TimeConverter.getLong(args[1]);
		if(time == 0) {
			sender.sendMessage("Warning: time cannot be parsed");
			return true;
		}
		sender.sendMessage("Will be banned for: " + TimeConverter.getMessage(time, 5));
		StringBuilder reason = new StringBuilder();
		for(int i = 2; i < args.length; i++) {
			reason.append(args[i]).append(' ');
		}
		plugin.actOnPlayer(sender, player.getUniqueId(), reason.toString().trim(), System.currentTimeMillis() + time);
		return true;
	}
	
	
}
