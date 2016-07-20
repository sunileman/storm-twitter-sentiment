/**
 * Taken from the storm-starter project on GitHub
 * https://github.com/nathanmarz/storm-starter/ 
 */
package com.teradata.storm;

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Spout me some Sample feed from twitter.
 */

@SuppressWarnings({ "rawtypes", "serial" })
public class TwitterSampleSpout extends BaseRichSpout {

	private SpoutOutputCollector collector;
    private LinkedBlockingQueue<Status> queue;
    private TwitterStream twitterStream;

	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		queue = new LinkedBlockingQueue<Status>(1000);
		this.collector = collector;

		StatusListener listener = new StatusListener() {
			@Override
			public void onStatus(Status status) {
				//add to linked list
				queue.offer(status);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice sdn) {
			}

			@Override
			public void onTrackLimitationNotice(int i) {
			}

			@Override
			public void onScrubGeo(long l, long l1) {
			}

            @Override
            public void onStallWarning(StallWarning stallWarning) {
            }

            @Override
			public void onException(Exception e) {
			}
		};

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("66swyxxx")
				.setOAuthConsumerSecret("5PmIMOVIiAth4Kxxx")
				.setOAuthAccessToken("181830716-Zxxx")
				.setOAuthAccessTokenSecret("PHBcWo6DeCqwzuEO5x");


        TwitterStreamFactory factory = new TwitterStreamFactory(cb.build());
		twitterStream = factory.getInstance();
		twitterStream.addListener(listener);
		twitterStream.sample("en");
	}

	@Override
	public void nextTuple() {
		//get head of list
		Status ret = queue.poll();
		if (ret == null) {
			Utils.sleep(50);
        } else {
			collector.emit(new Values(ret));
		}
	}

	@Override
	public void close() {
		twitterStream.shutdown();
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

	@Override
	public void ack(Object id) {
	}

	@Override
	public void fail(Object id) {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("tweet"));
	}

}
