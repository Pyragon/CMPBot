package com.reddit.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.reddit.CMPBot;
import com.reddit.db.CMPConnection;
import com.reddit.entities.CMPTask;
import com.reddit.entities.PrinterSubmission;
import com.reddit.managers.MarkdownManager;

import net.dean.jraw.Endpoint;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.references.SubmissionReference;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

public class CheckSubmissionTask extends CMPTask {
	
	private ArrayList<String> viewed_posts;
	
	private CMPConnection connection;

	public CheckSubmissionTask(CMPBot bot) {
		super(bot);
		connection = CMPBot.instance().getConnection();
		Object[] data = connection.handleRequest("get-viewed-posts");
		if(data == null) {
			viewed_posts = new ArrayList<String>();
			return;
		}
		viewed_posts = (ArrayList<String>) data[0];
	}

	@Override
	public String getName() {
		return "CheckSubmissionTask";
	}

	@Override
	public long getDelay() {
		return 180_000;
	}

	@Override
	public void run() {
		DefaultPaginator<Submission> paginator = bot.getSubredditRef().posts().limit(50).sorting(SubredditSort.NEW).timePeriod(TimePeriod.ALL).build();
		Listing<Submission> newest = paginator.next();
		for(Submission submission : newest) {
			String title = submission.getTitle();
			if(!submission.isSelfPost() || !title.toLowerCase().contains("[request]") || viewed_posts.contains(submission.getId())) 
				continue;
			viewed_posts.add(submission.getId());
			String[] lines = submission.getSelfText().split("\n");
			String url = lines[0];
			SubmissionReference reference = submission.toReference(bot.getClient());
			if(!url.startsWith("http://thingiverse.com/thing:") && !url.startsWith("https://thingiverse.com/thing:") && !url.startsWith("https://www.thingiverse.com/thing:") && !url.startsWith("http://www.thingiverse.com/thing:")) {
				reference.reply(CMPBot.instance().getMarkdownManager().getMarkdown("invalid"));
				bot.lockSubmission(submission);
				continue;
			}
			Object[] data = connection.handleRequest("get-submission-by-url", url);
			if(data != null) {
				PrinterSubmission printerSubmission = (PrinterSubmission) data[0];
				reference.reply(CMPBot.instance().getMarkdownManager().getMarkdown("toolate", "[toolate_link]", "http://reddit.com/r/controlmyprinter/comments/"+printerSubmission.getPostId(), "[toolate_user]", printerSubmission.getAuthor()));
				continue;
			}
			PrinterSubmission printSubmission = new PrinterSubmission(-1, submission.getId(), submission.getTitle(), submission.getAuthor(), url, null, false);
			connection.handleRequest("add-submission", printSubmission);
			reference.reply(CMPBot.instance().getMarkdownManager().getMarkdown("valid"));
		}
		connection.handleRequest("save-viewed-posts", viewed_posts);
	}

}
