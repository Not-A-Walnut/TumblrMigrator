package net.walnut.tumblr_migrator;

import static java.lang.System.out;

import java.io.IOException;
import java.io.PrintWriter;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
import com.tumblr.jumblr.types.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Likes {

	private String ck, cs, t1, t1s, t2, t2s;

	public Likes(String ckey, String csecret, String token1, String token1s, String token2, String token2s) {

		ck = ckey;
		cs = csecret;
		t1 = token1;
		t1s = token1s;
		t2 = token2;
		t2s = token2s;
	}

	public int doTheThing(int startat) {
		JumblrClient originalTumblr = new JumblrClient(ck, cs, t1, t1s);
		JumblrClient newTumblr = new JumblrClient(ck, cs, t2, t2s);
		int numLikes = originalTumblr.user().getLikeCount();
		List<Post> likes = new ArrayList<Post>();
		HashMap<String, Object> opt = new HashMap<String, Object>();
		opt.put("offset", 0);
		for (; (int) opt.get("offset") < numLikes; opt.put("offset", (int) opt.get("offset") + 20)) {
			try {
				likes.addAll(originalTumblr.userLikes(opt));
			} catch (JumblrException e) {
				if (e.getResponseCode() == 429)
					out.println(
							"This API key has exceeded the rate limit - it's either gone over 1000 requests/hr, 5000/day, or both. Wait an hour or use a different key.");
				else
					out.println(
							"Not sure what went wrong here. Stack trace is as follows (send this to Walnut#2445 on Discord): "
									+ e.getStackTrace());
				return 1;
			}
		}
		out.println("Finished gathering likes");
		for (int i = likes.size(); i >= 0; i--) {
			Post p = likes.get(i);
			try {
				newTumblr.like(p.getId(), p.getReblogKey());
			} catch (JumblrException e) {
				if (e.getResponseCode() == 429) {
					out.println(
							"This API key has exceeded the rate limit - it's either gone over 1000 requests/hr, 5000/day, or both. Wait an hour or use a different key, and enter this number when it asks you for i(Likes): "
									+ i);
					return 1;
				} else {
					String ct = Long.toString(System.currentTimeMillis() / 1000L);
					out.println(
							"Something happened, but I'm not sure what it was - I've saved a file in your current directory titled "
									+ ct + ", please send it to Walnut#2445 on Discord.");
					try {
						PrintWriter dump = new PrintWriter(Long.toString(System.currentTimeMillis() / 1000L), "UTF-8");
						dump.println(
								"Likes Migration encountered error " + e.getResponseCode() + ": " + e.getMessage());
						dump.println("While processing Post " + p.getPostUrl());
						dump.println();
						dump.println(e.getStackTrace());
						dump.close();
					} catch (IOException e1) {
					}
					out.println("I'm going to try to continue");
				}
			}
		}
		return 0;
	}
}
