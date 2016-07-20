package com.teradata.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import backtype.storm.tuple.Values;
import backtype.storm.tuple.Fields;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


public class SentimentCountBolt extends BaseRichBolt {
    private static final Logger logger = LoggerFactory.getLogger(SentimentCountBolt.class);
    /** Number of seconds before the top list will be logged to stdout. */
    private final long logIntervalSec;
    /** Number of seconds before the top list will be cleared. */
    private final long clearIntervalSec;


    private Map<String, Long> counter;
    private long lastLogTime;
    private long lastClearTime;


    private OutputCollector collector;

    private TreeMap<String, Long> tmap;

    public SentimentCountBolt(long logIntervalSec, long clearIntervalSec) {
        this.logIntervalSec = logIntervalSec;
        this.clearIntervalSec = clearIntervalSec;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        counter = new HashMap<String, Long>();
        lastLogTime = System.currentTimeMillis();
        lastClearTime = System.currentTimeMillis();

        TreeMap<String, Long> tmap = new TreeMap<String, Long>();

        this.collector = collector;

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        //declarer.declare(new Fields("sentiment", "sentimentCount", "timeOfSentiment"));
        declarer.declare(new Fields("sentiment"));
    }

    @Override
    public void execute(Tuple input) {
        String sentiment = (String) input.getValueByField("sentiment");


        
        Long count = counter.get(sentiment);
        count = count == null ? 1L : count + 1;
        counter.put(sentiment, count);

//        logger.info(new StringBuilder(word).append('>').append(count).toString());

        long now = System.currentTimeMillis();
        long logPeriodSec = (now - lastLogTime) / 1000;
        if (logPeriodSec > logIntervalSec) {
            logger.info("Calculating Sentiment ");

            publishSentiment();
            lastLogTime = now;
        }
    }

    private void publishSentiment() {
        // calculate top list:


        SortedMap<Long, String> top = new TreeMap<Long, String>();
        for (Map.Entry<String, Long> entry : counter.entrySet()) {
            //long count = entry.getValue();
            //String word = entry.getKey();
            //collector.emit(new Values(entry.getValue(), entry.getKey(), lastLogTime));
            collector.emit(new Values(new StringBuilder(entry.getValue().toString()).append("|").append(entry.getKey().toString()).append("|").append(lastLogTime).toString()));

            logger.info(new StringBuilder("Sentiment - ").append(entry.getValue()).append('=').append(entry.getKey()).toString());

        }


            //logger.info(new StringBuilder("top - ").append(entry.getValue()).append('>').append(entry.getKey()).toString());




        // Clear top list
        long now = System.currentTimeMillis();
        if (now - lastClearTime > clearIntervalSec * 1000) {
            counter.clear();
            lastClearTime = now;
        }
    }
}
