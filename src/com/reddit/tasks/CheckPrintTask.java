package com.reddit.tasks;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.reddit.CMPBot;
import com.reddit.entities.CMPTask;
import com.reddit.utils.Utilities;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.SubmissionKind;
import net.dean.jraw.references.SubmissionReference;
import net.dean.jraw.references.SubredditReference;

public class CheckPrintTask extends CMPTask {
	
	private long next_print_end;
	private boolean postedWarning;

	public CheckPrintTask(CMPBot bot) {
		super(bot);
		next_print_end = Utilities.currentTimeMillis() + (Utilities.random(60_000, 80_000));
	}

	@Override
	public void run() {
		SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		if(Utilities.timeRemaining(next_print_end) <= 60_000 && !postedWarning) {
			//bot.getSubredditRef().submit(SubmissionKind.SELF, "Vote for next print @ "+format.format(new Date(next_print_end)), "Test post. Markdown TODO", false);
			postedWarning = true;
			log("Posting thread as new print time is: "+format.format(new Date(next_print_end)));
		}
	}

	@Override
	public long getDelay() {
		return 5000;
	}

	@Override
	public String getName() {
		return "CheckPrintTask";
	}

}
