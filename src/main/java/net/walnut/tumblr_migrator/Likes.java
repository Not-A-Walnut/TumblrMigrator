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
		out.println("Finished gathering likes");
		int x;
		if (startat != -1) {
			try {
				likes.get(startat);
				x = startat;
			} catch (IndexOutOfBoundsException e) {
				out.println("Invalid i(Likes) - ignoring.");
				x = likes.size() - 1;
			}

		} else
			x = likes.size() - 1;
		for (; x >= 0; x--) {
			Post p = likes.get(x);
			try {
				newTumblr.like(p.getId(), p.getReblogKey());
			} catch (JumblrException e) {
				if (e.getResponseCode() == 429) {
					out.println(
							"This API key has exceeded the rate limit - it's either gone over 1000 requests/hr, 5000/day, or both. Wait an hour or use a different key, and enter this number when it asks you for i(Likes): "
									+ x);
					return 1;
				} else {
					String ct = Long.toString(System.currentTimeMillis() / 1000L) + ".txt";
					out.println("Something happened, but I'm not sure what it was - I've saved relevant information to "
							+ ct
							+ ", please open it and copy the contents into a new issue on https://github.com/WalnutBunny/TumblrMigrator/issues .");
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
