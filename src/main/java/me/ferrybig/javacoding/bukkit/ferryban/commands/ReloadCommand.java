/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.commands;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ferrybig.javacoding.bukkit.ferryban.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Fernando
 */
public class ReloadCommand implements CommandExecutor {
	private final Main main;

	public ReloadCommand(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!command.testPermission(sender))
			return true;
		try {
			main.load();
			sender.sendMessage("Plugin reloaded");
		} catch (IOException ex) {
			sender.sendMessage("Problems with comfiguration, check console");
			main.getLogger().log(Level.WARNING, "Problems with config:", ex);
		}
		return true;
	}
	
}
