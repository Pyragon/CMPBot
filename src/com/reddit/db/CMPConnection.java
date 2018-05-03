package com.reddit.db;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.reddit.CMPBot;
import com.reddit.entities.PrinterSubmission;
import com.reddit.entities.SQLQuery;

public class CMPConnection extends DatabaseConnection {

	public CMPConnection() {
		super("cmp_global");
		gson = CMPBot.instance().getGson();
	}
	
	private Gson gson;

	@Override
	public Object[] handleRequest(Object... data) {
		String opcode = (String) data[0];
		switch(opcode) {
		case "get-viewed-posts":
			return select("data", "name=?", GET_VIEWED_POSTS, "viewed_posts");
		case "save-viewed-posts":
			delete("data", "name=?", "viewed_posts");
			String list = gson.toJson((ArrayList<String>) data[1]);
			insert("data", "viewed_posts", list);
			break;
		case "add-submission":
			PrinterSubmission submission = (PrinterSubmission) data[1];
			insert("suggestions", submission.data());
			break;
		case "get-submission-by-url":
			return select("suggestions", "url LIKE ?", GET_SUBMISSION, (String) data[1]);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public SQLQuery GET_VIEWED_POSTS = (set) -> {
		if(empty(set)) return null;
		String list = getString(set, "value");
		ArrayList<String> viewed = gson.fromJson(list, ArrayList.class);
		return new Object[] { viewed };
	};
	
	public SQLQuery GET_SUBMISSION = (set) -> {
		if(empty(set)) return null;
		int id = getInt(set, "id");
		String postId = getString(set, "post_id");
		String title = getString(set, "title");
		String author = getString(set, "author");
		String suggestion = getString(set, "url");
		Timestamp stamp = getTimestamp(set, "date");
		int active = getInt(set, "active");
		return new Object[] { new PrinterSubmission(id, postId, title, author, suggestion, stamp, active == 1 ? true : false) };
	};

}
