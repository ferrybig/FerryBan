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

/**
 *
 * @author Fernando
 */
public class PardonCommand implements CommandExecutor {
	private final Main plugin;

	public PardonCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 1) {
			return false;
		}
		if(!command.testPermission(sender))
			return true;
		OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[0]);
		if(player == null) {
			sender.sendMessage("Player not found!");
			return true;
		}
		Command.broadcastCommandMessage(sender, "Unbanning player: " + player.getName() + " ("+player.getUniqueId() + ")");
		if(player.getFirstPlayed() == 0) {
			sender.sendMessage("Warning: this player has not played before");
		}
		plugin.playerBans.remove(player.getUniqueId());
		plugin.scheduleSave();
		return true;
	}
}
