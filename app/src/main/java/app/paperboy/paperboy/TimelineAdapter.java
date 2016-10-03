package app.paperboy.paperboy;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by David Jura
 *
 * This class extends an ArrayAdapter of type TimeLineDocument to setup article data for each row
 */
public class TimelineAdapter extends ArrayAdapter<TimeLineDocument> {
    private ImageHandler imgHandler;
    private int width;
    private Context mContext;

    /**
     * Default constructor for the TimelineAdapter
     *
     * @param context The activity context
     * @param data    The ArrayList of article data
     */
    public TimelineAdapter(Context context, ArrayList<TimeLineDocument> data) {
        super(context, 0, data);
        //Get default image width based on device width (taking into account 16 dpi padding)
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        width = (int) (((dm.widthPixels / dm.density) - 32) * (dm.densityDpi / 160));
        imgHandler = new ImageHandler(context);
        this.mContext = context;
    }

    /**
     * This class acts as a holder for all the view elements to by used by the getView() method
     */
    private class ViewHolder {
        TextView txtTitle;
        TextView txtInfo;
        ImageView imgArticle;
        ImageView imgPublisher;
    }

    /**
     * This override method sets up the views for the visible rows.
     *
     * @param position    The adapter's current position
     * @param convertView The view object to handle View elements from the requested position
     * @param parent      The parent ViewGroup
     * @return The set up view for the adapter's current position
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        Typeface mainFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/appfont.ttf");

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.article_layout, parent, false);
            holder = new ViewHolder();
            holder.imgArticle = (ImageView) convertView.findViewById(R.id.imgArticle);
            holder.imgArticle.getLayoutParams().width = width;
            //noinspection SuspiciousNameCombination
            holder.imgArticle.getLayoutParams().height = width;
            holder.imgArticle.setImageBitmap(null);
            holder.imgPublisher = (ImageView) convertView.findViewById(R.id.imgPublisher);
            holder.imgPublisher.setImageBitmap(null);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
            holder.txtInfo = (TextView) convertView.findViewById(R.id.txtInfo);
            holder.txtTitle.setTypeface(mainFont);
            holder.txtInfo.setTypeface(mainFont);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.imgArticle.setImageBitmap(null);
            holder.imgArticle.getLayoutParams().width = width;
            //noinspection SuspiciousNameCombination
            holder.imgArticle.getLayoutParams().height = holder.imgArticle.getLayoutParams().width;
            holder.imgPublisher.setImageBitmap(null);
        }

        //Setup the row click listener to transition to the article
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Pass the article data through the Intent extras.
                Intent viewArticle =
                        new Intent(mContext.getApplicationContext(), ArticleActivity.class);
                viewArticle.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                viewArticle.putExtra("title", getItem(position).getTitle().toUpperCase());
                viewArticle.putExtra("image", getItem(position).getImage());
                viewArticle.putExtra("date", getItem(position).getPosted().toUpperCase());
                viewArticle.putExtra("url", getItem(position).getUrl());
                viewArticle.putExtra("originator", getItem(position).getSite());
                //Start the Article activity
                mContext.getApplicationContext().startActivity(viewArticle);
            }
        });

        //Set article image vector based on the news provider
        String imgUrl = getItem(position).getImage();
        switch (getItem(position).getSite()) {
            case TimeLineDocument.BREITBART:
                holder.imgPublisher.setImageResource(R.drawable.breitbart);
                break;
            case TimeLineDocument.THE_BLAZE:
                holder.imgPublisher.setImageResource(R.drawable.blaze);
                break;
            case TimeLineDocument.HUFFPOST:
                holder.imgPublisher.setImageResource(R.drawable.huffpost);
                break;
            case TimeLineDocument.SALON:
                holder.imgPublisher.setImageResource(R.drawable.salon);
                break;
            case TimeLineDocument.BBC:
                holder.imgPublisher.setImageResource(R.drawable.bbc);
                break;
            case TimeLineDocument.ABC:
                holder.imgPublisher.setImageResource(R.drawable.abc);
                break;
            case TimeLineDocument.REUTERS:
                holder.imgPublisher.setImageResource(R.drawable.reuters);
                break;
        }

        holder.imgArticle.setImageBitmap(null);

        //If there is no image, set the width and height of the article image to 0
        if (!imgUrl.equals(""))
            imgHandler.setImage(getItem(position).getImage(), holder.imgArticle);
        else {
            holder.imgArticle.getLayoutParams().height = 0;
            holder.imgArticle.getLayoutParams().width = 0;
        }

        //Setup the title and posted date
        holder.txtTitle.setText(getItem(position).getTitle().toUpperCase());
        holder.txtInfo.setText(getItem(position).getPosted().toUpperCase());

        return convertView;
    }
}
