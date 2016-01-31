/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban.commands;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import me.ferrybig.javacoding.bukkit.ferryban.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

/**
 *
 * @author Fernando
 */
public class ReloadCommand implements TabExecutor {

	private final Main main;

	public ReloadCommand(Main main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.testPermission(sender)) {
			return true;
		}
		try {
			main.load();
			sender.sendMessage("Plugin reloaded");
		} catch (IOException ex) {
			sender.sendMessage("Problems with configuration, check console");
			main.getLogger().log(Level.WARNING, "Problems with config:", ex);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return Collections.emptyList();
	}

}
