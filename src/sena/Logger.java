package sena;

import ij.IJ;


public abstract class Logger {
	private String name;
	public Logger() {
		this.name = getClass().getName();	
		
	}
	
	public void log (String message) {
		IJ.log("["+Config.PLUGIN_NAME+" - "+name+"] "+message);
	}

}
