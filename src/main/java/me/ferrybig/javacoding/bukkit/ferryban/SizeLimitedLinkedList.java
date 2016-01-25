/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ferrybig.javacoding.bukkit.ferryban;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Fernando
 */
public class SizeLimitedLinkedList<K, V> extends LinkedHashMap<K, V> {
	
	private final int size;

	public SizeLimitedLinkedList(int size) {
		this.size = size;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > size;
	}
	
}
