package net.walnut.tumblr_migrator;

import static java.lang.System.out;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.apis.TumblrApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;


public class Main {

	static String request_url = "https://www.tumblr.com/oauth/request_token";
	static String access_url = "https://www.tumblr.com/oauth/access_token";
	static String auth_url = "https://www.tumblr.com/oauth/authorize";
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException
	{
		Scanner in = new Scanner(System.in);
		out.print("Please copy and paste the \"OAuth Consumer Key\" here: ");
		String ckey = in.nextLine();
		out.print("Please copy and paste the \"Secret Key\" here: ");
		String csecret = in.nextLine();
		out.println("I will now generate two URLs for you to follow.");
		out.println("Please follow the first one while logged in to the account you wish to transfer information FROM.");
		out.println();
		OAuth10aService service = new ServiceBuilder(ckey).apiSecret(csecret).callback("http://localhost/").build(TumblrApi.instance());
		OAuth1RequestToken requestToken = service.getRequestToken();
		out.println(service.getAuthorizationUrl(requestToken));
		out.println();
		out.print("Click 'Allow', and then paste the text after 'oauth_verifier' in your URL bar: ");
		String verifier = in.nextLine();
		OAuth1AccessToken accessToken = service.getAccessToken(requestToken, verifier);
		String token1 = accessToken.getToken();
		String token1s = accessToken.getTokenSecret();
		out.println("All done - I've got the access token and the token secret I need for the first account. Onto the second one!");
		out.println();
		out.println("Please follow this URL while logged into the account you wish to transfer information TO.");
		out.println();
		requestToken = service.getRequestToken();
		out.println(service.getAuthorizationUrl(requestToken));
		out.println();
		out.print("Click 'Allow', and then paste the text after 'oauth_verifier' in your URL bar: ");
		verifier = in.nextLine();
		accessToken = service.getAccessToken(requestToken, verifier);
		String token2 = accessToken.getToken();
		String token2s = accessToken.getTokenSecret();
		out.print("All done - I've got access to both accounts. Would you like to transfer your likes now? (y/n) ");
		char a = in.nextLine().toLowerCase().charAt(0);
		while(a == 'y')
		{
			out.print("Please enter i(Likes) (enter -1 if you don't know): ");
			int i = in.nextInt();
			in.nextLine();
			if(new Likes(ckey, csecret, token1, token1s, token2, token2s).doTheThing(i) != 0)
			{
				out.print("Looks like something went wrong. Do you want to try again? (y/n) ");
				a = in.nextLine().toLowerCase().charAt(0);
				continue;
			}
			a = 'n';
		}
		out.print("Do you want to transfer the blogs you're following now? (y/n) ");
		a = in.nextLine().toLowerCase().charAt(0);
		while(a == 'y')
		{
			out.print("Please enter i(Blogs) (enter -1 if you don't know): ");
			int i = in.nextInt();
			in.nextLine();
			if(new Follows(ckey, csecret, token1, token1s, token2, token2s).doTheThing(i) != 0)
			{
				out.print("Looks like something went wrong. Do you want to try again? (y/n) ");
				a = in.nextLine().toLowerCase().charAt(0);
				continue;
			}
			a = 'n';
		}
		in.close();
		out.println("Looks like we're all done here! Thanks for using me :D");
		out.println("Press Enter to close.");
		System.in.read();
	}
}
