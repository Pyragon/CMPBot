package com.reddit;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;

import com.google.gson.Gson;
import com.reddit.db.CMPConnection;
import com.reddit.entities.CMPTask;
import com.reddit.managers.MarkdownManager;
import com.reddit.utils.Logger;
import com.reddit.utils.Utilities;

import lombok.Cleanup;
import lombok.Getter;
import net.dean.jraw.Endpoint;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Account;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.references.SubmissionReference;
import net.dean.jraw.references.SubredditReference;

public class CMPBot {
	
	private @Getter RedditClient client;
	
	private @Getter Gson gson;
	
	private @Getter Properties properties;
	
	private Timer timer;
	
	private @Getter SubredditReference subredditRef;
	
	private @Getter CMPConnection connection;
	
	private @Getter MarkdownManager markdownManager;
	
	private static CMPBot INSTANCE;
	
	private CMPBot() {
		
	}
	
	private void startBot() {
		Logger.log(this.getClass(), "Starting...");
		gson = new Gson();
		loadProperties();
		Credentials creds = Credentials.script("controlmyprinter", properties.getProperty("red_pass"), properties.getProperty("client_id"), properties.getProperty("client_secret"));
		UserAgent agent = new UserAgent("bot", "com.cryo.cmpbot","1.0.0","controlmyprinter");
		client = OAuthHelper.automatic(new OkHttpNetworkAdapter(agent), creds);
		subredditRef = client.subreddit("controlmyprinter");
		connection = new CMPConnection();
		markdownManager = new MarkdownManager();
		markdownManager.init();
		startTasks();
	}
	
	public void lockSubmission(Submission submission) {
		getClient().request(r -> {
			return r.endpoint(Endpoint.POST_LOCK).post(new HashMap<String, String>() {{
				put("id", submission.getFullName());
			}});
		});
	}
	
	private void startTasks() {
		timer = new Timer();
		try {
			for(Class<?> c : Utilities.getClasses("com.reddit.tasks")) {
				if(c.isAnonymousClass()) continue;
				Object o = c.getDeclaredConstructor(CMPBot.class).newInstance(this);
				if(!(o instanceof CMPTask)) continue;
				timer.schedule((CMPTask) o, 0, ((CMPTask) o).getDelay());
			}
		} catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadProperties() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("props.json"));
			String line = reader.readLine();
			if(line == null) {
				reader.close();
				throw new RuntimeException("No properties found!");
			}
			properties = gson.fromJson(line, Properties.class);
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static CMPBot instance() {
		return INSTANCE;
	}
	
	public static void main(String[] args) {
		INSTANCE = new CMPBot();
		INSTANCE.startBot();
	}

}
