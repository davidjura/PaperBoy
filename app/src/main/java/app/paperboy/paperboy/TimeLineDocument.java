package app.paperboy.paperboy;

/**
 * Created by David Jura
 *
 * This class is intended to store information about a requested article
 */
public class TimeLineDocument {
    private String title;
    private String posted;
    private String image;
    private String url;
    private int site;

    public static final int BREITBART = 0;
    public static final int THE_BLAZE = 1;
    public static final int HUFFPOST = 2;
    public static final int SALON = 3;
    public static final int BBC = 4;
    public static final int ABC = 5;

    /**
     * The constructor for the TimeLineDocument
     *
     * @param title  The title of the article
     * @param posted The date posted
     * @param image  the image associated with the article (null is acceptable)
     * @param url    the url of the article
     * @param site   the site which the article originated from (for parsing purposes)
     */
    public TimeLineDocument(String title, String posted, String image, String url, int site) {
        this.title = title;
        this.site = site;
        this.posted = posted;
        this.image = image;
        this.url = url;
    }

    //Accessor methods below
    public int getSite() {
        return site;
    }

    public String getTitle() {
        return title;
    }

    public String getPosted() {
        return posted;
    }

    public String getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }
}
