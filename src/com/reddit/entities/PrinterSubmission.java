package com.reddit.entities;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrinterSubmission {
	
	private int id;
	
	private String postId, title, author, suggestion;
	
	private Timestamp timestamp;
	
	private boolean active;
	
	public Object[] data() {
		return new Object[]{ "DEFAULT", postId, title, author, suggestion, "DEFAULT", active ? 1 : 0 };
	}

}
