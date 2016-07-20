package com.teradata.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import backtype.storm.tuple.Fields;


public class Topology {

    private static final Logger logger = LoggerFactory.getLogger(Topology.class);
    static final String TOPOLOGY_NAME = "twittersentiment";



    public static void main(String[] args) throws Exception {


        Config config = new Config();
        config.setMessageTimeoutSecs(120);
        //config.setNumWorkers(1);

        TopologyBuilder b = new TopologyBuilder();
        b.setSpout("TwitterSampleSpout", new TwitterSampleSpout());
        b.setBolt("TwitterFireHoseBolt", new TwitterFireHoseBolt()).shuffleGrouping("TwitterSampleSpout");
        b.setBolt("LocationBolt", new LocationBolt()).shuffleGrouping("TwitterFireHoseBolt");
        b.setBolt("SentimentBolt", new SentimentBolt()).shuffleGrouping("LocationBolt");
        //keep hourly stats
        //b.setBolt("SentimentCountBolt2", new SentimentCountBolt2(10, 60 * 60,5)).shuffleGrouping("SentimentBolt");
        b.setBolt("SentimentCountBolt2", new SentimentCountBolt2(10, 60 * 60) , 1).fieldsGrouping("SentimentBolt", new Fields("sentiment", "userLocation"));
        //b.setBolt("SentimentCountBolt2", new SentimentCountBolt2(10, 60 * 60,5)).globalGrouping("SentimentBolt");



         // use "|" instead of "," for field delimiter
         RecordFormat format = new DelimitedRecordFormat().withFieldDelimiter("|");

         // sync the filesystem after every 5 tuples
         SyncPolicy syncPolicy = new CountSyncPolicy(5);

         // rotate files when they reach 5MB
         FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f, FileSizeRotationPolicy.Units.MB);

         FileNameFormat fileNameFormat = new DefaultFileNameFormat().withPath("/user/storm/twitter3/");

         HdfsBolt bolt = new HdfsBolt()
         .withFsUrl("hdfs://sdlc7047.labs.teradata.com")
         .withFileNameFormat(fileNameFormat)
         .withRecordFormat(format)
         .withRotationPolicy(rotationPolicy)
         .withSyncPolicy(syncPolicy);


         b.setBolt("hdfsbolt", bolt,1).shuffleGrouping("SentimentCountBolt2");




         Map conf = new HashMap();
         conf.put(Config.TOPOLOGY_WORKERS, 4);

         try {
         StormSubmitter.submitTopology("twittersentiment", conf, b.createTopology());
         } catch (AlreadyAliveException e) {
         e.printStackTrace();
         } catch (InvalidTopologyException e) {
         e.printStackTrace();
         } catch (AuthorizationException e) {
         e.printStackTrace();
         }








        /**local

        final LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(TOPOLOGY_NAME, config, b.createTopology());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cluster.killTopology(TOPOLOGY_NAME);
                cluster.shutdown();
            }
        });

       **/

    }

}
