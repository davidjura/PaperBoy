package app.paperboy.paperboy;

import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David Jura
 *
 * This class is intended to load the feed for user's homepage.
 */
public class FeedLoader {
    private Context mContext;
    private UserData data;
    private FeedLoadListener feedLoadListener;

    /**
     * Feedloader constructor
     *
     * @param context the application context
     */
    public FeedLoader(Context context) {
        this.mContext = context;
        this.data = new UserData(context);
    }

    /**
     * Sets the response listener for a feed request
     *
     * @param feedLoadListener FeedLoadListener instance
     */
    public void setFeedLoadListener(FeedLoadListener feedLoadListener) {
        this.feedLoadListener = feedLoadListener;
    }

    /**
     * Initiate an AsyncTask internal class to begin pulling and parsing data
     */
    public void fetchFeed() {
        new asyncLoader().execute();
    }

    /**
     * Interface used as a callback to retrieve data from a completed feed request
     */
    public interface FeedLoadListener {
        void onResult(ArrayList<TimeLineDocument> data);
    }

    /**
     * This internal class is used to run an AsyncTask request to pull and parse data from user's
     * news sources
     */
    private class asyncLoader extends AsyncTask<Void, Void, ArrayList<TimeLineDocument>> {
        @Override
        protected ArrayList<TimeLineDocument> doInBackground(Void... voids) {
            ArrayList<TimeLineDocument> timeline = new ArrayList<>();
            switch (data.getPreference()) {
                //GET CONSERVATIVE NEWS POSTS
                case UserData.CONSERVATIVE:
                    try {
                        //Download the article data using Jsoup
                        Document breitbart = Jsoup.connect(
                                mContext.getResources().getString(R.string.breitbart_api))
                                .parser(Parser.xmlParser()).get();

                        Document blaze = Jsoup.connect(
                                mContext.getResources().getString(R.string.the_blaze_api))
                                .parser(Parser.xmlParser()).get();

                        //Iterate through The Blaze data and append to timeline array
                        for (Element item : blaze.select("item")) {
                            Pattern pattern = Pattern.compile(
                                    "http://www.theblaze.com/wp-content/uploads/[^\"]*"
                            );
                            Matcher m = pattern.matcher(item.text());
                            String image = "";
                            if (m.find())
                                image = m.group();

                            TimeLineDocument temp = new TimeLineDocument(item.select("title")
                                    .text(),
                                    item.select("pubDate").text()
                                            .substring(0, item.select("pubDate").text()
                                                    .length() - 15),
                                    image,
                                    item.select("link").text(),
                                    TimeLineDocument.THE_BLAZE
                            );
                            timeline.add(temp);
                        }

                        //Iterate through the Breitbart data and append to timeline list
                        for (Element item : breitbart.select("item")) {
                            TimeLineDocument temp = new TimeLineDocument(
                                    item.select("title").text(),
                                    item.getElementsByIndexEquals(4).text()
                                            .substring(0, item.getElementsByIndexEquals(4).text().
                                                    length() - 15),
                                    item.select("enclosure").first().attr("url"),
                                    item.getElementsByIndexEquals(7).text(),
                                    TimeLineDocument.BREITBART
                            );
                            timeline.add(temp);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Shuffle the article count so the user gets a fresh feed each refresh
                    Collections.shuffle(timeline, new Random(System.nanoTime()));
                    //Each list will be a maximum of 20 articles long
                    ArrayList<TimeLineDocument> finalList = new ArrayList<>();
                    try {
                        for (int i = 0; i < 20; i++) {
                            finalList.add(timeline.get(i));
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }

                    //Return the new ArrayList of 20 articles
                    return finalList;

                //GET LIBERAL NEWS POSTS
                case UserData.LIBERAL:
                    try {
                        //Download Huffington Post data
                        Document huffpost = Jsoup.connect(
                                mContext.getString(R.string.huffington_post_api))
                                .parser(Parser.xmlParser()).get();

                        //Download the Salon data
                        Document salon = Jsoup.connect(
                                mContext.getString(R.string.salon_api))
                                .parser(Parser.xmlParser()).get();

                        //Iterate through the Huffington Post data and append to timeline list
                        for (Element item : huffpost.select("item")) {
                            String thumbnail = item.select("enclosure").first().attr("url");
                            /*Since the Huffington Post feed contains only a thumbnail, get the
                                image name and use the url below to get the full size image
                             */
                            String imgUrl =
                                    "http://img.huffingtonpost.com/asset/scalefit_630_noupscale/"
                                            + thumbnail.substring(42, thumbnail.length());

                            TimeLineDocument temp = new TimeLineDocument(
                                    item.select("title").text(),
                                    item.select("pubDate").text()
                                            .substring(0, item.select("pubDate")
                                                    .text().length() - 15),
                                    imgUrl,
                                    item.select("link").text(),
                                    TimeLineDocument.HUFFPOST

                            );
                            timeline.add(temp);
                        }

                        //Iterate through Salon data and append to timeline list
                        for (Element item : salon.select("channel").select("item")) {
                            //This Regex pattern is used to parse out the article image url
                            Pattern pattern = Pattern.compile(
                                    "http://media.salon.com/[^\"]*"
                            );
                            Matcher m = pattern.matcher(item.text());
                            String image = "";
                            if (m.find())
                                image = m.group();

                            TimeLineDocument temp = new TimeLineDocument(
                                    item.select("title").text(),
                                    item.select("pubDate").text()
                                            .substring(0, item.select("pubDate")
                                                    .text().length() - 15),
                                    image,
                                    item.select("link").text(),
                                    TimeLineDocument.SALON
                            );
                            timeline.add(temp);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //shuffle the article count so the user gets a fresh feed each refresh
                    Collections.shuffle(timeline, new Random(System.nanoTime()));

                    return timeline;

                //GET MODERATE NEWS POSTS
                case UserData.MODERATE:
                    try {
                        //download BBC data
                        Document bbc = Jsoup.connect(mContext.getString(R.string.bbc_api))
                                .parser(Parser.xmlParser()).get();

                        //download ABC data
                        Document abc = Jsoup.connect(mContext.getString(R.string.abc_api))
                                .parser(Parser.xmlParser()).get();

                        //Iterate through BBC data and append to timeline list
                        for (Element item : bbc.select("channel").select("item")) {
                            //Discard intro article (Because BBC included it in their feed)
                            if (item.select("title").text().toUpperCase()
                                    .equals("BBC NEWS CHANNEL") ||
                                    item.select("title").text().toUpperCase()
                                            .equals("BBC BREAKFAST") ||
                                    item.select("title").text().toUpperCase().contains("WATCH:"))
                                continue;
                            TimeLineDocument temp = new TimeLineDocument(
                                    item.select("title").text(),
                                    item.select("pubDate").text()
                                            .substring(0, item.select("pubDate")
                                                    .text().length() - 13),
                                    item.select("media|thumbnail").first().attr("url"),
                                    item.select("link").text(),
                                    TimeLineDocument.BBC
                            );
                            timeline.add(temp);
                        }

                        //Iterate through ABC data and append to timeline list
                        for (Element item : abc.select("channel").select("item")) {

                            TimeLineDocument temp = new TimeLineDocument(
                                    item.select("title").text(),
                                    item.select("pubDate").text()
                                            .substring(0, item.select("pubDate")
                                                    .text().length() - 15),
                                    item.select("media|thumbnail").get(4).attr("url"),
                                    item.select("link").text(),
                                    TimeLineDocument.ABC
                            );
                            timeline.add(temp);
                        }

                        //Shuffle the timeline list
                        Collections.shuffle(timeline, new Random(System.nanoTime()));

                        //Append and return a list of the first 20 articles
                        ArrayList<TimeLineDocument> finalList2 = new ArrayList<>();
                        try {
                            for (int i = 0; i < 20; i++) {
                                finalList2.add(timeline.get(i));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }

                        return finalList2;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<TimeLineDocument> s) {
            super.onPostExecute(s);
            //pass the data to the FeedLoadListener interface callback if not null
            if (s == null)
                feedLoadListener.onResult(new ArrayList<TimeLineDocument>());
            else
                feedLoadListener.onResult(s);
        }
    }
}
