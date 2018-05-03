package com.reddit.entities;

import java.util.TimerTask;

import com.reddit.CMPBot;
import com.reddit.utils.Logger;

public abstract class CMPTask extends TimerTask {
	
	protected CMPBot bot;
	
	public abstract String getName();
	
	public abstract long getDelay();
	
	public CMPTask(CMPBot bot) {
		this.bot = bot;
	}
	
	protected void log(String message) {
		Logger.log(getName(), message);
	}

}
