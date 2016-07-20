package com.teradata.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;


import junit.framework.Assert;
import twitter4j.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import java.util.Map;


public class TwitterFireHoseBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(TwitterFireHoseBolt.class);


    private OutputCollector collector;



    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        Status tweet = (Status) input.getValueByField("tweet");

        String userLocation = tweet.getUser().getLocation();



        //logger.info(new StringBuilder("Tweet - ").append(userLocation).toString());



        String text = tweet.getText().replaceAll("\\p{Punct}", " ").toLowerCase();




        /**
        JSONArray jsonArray = new JSONArray(tweet.toString());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            logger.info(new StringBuilder("Tweet - ").append(jsonObject.getString("text")).toString());
            //System.out.println(jsonObject.getString("id"));
            //System.out.println(jsonObject.getString("text"));
            //System.out.println(jsonObject.getString("created_at"));
        }
         **/

        //JSONObject jsonObject = new JSONObject(tweet.toString());
        //logger.info(new StringBuilder("Tweet - ").append(jsonObject.getString("text")).toString());


        //logger.info(new StringBuilder("Tweet - ").append(tweet.getText().replaceAll("\\p{Punct}", " ").toLowerCase()).toString());

        //String[] words = text.split(" ");

        //logger.info(new StringBuilder("Tweet - ").append(text).toString());

        if(userLocation != null){collector.emit(new Values(text,userLocation.toLowerCase()));}



    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tweet", "userLocation"));
    }
}
