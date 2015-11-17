/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

/**
 *
 * @author Fernando
 */
public class PlayerBanInfo extends BanInfo{
	private final UUID id;

	public PlayerBanInfo(UUID id, long until, UUID banner, String reason) {
		super(until, banner, reason);
		this.id = id;
	}

	public UUID getId() {
		return id;
	}
	
	
	
}
