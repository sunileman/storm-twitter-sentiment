package com.teradata.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Fields;

import backtype.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;

import static org.junit.Assert.assertNotNull;


public class SentimentBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(SentimentBolt.class);


    private OutputCollector collector;


    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {


        this.collector = collector;


    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("sentiment", "userLocation"));
    }

    @Override
    public void execute(Tuple input) {
        String tweet = (String) input.getValueByField("tweet");
        String userLoc = (String) input.getValueByField("userLocation");

            try {
                String sentiment = textprocessingDotCom.getSentiment(tweet);



                logger.info(new StringBuilder("\nSentiment: ").append(sentiment).append(" from: ").append(userLoc).toString());


                collector.emit(new Values(sentiment, userLoc));


            } catch (Exception e) {
                e.printStackTrace();
            }



    }
}
