package app.paperboy.paperboy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by David Jura
 *
 * This class is intended to utilize a local SQLite database to store user's preferences and
 * favorites
 */
public class UserData extends SQLiteOpenHelper {
    private Context mContext;

    public static final int CONSERVATIVE = 1;
    public static final int LIBERAL = 0;
    public static final int MODERATE = 2;

    private static final String DATABASE_NAME = "paperboy.db";
    private final String PREFERENCE_TABLE_NAME = "preference";
    private final String PREFERENCE_COLUMN_ID = "id";
    private final String PREFERENCE_COLUMN_SELECTION = "selection";

    private final String FAVORITES_TABLE_NAME = "favorites";
    private final String FAVORITES_COLUMN_ID = "id";
    private final String FAVORITES_COLUMN_DATE = "date";
    private final String FAVORITES_COLUMN_SAVED = "saved";
    private final String FAVORITES_COLUMN_TITLE = "title";
    private final String FAVORITES_COLUMN_URL = "url";
    private final String FAVORITES_COLUMN_IMAGE = "image";
    private final String FAVORITES_COLUMN_ORIGIN = "origin";

    public UserData(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    /**
     * Override method to create the sqlite database
     *
     * @param sqLiteDatabase the SQLiteDatabase instance
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE " + PREFERENCE_TABLE_NAME
                        + "(" + PREFERENCE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PREFERENCE_COLUMN_SELECTION + " INTEGER );"
        );

        sqLiteDatabase.execSQL(
                "CREATE TABLE " + FAVORITES_TABLE_NAME
                        + " (" + FAVORITES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + FAVORITES_COLUMN_SAVED + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                        + FAVORITES_COLUMN_TITLE + " TEXT, "
                        + FAVORITES_COLUMN_DATE + " TEXT, "
                        + FAVORITES_COLUMN_IMAGE + " TEXT, "
                        + FAVORITES_COLUMN_URL + " TEXT, "
                        + FAVORITES_COLUMN_ORIGIN + " INTEGER, "
                        + "UNIQUE(" + FAVORITES_COLUMN_TITLE + ", " + FAVORITES_COLUMN_URL
                        + ") ON CONFLICT REPLACE);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //no upgrades yet
    }

    /**
     * Get the user preference (conservative or liberal) from sqlite database
     *
     * @return the int determining the user preference
     */
    public int getPreference() {
        SQLiteDatabase currdb = this.getReadableDatabase();
        Cursor cursor = currdb.rawQuery(
                "SELECT " + PREFERENCE_COLUMN_SELECTION
                        + " FROM " + PREFERENCE_TABLE_NAME + ";", null
        );

        if (cursor.moveToFirst()) {
            int data = cursor.getInt(0);
            cursor.close();
            currdb.close();
            return data;
        } else
            return -1;
    }

    /**
     * updates the user preference in the sqlite database.
     *
     * @param preference the integer of the user's preference (1 - conservative, 0 - liberal)
     */
    public void setPreference(int preference) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + PREFERENCE_TABLE_NAME + ";");
        db.execSQL(
                "INSERT INTO " + PREFERENCE_TABLE_NAME + "("
                        + PREFERENCE_COLUMN_SELECTION
                        + ") VALUES( "
                        + preference + ");"
        );
        db.close();
    }

    /**
     * Add an article to the favorites table in the local sqlite database
     *
     * @param title  The title of the article
     * @param date   The date the article was posted
     * @param url    The url of the article
     * @param image  The image url of the article
     * @param origin The integer determining the originating news site
     */
    public void addFavorite(String title, String date, String url, String image, int origin) {
        SQLiteDatabase db = this.getWritableDatabase();
        title = title.replaceAll("'", "");
        db.execSQL(
                "INSERT INTO " + FAVORITES_TABLE_NAME + " ("
                        + FAVORITES_COLUMN_TITLE + ", "
                        + FAVORITES_COLUMN_DATE + ", "
                        + FAVORITES_COLUMN_URL + ", "
                        + FAVORITES_COLUMN_IMAGE + ", "
                        + FAVORITES_COLUMN_ORIGIN + ") "
                        + "VALUES ('"
                        + title + "', '"
                        + date + "', '"
                        + url + "', '"
                        + image + "', "
                        + origin + ");"

        );
        db.close();
    }

    /**
     * Gets an ArrayList of the user's favorite articles
     *
     * @return ArrayList of users favorite articles
     */
    public ArrayList<TimeLineDocument> getFavoritesList() {
        ArrayList<TimeLineDocument> favoritesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + FAVORITES_TABLE_NAME
                        + " ORDER BY " + FAVORITES_COLUMN_SAVED
                        + " DESC;"
                , null
        );
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                favoritesList.add(new TimeLineDocument(
                        cursor.getString(cursor.getColumnIndex(FAVORITES_COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(FAVORITES_COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndex(FAVORITES_COLUMN_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(FAVORITES_COLUMN_URL)),
                        cursor.getInt(cursor.getColumnIndex(FAVORITES_COLUMN_ORIGIN))));
                cursor.moveToNext();
            }
            db.close();
            cursor.close();
        } else {
            return favoritesList;
        }
        return favoritesList;
    }

    /**
     * Check to see if a a specified article is saved into favorites
     *
     * @param title The title of the article
     * @return true if the specified record exists, false if not
     */
    public boolean isFavorite(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        title = title.replaceAll("'", "");
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + FAVORITES_TABLE_NAME
                        + " WHERE " + FAVORITES_COLUMN_TITLE + "='" + title + "';"
                , null);
        boolean result = cursor.moveToFirst();
        db.close();
        cursor.close();
        return result;
    }

    /**
     * Deletes a favorite from the favorites table in the local SQLite database
     *
     * @param title the article title the user wishes to remove from their favorites
     */
    public void deleteFavorite(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        title = title.replaceAll("'", "");
        db.execSQL(
                "DELETE FROM " + FAVORITES_TABLE_NAME
                        + " WHERE " + FAVORITES_COLUMN_TITLE + "='" + title + "';"
        );
        db.close();
    }
}
