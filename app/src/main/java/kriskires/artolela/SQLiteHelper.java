package kriskires.artolela;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import kriskires.artolela.Data.PictureContract;

import static android.content.ContentValues.TAG;
import static android.net.Uri.decode;

/* Helper for Database */

class SQLiteHelper extends SQLiteOpenHelper {
    private static String DATABASE_NAME = "PicturesDataBase";
    private static final int DATABASE_VERSION = 16; // Increase by one when changing the database
    private final Context mContext;
    private Picture[] pictures;

    SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i("SQLite", "create");

        // String for creating table Pictures
        String SQL_CREATE_PICTURES_TABLE = "CREATE TABLE " + PictureContract.Picture.TABLE_NAME + " ("
                + PictureContract.Picture.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PictureContract.Picture.COLUMN_LABEL_RU + " TEXT NOT NULL, "
                + PictureContract.Picture.COLUMN_LABEL_EN + " TEXT NOT NULL, "
                + PictureContract.Picture.COLUMN_LABEL_IT + " TEXT NOT NULL, "
                + PictureContract.Picture.COLUMN_IMAGE_LINK + " TEXT NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_PICTURES_TABLE);

        jsonFileToObjects();
        ContentValues picturesValues = new ContentValues();

        for (int i = 0; i < pictures.length; i++) {
            picturesValues.put(PictureContract.Picture.COLUMN_LABEL_RU, pictures[i].getLabel_ru());
            picturesValues.put(PictureContract.Picture.COLUMN_LABEL_EN, pictures[i].getLabel_en());
            picturesValues.put(PictureContract.Picture.COLUMN_LABEL_IT, pictures[i].getLabel_it());
            picturesValues.put(PictureContract.Picture.COLUMN_IMAGE_LINK, pictures[i].getImage());
            sqLiteDatabase.insert(PictureContract.Picture.TABLE_NAME, null, picturesValues);
        }

        // String for creating table Images
        String SQL_CREATE_IMAGES_TABLE = "CREATE TABLE " + PictureContract.Image.TABLE_NAME + " ("
                + PictureContract.Image.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PictureContract.Image.COLUMN_URL + " TEXT NOT NULL, "
                + PictureContract.Image.COLUMN_FILENAME + " TEXT NOT NULL,"
                + " FOREIGN KEY(" + PictureContract.Image.COLUMN_URL + ") REFERENCES " + PictureContract.Picture.TABLE_NAME + "(" + PictureContract.Picture.COLUMN_IMAGE_LINK + "));";
        sqLiteDatabase.execSQL(SQL_CREATE_IMAGES_TABLE);

        ContentValues imagesValues = new ContentValues();
        Resources res = mContext.getResources();

        XmlResourceParser _xml = res.getXml(R.xml.image_links);
        try {
            // Looking for the end of the document
            int eventType = _xml.getEventType();
            String url = null;
            String filename = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Looking for URL tags
                if ((eventType == XmlPullParser.START_TAG) && (_xml.getName().equals("URL"))) {
                    // The URL tag was found, now we get it value and put it into the table
                    url = _xml.nextText();
                    imagesValues.put("url", url);
                }
                if ((eventType == XmlPullParser.START_TAG) && (_xml.getName().equals("FileName"))) {
                    try {
                        filename = _xml.nextText().substring(19);///23
                    }catch (Exception e){
                    }
                    imagesValues.put("filename", filename);
                }
                if ((url != null) && (filename != null)) {
                    sqLiteDatabase.insert(PictureContract.Image.TABLE_NAME, null, imagesValues);
                    url = null;
                    filename = null;
                }
                eventType = _xml.next();
            }
        } catch (XmlPullParserException e) {
            Log.e("Error", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("Error", e.getMessage(), e);
        } finally {
            _xml.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w("SQLite", "Update from version " + oldVersion + " to version " + newVersion);

        // Delete old tables Pictures and Images
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PictureContract.Picture.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PictureContract.Image.TABLE_NAME);

        // Create new tables
        onCreate(sqLiteDatabase);
    }

    // Convert from a file into objects
    private void jsonFileToObjects() {
        StringBuilder data = new StringBuilder("");
        AssetManager am = mContext.getAssets();
        String filename = "list_of_pictures.json";
        try {
            InputStream fis = am.open(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String readString = br.readLine();
            while (readString != null) {
                data.append(readString);
                readString = br.readLine();
            }
            isr.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        String jsonData = data.toString();
        Gson json = new Gson();
        pictures = json.fromJson(jsonData, Picture[].class);
    }
}
