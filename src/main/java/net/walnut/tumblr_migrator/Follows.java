package net.walnut.tumblr_migrator;

import static java.lang.System.out;

import java.io.IOException;
import java.io.PrintWriter;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
import com.tumblr.jumblr.types.Blog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Follows {

	private String ck, cs, t1, t1s, t2, t2s;

	public Follows(String ckey, String csecret, String token1, String token1s, String token2, String token2s) {

		ck = ckey;
		cs = csecret;
		t1 = token1;
		t1s = token1s;
		t2 = token2;
		t2s = token2s;
	}

	public int doTheThing() {
		return doTheThing(Integer.MAX_VALUE);
	}

	public int doTheThing(int startat) {
		JumblrClient originalTumblr = new JumblrClient(ck, cs, t1, t1s);
		JumblrClient newTumblr = new JumblrClient(ck, cs, t2, t2s);
		int numBlogs = originalTumblr.user().getFollowingCount();
		List<Blog> blogs = new ArrayList<Blog>();
		HashMap<String, Object> opt = new HashMap<String, Object>();
		opt.put("offset", 0);
		for (; (int) opt.get("offset") < numBlogs; opt.put("offset", (int) opt.get("offset") + 20)) {
			try {
				blogs.addAll(originalTumblr.userFollowing(opt));
			} catch (JumblrException e) {
				if (e.getResponseCode() == 429)
					out.println(
							"This API key has exceeded the rate limit - it's either gone over 1000 requests/hr, or 5000/day, or both. Wait an hour or use a different key.");
				else {
					String ct = Long.toString(System.currentTimeMillis() / 1000L) + ".txt";
					try {
						PrintWriter dump = new PrintWriter(ct, "UTF-8");
						dump.println(
								"Follow Migration encountered error " + e.getResponseCode() + ": " + e.getMessage());
						dump.close();
					} catch (IOException e1) {
					}
					out.println("I've encountered an error. I've saved relevant information to " + ct
							+ ", please open it and copy the contents into a new issue at https://github.com/WalnutBunny/TumblrMigrator/issues .");
				}
				return 1;
			}
		}
		out.println("Finished gathering followed blogs");
		int o = 0, x;
		if (startat != -1) {
			try {
				blogs.get(startat);
				x = startat;
			} catch (IndexOutOfBoundsException e) {
				out.println("Invalid i(Blogs) - ignoring.");
				x = blogs.size() - 1;
			}
		} else
			x = blogs.size() - 1;
		for (; x >= 0; x--, o++) {
			Blog b = blogs.get(x);
			try {
				newTumblr.follow(b.getName());
			} catch (JumblrException e) {
				if (e.getResponseCode() == 429) {
					out.println(
							"This API key has exceeded the rate limit - it's either gone over 1000 requests/hr, 5000/day, or both. Wait an hour or use a different key. Input this when asked for i(Blogs): "
									+ x);
					return 1;
				} else if (e.getResponseCode() == 404) {
					out.println("You were following a blog that doesn't exist anymore - ignoring it.");
					continue;
				} else {
					String ct = Long.toString(System.currentTimeMillis() / 1000L) + ".txt";
					out.println("Something happened, but I'm not sure what it was - I've saved relevant information to "
							+ ct
							+ ", please open it and copy the contents into a new issue on https://github.com/WalnutBunny/TumblrMigrator/issues .");
					try {
						PrintWriter dump = new PrintWriter(ct, "UTF-8");
						dump.println(
								"Follow Migration encountered error " + e.getResponseCode() + ": " + e.getMessage());
						dump.println("While processing Blog " + b.getName());
						dump.println();
						dump.println(e.getStackTrace());
						dump.close();
					} catch (IOException e1) {
					}
					out.println("I'm going to try to continue");
					o--;
					continue;
				}
			}
			if (o == 199) {
				out.println(
						"You've reached the daily follow cap - Tumblr only allows you to follow 200 blogs a day. Enter this when prompted for i(Blogs) tomorrow: "
								+ x);
				return 1;
			}
		}
		return 0;
	}
}
