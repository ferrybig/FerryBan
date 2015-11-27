/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.commands;

import java.util.Date;
import me.ferrybig.javacoding.bukkit.ferryban.BanInfo;
import me.ferrybig.javacoding.bukkit.ferryban.Main;
import static me.ferrybig.javacoding.bukkit.ferryban.Main.CONSOLE;
import me.ferrybig.javacoding.bukkit.ferryban.utils.TimeConverter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Fernando
 */
public class BanInfoCommand implements CommandExecutor{
	private final Main plugin;

	public BanInfoCommand(Main plugin) {
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
		BanInfo info = this.plugin.playerBans.get(player.getUniqueId());
		if(info == null) {
			sender.sendMessage("Player not banned!");
		} else {
			sender.sendMessage("Player is banned!");
			sender.sendMessage("Banner: " + (info.getBanner().equals(CONSOLE) ? "Console" : this.plugin.getServer().getOfflinePlayer(info.getBanner()).getName()));
			sender.sendMessage("Banned until: " + Main.df.format(new Date(info.getUntil())));
			sender.sendMessage("Time remaining: " + TimeConverter.getMessage(info.getUntil() - System.currentTimeMillis(),2));
			sender.sendMessage("Reason: "+ info.getReason());
		}
		return true;
	}
	
	
}