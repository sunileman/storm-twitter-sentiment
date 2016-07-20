package com.teradata.storm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.PendingResult;
import com.google.maps.model.*;

import static org.junit.Assert.*;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;


public class textprocessingDotCom {

    private static final Logger logger = LoggerFactory.getLogger(textprocessingDotCom.class);



    public static String getSentiment(String textToAnalyze) throws Exception{

        //logger.info(new StringBuilder("\ngetSentiment:textToAnalyze: ").append(textToAnalyze).toString());


        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost("http://text-processing.com/api/sentiment/");
        //httpPost.addHeader("User-Agent", USER_AGENT);


        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

        urlParameters.add(new BasicNameValuePair("text", textToAnalyze));
        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
        httpPost.setEntity(postParams);

        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        //System.out.println("POST Response Status:: "
               // + httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);

        }

        reader.close();

        // print result
        //System.out.println(response.toString());
        httpClient.close();

        JSONObject jsonObj = new JSONObject(response.toString());
        jsonObj.append("Text", textToAnalyze);
        //System.out.println(jsonObj.get("label"));


        String reValue = sentiment.valueOf(jsonObj.get("label").toString()).toString();

        //logger.info("enum - " + sentiment.valueOf(jsonObj.get("label").toString()));


        //System.out.println(sentiment.valueOf(jsonObj.get("label").toString()));

        //return jsonObj.append("Text", textToAnalyze);

        //logger.info(new StringBuilder("\nreturnSentiment: ").append(new JSONObject(response.toString()).get("label").toString()).toString());

        //return  new JSONObject(response.toString()).get("label").toString();

        return reValue;




    }



    public static void main(String[] args) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://text-processing.com/api/sentiment/");
        //httpPost.addHeader("User-Agent", USER_AGENT);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("text", "awesome"));

        HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
        httpPost.setEntity(postParams);

        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        //System.out.println("POST Response Status:: "
                //+ httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
            JSONObject jsonObj = new JSONObject(response.toString());
            //System.out.println(jsonObj.get("label"));


            //System.out.println(sentiment.valueOf(jsonObj.get("label").toString()));

        }
        reader.close();

        // print result
        //System.out.println(response.toString());
        httpClient.close();
    }

    public enum sentiment {

        pos ("Positive"),
        neg ("Negative"),
        neutral("Neutral");

        private final String name;

        private sentiment(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return (otherName == null) ? false : name.equals(otherName);
        }

        public String toString() {
            return this.name;
        }

    }
}