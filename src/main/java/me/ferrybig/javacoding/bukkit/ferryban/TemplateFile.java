/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.bukkit.ChatColor;

/**
 *
 * @author Fernando
 */
public class TemplateFile {

	private final Main main;
	
	private final File file;
	private String contents = "";

	public TemplateFile(Main main, File file) {
		this.main = main;
		this.file = file;
	}

	public String getFormat() {
		return contents;
	}

	public void load(List<Exception> exceptionHandler) {
		try {
			if (!file.exists()) {
				main.saveResource(file.getName(), false);
			}
			try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					total.append(line).append('\n');
				}
				this.contents = ChatColor.translateAlternateColorCodes('&', total.toString());
			}
		} catch (IOException ex) {
			exceptionHandler.add(ex);
		}
	}
	
}
