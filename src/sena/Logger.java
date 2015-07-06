package sena;

import ij.IJ;


public abstract class Logger {
	private String name;
	public Logger(String name) {
		this.name = name;
	}
	
	public void log (String message) {
		IJ.log("["+Config.PLUGIN_NAME+" - "+name+"] "+message);
	}

}