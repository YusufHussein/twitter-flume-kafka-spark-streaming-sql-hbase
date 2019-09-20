package CS523.FinalProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;

import twitter4j.DirectMessage;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
public class TwitterSource extends AbstractSource implements EventDrivenSource, Configurable {
	
	private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private String body=" ";
    private TwitterStream twitterStream;
    


	@Override
	public void start() {
	    
		System.out.println("write your Search Keywords comma sep");
		Scanner scanner = new Scanner(System.in);
		String Keywords = scanner.nextLine();
		scanner.close();
		
        final ChannelProcessor channel = getChannelProcessor();
		final Map<String, String> headers = new HashMap<String, String>();
	

        	twitterStream.addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                System.out.println("User @" + status.getUser().getScreenName());
                System.out.println("iD #" + status.getId());
                System.out.println("lang #" + status.getLang());
                System.out.println("text #" + status.getText());
                System.out.println("location #" + status.getGeoLocation());
                System.out.println("craeted Date #" + status.getCreatedAt());
                System.out.println("isFavorited #" + status.isFavorited());
                System.out.println("#" + status.getHashtagEntities());
                System.out.println("Source #" + status.getSource());
                System.out.println("getPlace #" + status.getPlace());
                headers.put("timestamp", String.valueOf(status.getCreatedAt().getTime()));
				body = status.getId() + "||"
						+ status.getUser().getScreenName() + "||" + status.getLang() + "||"
						+ status.getText() + "||" + status.getGeoLocation() + "||" +  status.getSource() + "||" + status.getPlace();
				// The EventBuilder is used to build an event using headers and body
				Event event = EventBuilder.withBody(body.getBytes(), headers);
				try {
					channel.processEvent(event);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        });

        ArrayList<Long> follow = new ArrayList<Long>();
        ArrayList<String> track = new ArrayList<String>();
        for (String arg : Keywords.split(",")) {
            if (isNumericalArgument(arg)) {
                for (String id : arg.split(",")) {
                    follow.add(Long.parseLong(id));
                }
            } else {
                track.addAll(Arrays.asList(arg.split(",")));
            }
        }
        long[] followArray = new long[follow.size()];
        for (int i = 0; i < follow.size(); i++) {
            followArray[i] = follow.get(i);
        }
        String[] trackArray = track.toArray(new String[track.size()]);

        // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
       // twitterStream.filter(new FilterQuery(0, followArray, trackArray));
        twitterStream.sample();
    }

    private static boolean isNumericalArgument(String argument) {
        String args[] = argument.split(",");
        boolean isNumericalArgument = true;
        for (String arg : args) {
            try {
                Integer.parseInt(arg);
            } catch (NumberFormatException nfe) {
                isNumericalArgument = false;
                break;
            }
        }
        return isNumericalArgument;
    }

	@Override
	public void configure(Context context) {
		// TODO Auto-generated method stub
	 	consumerKey = context.getString(TwitterSourceConstants.CONSUMER_KEY_KEY);
        consumerSecret = context.getString(TwitterSourceConstants.CONSUMER_SECRET_KEY);
        accessToken = context.getString(TwitterSourceConstants.ACCESS_TOKEN_KEY);
        accessTokenSecret = context.getString(TwitterSourceConstants.ACCESS_TOKEN_SECRET_KEY);    
 
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(accessToken);
        cb.setOAuthAccessTokenSecret(accessTokenSecret);
        cb.setJSONStoreEnabled(true);
        cb.setIncludeEntitiesEnabled(true);
        
       twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		
	}
}