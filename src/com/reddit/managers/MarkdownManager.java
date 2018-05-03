package com.reddit.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Stream;

import com.google.gson.internal.Streams;

import lombok.Getter;

public class MarkdownManager {
	
	private @Getter HashMap<String, String> markdown;
	
	public void init() {
		markdown = new HashMap<>();
		File dir = new File("./data/markdown/");
		try {
			for(File file : dir.listFiles()) {
				if(file.isDirectory() || !file.getName().contains(".md"))
					continue;
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = null;
				StringBuilder builder = new StringBuilder();
				while((line = reader.readLine()) != null) {
					builder.append(line);
				}
				reader.close();
				markdown.put(file.getName().toLowerCase().replace(".md", ""), builder.toString());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getMarkdown(String file, String...values) {
		if(!markdown.containsKey(file)) return null;
		String reply = markdown.get(file);
		reply = reply.replaceAll("--", "%0A").replaceAll("\\+", "%2B");
		int index = 0;
		while(index < values.length) {
			String key = values[index++];
			String value = values[index++];
			reply = reply.replace(key, value);
		}
		return reply;
	}

}
