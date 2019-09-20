package CS523.FinalProject;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;


public final class TwitterStream extends AbstractSource implements EventDrivenSource, Configurable {

	
	
	
    public static void main(String[] args) throws TwitterException {
        if (args.length < 1) {
            System.out.println("Usage: java twitter4j.examples.PrintFilterStream [follow(comma separated numerical user ids)] [track(comma separated filter terms)]");
            System.exit(-1);
        }
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(TwitterSourceConstants.CONSUMER_KEY_KEY);
        cb.setOAuthConsumerSecret(TwitterSourceConstants.CONSUMER_SECRET_KEY);
        cb.setOAuthAccessToken(TwitterSourceConstants.ACCESS_TOKEN_KEY);
        cb.setOAuthAccessTokenSecret(TwitterSourceConstants.ACCESS_TOKEN_SECRET_KEY);
        cb.setJSONStoreEnabled(true);
        cb.setIncludeEntitiesEnabled(true);
 
       
		

        twitter4j.TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance().addListener(new StatusListener() {
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
        for (String arg : args) {
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
        twitterStream.filter(new FilterQuery(0, followArray, trackArray));
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
		
	}
}