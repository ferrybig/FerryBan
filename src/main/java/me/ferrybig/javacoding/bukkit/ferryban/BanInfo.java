/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban;

import java.util.UUID;

/**
 *
 * @author Fernando
 */
public abstract class BanInfo {
	private final long until;
	private final UUID banner;
	private final String reason;

	public BanInfo(long until, UUID banner, String reason) {
		this.until = until;
		this.banner = banner;
		this.reason = reason;
	}

	public long getUntil() {
		return until;
	}

	public UUID getBanner() {
		return banner;
	}

	public String getReason() {
		return reason;
	}
	
}
