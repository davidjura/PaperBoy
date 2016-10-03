package app.paperboy.paperboy;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by David Jura
 *
 * This class handles the Article Activity
 */
public class ArticleActivity extends AppCompatActivity {
    private String title;
    private String image;
    private String date;
    private String url;
    private int originator;
    int width;
    private boolean isFavorited;
    private UserData uData;
    private TextView txtBody;
    private ProgressBar progressLoading;
    private ImageHandler imageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        //retrieve article data extras from intent
        Intent me = getIntent();
        this.title = me.getExtras().getString("title");
        this.image = me.getExtras().getString("image");
        this.date = me.getExtras().getString("date");
        this.url = me.getExtras().getString("url");
        this.originator = me.getExtras().getInt("originator");

        this.uData = new UserData(this);

        //get image display width (taking into account the 16 dpi padding)
        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = (int) (((dm.widthPixels / dm.density) - 32) * (dm.densityDpi / 160));

        this.imageHandler = new ImageHandler(this);

        Typeface mainfont = Typeface.createFromAsset(getAssets(), "fonts/appfont.ttf");

        //set the new article source image based on the originator passed from extras
        ImageView btnArticleSource = (ImageView) findViewById(R.id.imgArticleSource);
        switch (originator) {
            case TimeLineDocument.BREITBART:
                btnArticleSource.setImageResource(R.drawable.breitbart);
                break;
            case TimeLineDocument.THE_BLAZE:
                btnArticleSource.setImageResource(R.drawable.blaze);
                break;
            case TimeLineDocument.HUFFPOST:
                btnArticleSource.setImageResource(R.drawable.huffpost);
                break;
            case TimeLineDocument.SALON:
                btnArticleSource.setImageResource(R.drawable.salon);
                break;
            case TimeLineDocument.BBC:
                btnArticleSource.setImageResource(R.drawable.bbc);
                break;
            case TimeLineDocument.ABC:
                btnArticleSource.setImageResource(R.drawable.abc);
                break;
            case TimeLineDocument.REUTERS:
                btnArticleSource.setImageResource(R.drawable.reuters);
                break;
        }

        /* Setup favorite button and set appropriate drawable resource (if the article has been
            favorited or not. )
         */
        final ImageView btnFavorite = (ImageView) findViewById(R.id.btnFavorite);
        if (uData.isFavorite(title)) {
            btnFavorite.setImageResource(R.drawable.star_clicked);
            isFavorited = true;
        } else {
            btnFavorite.setImageResource(R.drawable.star_unclicked);
            isFavorited = false;
        }

        //Setup loading circle and set the color to the app's primary color
        progressLoading = (ProgressBar) findViewById(R.id.progressBar);
        progressLoading.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(
                        this,
                        R.color.colorPrimary),
                        PorterDuff.Mode.SRC_IN
                );


        //Set a handler for the favorite button
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFavorited) {
                    uData.deleteFavorite(title);
                    btnFavorite.setImageResource(R.drawable.star_unclicked);
                    isFavorited = false;
                    Toast.makeText(getApplicationContext(), getString(R.string.unfavorite_msg)
                            , Toast.LENGTH_SHORT).show();
                } else {
                    uData.addFavorite(title, date, url, image, originator);
                    btnFavorite.setImageResource(R.drawable.star_clicked);
                    isFavorited = true;
                    Toast.makeText(getApplicationContext(), getString(R.string.favorite_msg)
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setTypeface(mainfont);
        txtTitle.setText(title);

        TextView txtDate = (TextView) findViewById(R.id.txtDate);
        txtDate.setTypeface(mainfont);
        txtDate.setText(date);

        txtBody = (TextView) findViewById(R.id.txtBody);
        txtBody.setTypeface(mainfont);

        /*Set a click listener for the browser button to send the user directly to the article
            using an Intent.
         */
        ImageView btnOpenBrowser = (ImageView) findViewById(R.id.btnOpenBrowser);
        btnOpenBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(openBrowser);
            }
        });

        //Setup the back button so the user can return to the timeline
        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //setup header image
        ImageView imgHead = (ImageView) findViewById(R.id.imgHead);
        if (!image.equals("")) {
            imgHead.getLayoutParams().width = width;
            //noinspection SuspiciousNameCombination
            imgHead.getLayoutParams().height = width;
            imageHandler.setImage(image, imgHead);
        } else {
            imgHead.getLayoutParams().width = 0;
            imgHead.getLayoutParams().height = 0;
        }

        //Download article body asynchronously
        articleBodyDownloader dl = new articleBodyDownloader();
        dl.execute();
    }

    /**
     * This class is intended to extract article paragraphs from a requested article using
     * the async task
     */
    private class articleBodyDownloader extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... strings) {
            String currText = "";
            switch (originator) {
                //Download and parse the Breitbart article
                case TimeLineDocument.BREITBART:
                    currText = downloadBreitbart();
                    break;
                //Download and parse The Blaze article
                case TimeLineDocument.THE_BLAZE:
                    currText = downloadBlaze();
                    break;
                //Download and parse the Huffington Post article
                case TimeLineDocument.HUFFPOST:
                    currText = downloadHuffPost();
                    break;
                //Download and parse the Salon article
                case TimeLineDocument.SALON:
                    currText = downloadSalon();
                    break;
                //Download and parse the ABC article
                case TimeLineDocument.ABC:
                    currText = downloadABC();
                    break;
                //Download and parse the BBC article
                case TimeLineDocument.BBC:
                    currText = downloadBBC();
                    break;
                    //Download and parse the Reuters article
                case TimeLineDocument.REUTERS:
                    currText = downloadReuters();
                    break;
            }
            return currText;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //When finished, set the progress bar to invisible and set the body text
            progressLoading.setVisibility(View.INVISIBLE);
            txtBody.setText(s);
        }
    }

    /**
     * Download the article body of the Breitbart article
     *
     * @return String of the article body
     */
    public String downloadBreitbart() {
        String currText = "";
        try {
            Document breitbartDownload = Jsoup.connect(url).get();
            Elements paragraphs = breitbartDownload.select("p");
            //Parse out paragraphs
            for (Element p : paragraphs) {
                if (p.text().toUpperCase().contains("BY "))
                    continue;
                if (p.text().toUpperCase().contains("COMMENT COUNT"))
                    break;
                currText += p.text().toUpperCase();
                currText += "\n\n";
            }
            if (currText.equals(""))
                currText = getString(R.string.empty_article);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currText;
    }

    /**
     * Download the article body of The Blaze article
     *
     * @return String of the article body
     */
    public String downloadBlaze() {
        String currText = "";
        try {
            Document blazeDownload = Jsoup.connect(url).get();
            Elements paragraphs = blazeDownload.select("p");
            //Counter used to parse out dash character without effecting main article
            int counter = 0;
            for (Element p : paragraphs) {
                if (p.text().toUpperCase().contains("—") && counter > 4)
                    break;
                counter++;
                currText += p.text().toUpperCase();
                currText += "\n\n";
            }
            if (currText.equals(""))
                currText = getString(R.string.empty_article);
        } catch (IOException e) {
            e.printStackTrace();

        }
        return currText;
    }

    /**
     * Download the article body of the Huffington Post article
     *
     * @return String of the article body
     */
    public String downloadHuffPost() {
        String currText = "";
        try {
            Document huffDownload = Jsoup.connect(url).get();

            Elements paragraphs = huffDownload.select("p");

            //Parse out paragraphs
            for (Element item : paragraphs) {
                if (item.text().toUpperCase().contains("COPYRIGHT"))
                    continue;
                currText += item.text().toUpperCase();
                currText += "\n\n";
            }
            if (currText.equals(""))
                currText = getString(R.string.empty_article);
        } catch (IOException e) {
            e.printStackTrace();

        }
        return currText;
    }

    /**
     * Download the article body of the Salon article
     *
     * @return String of the article body
     */
    public String downloadSalon() {
        String currText = "";
        try {
            Document salonDownload = Jsoup.connect(url).get();
            Elements paragraphs = salonDownload.select("p");

            //skipIndex counter used to skip first paragraph of of article (contains garbage text)
            int skipIndex = 0;
            for (Element item : paragraphs) {
                if (skipIndex < 1) {
                    skipIndex++;
                    continue;
                }
                if (item.text().toUpperCase().contains("TOPICS:"))
                    continue;
                if (item.text().toUpperCase().contains("LOADING COMMENTS"))
                    break;
                if (item.text().toUpperCase().contains("BACK TO THEBLAZE"))
                    break;
                currText += item.text().toUpperCase();
                currText += "\n\n";
            }
            if (currText.equals(""))
                currText = getString(R.string.empty_article);
        } catch (IOException e) {
            e.printStackTrace();

        }
        return currText;
    }

    /**
     * Download the article body of the ABC article
     *
     * @return String of the article body
     */
    public String downloadABC() {
        String currText = "";
        try {
            Document abcDownload = Jsoup.connect(url).get();

            Elements paragraphs = abcDownload.select("p");
            //Parse out paragraphs
            for (Element item : paragraphs) {
                if(item.text().equals(""))
                    continue;
                currText += item.text().toUpperCase();
                currText += "\n\n";
            }
            if (currText.equals(""))
                currText = getString(R.string.empty_article);
        } catch (IOException e) {
            e.printStackTrace();

        }
        return currText;
    }

    /**
     * Download the article body of the BBC article
     *
     * @return String of the article body
     */
    public String downloadBBC() {
        String currText = "";
        try {
            Document bbcDownload = Jsoup.connect(url).get();

            Elements paragraphs = bbcDownload.select("p");
            //Parse out paragraphs
            for (Element item : paragraphs) {
                //The if statements below clean out the garbage text in the article
                if(item.text().equals(""))
                    continue;
                if (item.text().toUpperCase().contains("SHARE THIS WITH") ||
                        item.text().toUpperCase().contains("COPY THIS LINK")) {
                    currText = "";
                    continue;
                }
                if (item.text().toUpperCase().contains("RUN BY THE BBC AND PARTNERS"))
                    break;
                if (item.text().toUpperCase().
                        contains("CAN YOU BE POLITICALLY ACTIVE IF"))
                    break;
                if (item.text().toUpperCase().contains("LAST UPDATED AT"))
                    continue;
                currText += item.text().toUpperCase();
                currText += "\n\n";
            }
            if (currText.equals(""))
                currText = getString(R.string.empty_article);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currText;
    }

    /**
     * Downloads the article body of the Reuters article
     *
     * @return String of the article body
     */
    private String downloadReuters(){
        String currText = "";
        try{
            Document reutersDownload = Jsoup.connect(url).get();
            Elements paragraphs = reutersDownload.select("p");
            for(Element item:paragraphs){
                //condition to check for garbage data to break out of parsing
                if(item.text().toUpperCase().contains("REUTERS IS THE NEWS AND MEDIA DIVISION"))
                    break;
                currText += item.text().toUpperCase();
                currText += "\n\n";
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return currText;
    }
}
