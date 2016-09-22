package app.paperboy.paperboy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

/**
 * Created by David Jura
 *
 * This class pulls and sets image data for a requested article and utilized LruCache to cache
 * bitmaps in memory.
 */
public class ImageHandler {
    private RequestQueue requestQueue;
    private LruCache<String, Bitmap> cache;
    private Context context;
    private ImageByteStreamRequest imgByteReq;
    //Retrieve the device's max memory
    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    //Get 1/8 of the device's current memory (to be used for LRU caching)
    private final int cacheSize = maxMemory / 8;
    private int width;

    /**
     * ImageHandler constructor
     *
     * @param context The activity context reference
     */
    public ImageHandler(Context context) {
        this.context = context;
        //Get default image width based on device width (taking into account 16 dpi padding)
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        width = (int) (((dm.widthPixels / dm.density) - 32) * (dm.densityDpi / 160));
        requestQueue = Volley.newRequestQueue(context);

        //Setup the LruCache instance using 1/8 of the devices current memory capacity
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    /**
     * Set's the ImageView bitmap from downloaded byte data and stores it in Lrucache
     *
     * @param url   The url of the image
     * @param image The destination ImageView reference
     */
    public void setImage(final String url, final ImageView image) {
        if (cache.get("url") == null) {
            imgByteReq = new ImageByteStreamRequest(url, new Response.Listener<byte[]>() {
                @Override
                public void onResponse(byte[] response) {
                    Bitmap data = BitmapFactory.decodeByteArray(response, 0, response.length);
                    int width = data.getWidth();
                    int height = data.getHeight();
                    float ratio = (float) width / height;
                    Bitmap finalBitmap = Bitmap.createScaledBitmap(
                            data, width, (int) ((float) width / ratio), true
                    );
                    cache.put(url, finalBitmap);
                    image.setImageBitmap(finalBitmap);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Error downloading, do nothing
                }
            });
            requestQueue.add(imgByteReq);
        } else {
            //Image is already cached, load the bitmap from the cache.
            image.setImageBitmap(cache.get("url"));
        }
    }


    /**
     * Volley ImageRequest is deprecated. Wrote my own request to download image byte data
     */
    private class ImageByteStreamRequest extends Request<byte[]> {
        private Response.Listener<byte[]> mListener;

        /**
         * Constructor for the ImageByeStreamRequest
         *
         * @param url              The requesting image url
         * @param responseListener The callback listener interface to handle the response
         * @param errorListener    The error listener interface to handle error responses
         */
        public ImageByteStreamRequest(String url, Response.Listener<byte[]> responseListener,
                                      Response.ErrorListener errorListener) {
            super(Method.GET, url, errorListener);
            mListener = responseListener;
        }

        /**
         * Subclasses must implement this to parse the raw network response
         * and return an appropriate response type. This method will be
         * called from a worker thread.
         *
         * @param response Response from the network
         * @return The parsed response, or null in the case of an error
         */
        @Override
        protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
            return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
        }

        /**
         * Subclasses must implement this to perform delivery of the parsed
         * response to their listeners.  The given response is guaranteed to
         * be non-null; responses that fail to parse are not delivered.
         *
         * @param response The parsed response returned by
         *                 {@link #parseNetworkResponse(NetworkResponse)}
         */
        @Override
        protected void deliverResponse(byte[] response) {
            mListener.onResponse(response);
        }
    }
}
