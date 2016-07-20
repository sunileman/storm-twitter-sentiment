package com.teradata.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;


public class SentimentCountBolt2 extends BaseRichBolt {
    private static final Logger logger = LoggerFactory.getLogger(SentimentCountBolt2.class);
    /** Number of seconds before the top list will be logged to stdout. */
    private final long logIntervalSec;
    /** Number of seconds before the top list will be cleared. */
    private final long clearIntervalSec;


    public static boolean ASC = true;
    public static boolean DESC = false;

    //private Map<String, Long> counter;

    private Map<String, Long> posCounter;
    private Map<String, Long> negCounter;
    private Map<String, Long> neuCounter;

    Map<String, Long> sortedposCounter;
    Map<String, Long> sortednegCounter;
    Map<String, Long> sortedneuCounter;

    private long lastLogTime;
    private long lastClearTime;

    private final static String constantS   = "-SunileIsAwesome-";


    private OutputCollector collector;



    private TreeMap<String, Long> tmap;

    public SentimentCountBolt2(long logIntervalSec, long clearIntervalSec) {
        this.logIntervalSec = logIntervalSec;
        this.clearIntervalSec = clearIntervalSec;

    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        //counter = new HashMap<String, Long>();
        posCounter = new HashMap<String, Long>();
        negCounter = new HashMap<String, Long>();
        neuCounter = new HashMap<String, Long>();
        sortedposCounter= new HashMap<String, Long>();
        sortednegCounter= new HashMap<String, Long>();
        sortedneuCounter= new HashMap<String, Long>();


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
        String userLoc = (String) input.getValueByField("userLocation");


        //logger.info("Sentiment:" + sentiment + "  userLocation: " + userLoc);



        String sentiByLoc = userLoc + constantS +sentiment;

        //logger.info("sentiByLoc:"+sentiByLoc);

        //logger.info(sentiment);

        if(sentiment.equals("Positive")) {
            //logger.info("If Postive Sentiment:" + sentiment + "  userLocation: " + userLoc);
            Long count = posCounter.get(sentiByLoc);
            count = count == null ? 1L : count + 1;
            posCounter.put(sentiByLoc, count);

            sortedposCounter = sortByComparator(posCounter, DESC);
        }
        else if(sentiment.equals("Negative")) {
            //logger.info("If Negative Sentiment:" + sentiment + "  userLocation: " + userLoc);
            Long count = negCounter.get(sentiByLoc);
            count = count == null ? 1L : count + 1;
            negCounter.put(sentiByLoc, count);
            sortednegCounter = sortByComparator(negCounter, DESC);
        }
        else if(sentiment.equals("Neutral")) {
            //logger.info("If Neutral Sentiment:" + sentiment + "  userLocation: " + userLoc);
            Long count = neuCounter.get(sentiByLoc);
            count = count == null ? 1L : count + 1;
            neuCounter.put(sentiByLoc, count);
            sortedneuCounter = sortByComparator(neuCounter, DESC);
        }




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


            if(!sortedposCounter.isEmpty()) {
                logger.info("************POSITIVE SENTIMENT START****************");

                for (Map.Entry<String, Long> entry : sortedposCounter.entrySet()) {


                    collector.emit(new Values(new StringBuilder(entry.getKey().split(Pattern.quote(constantS))[0]).append("|").append(entry.getKey().split(Pattern.quote(constantS))[1]).append("|").append(entry.getValue().toString()).append("|").append(lastLogTime).toString()));


                    logger.info(new StringBuilder("Sentiment - ").append(entry.getKey().split(Pattern.quote(constantS))[0]).append(" : ").append(entry.getKey().split(Pattern.quote(constantS))[1]).append('=').append(entry.getValue()).toString());



                }
                logger.info("************POSITIVE SENTIMENT END******************");
            }


        if(!sortednegCounter.isEmpty()) {

            logger.info("************NEGATIVE SENTIMENT START******************");
            for (Map.Entry<String, Long> entry : sortednegCounter.entrySet()) {

                collector.emit(new Values(new StringBuilder(entry.getKey().split(Pattern.quote(constantS))[0]).append("|").append(entry.getKey().split(Pattern.quote(constantS))[1]).append("|").append(entry.getValue().toString()).append("|").append(lastLogTime).toString()));



                logger.info(new StringBuilder("Sentiment - ").append(entry.getKey().split(Pattern.quote(constantS))[0]).append(" : ").append(entry.getKey().split(Pattern.quote(constantS))[1]).append('=').append(entry.getValue()).toString());


            }

            logger.info("************NEGATIVE SENTIMENT END******************");
        }



        if(!sortedneuCounter.isEmpty()) {

            logger.info("************NEUTRAL SENTIMENT START******************");
            for (Map.Entry<String, Long> entry : sortedneuCounter.entrySet()) {

                collector.emit(new Values(new StringBuilder(entry.getKey().split(Pattern.quote(constantS))[0]).append("|").append(entry.getKey().split(Pattern.quote(constantS))[1]).append("|").append(entry.getValue().toString()).append("|").append(lastLogTime).toString()));


                logger.info(new StringBuilder("Sentiment - ").append(entry.getKey().split(Pattern.quote(constantS))[0]).append(" : ").append(entry.getKey().split(Pattern.quote(constantS))[1]).append('=').append(entry.getValue()).toString());

            }

            logger.info("************NEUTRAL SENTIMENT END******************");

        }

        // Clear counters after time has hit

         long now = System.currentTimeMillis();
         if (now - lastClearTime > clearIntervalSec * 1000) {
         posCounter.clear();
             sortedposCounter.clear();
         neuCounter.clear();
             neuCounter.clear();
         negCounter.clear();
             negCounter.clear();
         lastClearTime = now;

         }
    }



    private static Map<String, Long> sortByComparator(Map<String, Long> unsortMap, final boolean order)
    {

        List<Map.Entry<String, Long>> list = new LinkedList<Map.Entry<String, Long>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String,Long>>()
        {
            public int compare(Map.Entry<String, Long> o1,
                               Map.Entry<String, Long> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
