package com.teradata.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PendingResult;
import com.google.maps.model.GeocodingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


public class LocationBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(LocationBolt.class);


    private OutputCollector collector;

    //private static GeoApiContext context;


    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {


        this.collector = collector;


    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tweet", "userLocation"));
    }


    @Override
    public void execute(Tuple input) {
        final String tweet = (String) input.getValueByField("tweet");
        final String userLoc = (String) input.getValueByField("userLocation");

        //System.out.println(tweet +":" +userLoc);

        GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyCzAowvB_fET0xemJhw12uzfXj9rPR2MmQ");

        final List<GeocodingResult[]> resps = new ArrayList<GeocodingResult[]>();

        PendingResult.Callback<GeocodingResult[]> callback =
                new PendingResult.Callback<GeocodingResult[]>() {
                    @Override
                    public void onResult(GeocodingResult[] result) {
                        //resps.add(result);
                        collector.emit(new Values(tweet, result[0].formattedAddress));
                        logger.info("tweet:"+ tweet +" address: " + result[0].formattedAddress);
                        //System.out.println(result[0].formattedAddress);
                        //System.out.println(tweet);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        //
                    }
                };


        GeocodingApi.newRequest(context).address(userLoc).setCallback(callback);

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //assertFalse(resps.isEmpty());
        //assertNotNull(resps.get(0));
        //checkSydneyResult(resps.get(0));
        //GeocodingResult[] results = resps.get(0);
        //collector.emit(new Values(textprocessingDotCom.getSentiment(tweet)));
        //System.out.println(results[0].formattedAddress);


        try {
            //String sentiment = textprocessingDotCom.getSentiment(tweet);
            //System.out.println(sentiment);
            //System.out.println(results[0].formattedAddress);



            //logger.info(new StringBuilder("\nSentiment: ").append(sentiment).toString());


            //collector.emit(new Values(textprocessingDotCom.getSentiment(tweet)));


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
