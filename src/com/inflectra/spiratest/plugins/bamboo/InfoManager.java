package com.inflectra.spiratest.plugins.bamboo;

public class InfoManager {

	private static String revision = null;
	
	
	public void storeRevision(String rev) {
		revision = rev;
	}

	public static String getRevision() {
		return revision;
	}	
}
