/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.commands;

import me.ferrybig.javacoding.bukkit.ferryban.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Fernando
 */
public class KickCommand implements CommandExecutor{
	private final Main plugin;

	public KickCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 1) {
			return false;
		}
		if(!command.testPermission(sender))
			return true;
		Player player = plugin.getServer().getPlayer(args[0]);
		if(player == null) {
			sender.sendMessage("Player not found!");
			return true;
		}
		sender.sendMessage("Kicking player: " + player.getName() + " ("+player.getUniqueId() + ")");
		if(player.getFirstPlayed() == 0) {
			sender.sendMessage("Warning: this player has not played before");
		}
		StringBuilder reason = new StringBuilder();
		for(int i = 1; i < args.length; i++) {
			reason.append(args[i]).append(' ');
		}
		plugin.actOnPlayer(sender, player.getUniqueId(), reason.toString().trim(), 0);
		return true;
	}
	
	
}