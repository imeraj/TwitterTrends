package com.example.meraj.twittertrends;

/**
 * Created by meraj on 26/02/2018.
 */

public class Globals {
    public static final String CONSUMER_KEY = "8wKM8JZ3BQYr5YW82ynQG4tX6";
    public static final String CONSUMER_SECRET = "CH6bByZSpFOtDUpYY51R1mW4X7ESMguHa943oC5FgeiCgYFStq";

    public static final String WOEID_URL = "http://query.yahooapis.com/v1/public/yql?format=json&q=select woeid from geo.places where text=";
    public static final String TWITTER_AUTH_API = "https://api.twitter.com/oauth2/token";
    public static final String TWITTER_TRENDS_API = "https://api.twitter.com/1.1/trends/place.json?id=";
}
