/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import me.ferrybig.javacoding.bukkit.ferryban.commands.BanCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.BanInfoCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.BanInfoIpCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.BanIpCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.KickCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.KickIpCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.PardonCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.PardonIpCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.ReloadCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.TempBanCommand;
import me.ferrybig.javacoding.bukkit.ferryban.commands.TempBanIpCommand;
import me.ferrybig.javacoding.bukkit.ferryban.utils.TimeConverter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Fernando
 */
public class Main extends JavaPlugin implements Listener {

	private static final Charset UTF8 = StandardCharsets.UTF_8;
	private static final String BAN_FILE_CONFIG_HEADER = "#| FerryBan config V ";
	private static final String IP_BANS_FILE = "ipbans.txt";
	private static final String PLAYER_BANS_FILE = "playerbans.txt";
	private static final String CONFIG_VERSION = "1";

	public static final UUID CONSOLE = UUID.nameUUIDFromBytes("CONSOLE".getBytes(UTF8));
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm (X)");

	public final Map<InetAddress, IpBanInfo> ipBans = new HashMap<>();
	public final Map<UUID, PlayerBanInfo> playerBans = new HashMap<>();

	public final Map<UUID, InetAddress> playerToIp = new SizeLimitedLinkedMap<>(100);
	public final Map<InetAddress, UUID> ipToPlayer = new SizeLimitedLinkedMap<>(100);

	private boolean enabled = false;

	private TemplateFile banFormat;
	private TemplateFile foreverBanFormat;
	private TemplateFile kickFormat;

	private TemplateFile kickGameFormat;
	private TemplateFile banGameFormat;

	private BukkitRunnable saveTask;

	@Override
	public void onEnable() {
		try {
			load();
		} catch (IOException e) {
			this.getLogger().log(Level.SEVERE, "Failed to load configuration files", e);
			this.getServer().getPluginManager().disablePlugin(this);
			this.setEnabled(false);
			return;
		}
		this.getCommand("ban").setExecutor(new BanCommand(this));
		this.getCommand("ban-ip").setExecutor(new BanIpCommand(this));
		this.getCommand("ban-info").setExecutor(new BanInfoCommand(this));
		this.getCommand("ban-info-ip").setExecutor(new BanInfoIpCommand(this));
		this.getCommand("temp-ban-ip").setExecutor(new TempBanIpCommand(this));
		this.getCommand("temp-ban").setExecutor(new TempBanCommand(this));
		this.getCommand("kick").setExecutor(new KickCommand(this));
		this.getCommand("kick-ip").setExecutor(new KickIpCommand(this));
		this.getCommand("pardon").setExecutor(new PardonCommand(this));
		this.getCommand("pardon-ip").setExecutor(new PardonIpCommand(this));
		this.getCommand("ferrybanreload").setExecutor(new ReloadCommand(this));
		this.getServer().getPluginManager().registerEvents(this, this);
		enabled = true;
	}

	@Override
	public void onDisable() {
		if (!enabled) {
			return;
		}
		if (this.saveTask != null) {
			this.saveTask.run();
			this.saveTask = null;
		}
		enabled = false;
	}

	public void load() throws IOException {
		File bansInfo = new File(this.getDataFolder(), IP_BANS_FILE);
		File playersInfo = new File(this.getDataFolder(), PLAYER_BANS_FILE);
		{
			File legacyBansInfo = new File(this.getDataFolder(), IP_BANS_FILE.replace("txt", "yml"));
			if(legacyBansInfo.exists() && !bansInfo.exists()) {
				legacyBansInfo.renameTo(bansInfo);
			}
			File legacyplayersInfo = new File(this.getDataFolder(), PLAYER_BANS_FILE.replace("txt", "yml"));
			if(legacyplayersInfo.exists() && !bansInfo.exists()) {
				legacyplayersInfo.renameTo(bansInfo);
			}
		}
		banFormat = new TemplateFile(this, new File(this.getDataFolder(), "ban.template"));
		kickFormat = new TemplateFile(this, new File(this.getDataFolder(), "kick.template"));
		foreverBanFormat = new TemplateFile(this, new File(this.getDataFolder(), "banforever.template"));
		kickGameFormat = new TemplateFile(this, new File(this.getDataFolder(), "kick.ingame.template"));
		banGameFormat = new TemplateFile(this, new File(this.getDataFolder(), "ban.ingame.template"));
		List<Exception> exceptions = new ArrayList<>();
		long now = System.currentTimeMillis();

		if (bansInfo.exists()) {
			ipBans.clear();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					bansInfo), UTF8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("#")) {
						continue;
					}
					String[] parts = line.split("\t", 4);
					if (parts.length < 4) {
						continue;
					}
					InetAddress addr = InetAddress.getByName(parts[0]);
					UUID banner = UUID.fromString(parts[1]);
					long time = Long.parseLong(parts[2]);
					String reason = parts[3];

					if (now > time) {
						getLogger().log(Level.INFO, "Ban of {0} has expired.", addr.getHostAddress());
						continue;
					}
					this.ipBans.put(addr, new IpBanInfo(addr, time, banner, reason));
				}
			} catch (IOException ex) {
				exceptions.add(ex);
			}
		}

		if (playersInfo.exists()) {
			playerBans.clear();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					playersInfo), UTF8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("#")) {
						continue;
					}
					String[] parts = line.split("\t", 4);
					if (parts.length < 4) {
						continue;
					}
					UUID userid = UUID.fromString(parts[0]);
					UUID banner = UUID.fromString(parts[1]);
					long time = Long.parseLong(parts[2]);
					String reason = parts[3];

					if (now > time) {
						getLogger().log(Level.INFO, "Ban of {0} has expired.", userid);
						continue;
					}
					this.playerBans.put(userid, new PlayerBanInfo(userid, time, banner, reason));
				}
			} catch (IOException ex) {
				exceptions.add(ex);
			}
		}

		kickGameFormat.load(exceptions);
		kickFormat.load(exceptions);
		banGameFormat.load(exceptions);
		banFormat.load(exceptions);
		foreverBanFormat.load(exceptions);

		if (!exceptions.isEmpty()) {
			if (exceptions.size() == 1) {
				throw new IOException("Problem loading config", exceptions.get(0));
			} else {
				IOException ex = new IOException("Problem loading config");
				for (Exception e : exceptions) {
					ex.addSuppressed(e);
				}
				throw ex;
			}
		}
	}

	private void refreshTask() {
		this.saveTask = this.createTask();
	}

	public void scheduleSave() {
		if (this.saveTask == null) {
			refreshTask();
			this.saveTask.runTaskTimer(this, AUTO_SAVE_INTERVAL, AUTO_SAVE_RETRY_INTERVAL);
		}
	}
	
	private static final int AUTO_SAVE_INTERVAL = 6000;
	private static final int AUTO_SAVE_RETRY_INTERVAL = 6000;

	private BukkitRunnable createTask() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				try {
					save();
					cancel();
					saveTask = null;
				} catch (IOException ex) {
					getLogger().log(Level.SEVERE, "Cannot save bans information!", ex);
				}
			}
		};
	}

	private void save() throws IOException {
		this.getDataFolder().mkdir();
		List<Exception> exceptions = new ArrayList<>();

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				new File(this.getDataFolder(), IP_BANS_FILE)), UTF8))) {
			writer.append(BAN_FILE_CONFIG_HEADER);
			writer.append(CONFIG_VERSION);
			writer.newLine();
			writer.append("# ");
			writer.newLine();
			for (IpBanInfo info : ipBans.values()) {
				writer.append(info.getIp().getHostAddress());
				writer.append('\t');
				writer.append(String.valueOf(info.getBanner()));
				writer.append('\t');
				writer.append(String.valueOf(info.getUntil()));
				writer.append('\t');
				writer.append(info.getReason());
				writer.newLine();
			}
		} catch (IOException ex) {
			exceptions.add(ex);
		}

		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				new File(this.getDataFolder(), PLAYER_BANS_FILE)), UTF8))) {
			writer.append(BAN_FILE_CONFIG_HEADER);
			writer.append(CONFIG_VERSION);
			writer.newLine();
			writer.append("# ");
			writer.newLine();
			for (PlayerBanInfo info : playerBans.values()) {
				writer.append(String.valueOf(info.getId()));
				writer.append('\t');
				writer.append(String.valueOf(info.getBanner()));
				writer.append('\t');
				writer.append(String.valueOf(info.getUntil()));
				writer.append('\t');
				writer.append(info.getReason());
				writer.newLine();
			}
		} catch (IOException ex) {
			exceptions.add(ex);
		}

		if (!exceptions.isEmpty()) {
			IOException e = new IOException("Problem saving plugin");
			for (Exception ee : exceptions) {
				e.addSuppressed(ee);
			}
			throw e;
		}
	}

	public String formatBanInfo(BanInfo info) {
		TemplateFile format;
		if (info.getUntil() == 0) {
			format = kickFormat;
		} else if (info.getUntil() == Long.MAX_VALUE) {
			format = foreverBanFormat;
		} else {
			format = banFormat;
		}
		return format.getFormat().replace("{{reason}}", info.getReason()).replace("{{expires}}", TimeConverter.getMessage(info.getUntil() - System.currentTimeMillis(), 1));
	}

	public void actOnIp(CommandSender banner, InetAddress address, String reason, long time) {
		IpBanInfo info = new IpBanInfo(address, time, banner instanceof Player ? ((Player) banner).getUniqueId() : CONSOLE, reason);
		if (time != 0) {
			banner.sendMessage("Banned ip address " + address);
			this.ipBans.put(address, info);
		} else {
			banner.sendMessage("Kicked ip address " + address);
		}
		String formatted = this.formatBanInfo(info);
		for (Map.Entry<UUID, InetAddress> entry : this.playerToIp.entrySet()) {
			if (entry.getValue().equals(address)) {
				OfflinePlayer player = this.getServer().getOfflinePlayer(entry.getKey());
				if (player.isOnline()) {
					((Player) player).kickPlayer(formatted);
					banner.sendMessage("Kicked " + player.getName() + " because he has a banned/kicked ip");
				}

			}
		}
		this.scheduleSave();
	}

	public void actOnIp(CommandSender banner, UUID username, String reason, long time) {
		OfflinePlayer o = this.getServer().getOfflinePlayer(username);
		InetAddress ip = this.playerToIp.get(username);
		if (ip == null) {
			banner.sendMessage("Username " + o.getName() + " has NO ip address");
			return;
		}
		banner.sendMessage("Username " + o.getName() + " has ip " + ip.getHostAddress());
		this.actOnIp(banner, ip, reason, time);

	}

	public void actOnPlayer(CommandSender banner, UUID username, String reason, long time) {
		final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
		final UUID uniqueId = offlinePlayer.getUniqueId();
		PlayerBanInfo info = new PlayerBanInfo(uniqueId, time, banner instanceof Player ? ((Player) banner).getUniqueId() : CONSOLE, reason);
		if (time != 0) {
			Command.broadcastCommandMessage(banner, "Banned player " + offlinePlayer.getName());
			this.playerBans.put(uniqueId, info);
			for (String s : this.banGameFormat.getFormat()
					.replace("{{name}}", offlinePlayer.getName())
					.replace("{{reason}}", reason)
					.replace("{{expires}}", TimeConverter.getMessage(info.getUntil() - System.currentTimeMillis(), 1))
					.split("\n")) {
				Bukkit.broadcastMessage(s);
			}
		} else {
			Command.broadcastCommandMessage(banner, "Kicked player " + offlinePlayer.getName());
			for (String s : this.kickGameFormat.getFormat()
					.replace("{{name}}", offlinePlayer.getName())
					.replace("{{reason}}", reason)
					.split("\n")) {
				Bukkit.broadcastMessage(s);
			}
		}
		String formatted = this.formatBanInfo(info);
		if (offlinePlayer.isOnline()) {
			((Player) offlinePlayer).kickPlayer(formatted);
		}
		this.scheduleSave();
	}

	@EventHandler
	public void onjoin(PlayerJoinEvent evt) {
		this.ipToPlayer.put(evt.getPlayer().getAddress().getAddress(), evt.getPlayer().getUniqueId());
		this.playerToIp.put(evt.getPlayer().getUniqueId(), evt.getPlayer().getAddress().getAddress());
	}

	@EventHandler
	public void onlogin(AsyncPlayerPreLoginEvent event) {
		BanInfo info = this.playerBans.get(event.getUniqueId());
		if (info != null) {
			if (info.getUntil() < System.currentTimeMillis()) {
				this.playerBans.remove(event.getUniqueId());
				this.scheduleSave();
			} else {
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, this.formatBanInfo(info));
				return;
			}
		}
		info = this.ipBans.get(event.getAddress());
		if (info != null) {
			if (info.getUntil() < System.currentTimeMillis()) {
				this.ipBans.remove(event.getAddress());
				this.scheduleSave();
			} else {
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, this.formatBanInfo(info));
			}
		}
	}

}
