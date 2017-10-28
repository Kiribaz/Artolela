package kriskires.artolela.Data;

import android.provider.BaseColumns;

/* Structure of Database */

public final class PictureContract {
    private PictureContract() {

    }

    public static final class Picture implements BaseColumns {
        public final static String TABLE_NAME = "pictures";

        public final static String ID = BaseColumns._ID;
        public final static String COLUMN_LABEL_RU = "label_ru";
        public final static String COLUMN_LABEL_EN = "label_en";
        public final static String COLUMN_LABEL_IT = "label_it";
        public final static String COLUMN_IMAGE_LINK = "image_link";
    }

    public static final class Image implements BaseColumns {
        public final static String TABLE_NAME = "images";

        public final static String ID = BaseColumns._ID;
        public final static String COLUMN_URL = "url";
        public final static String COLUMN_FILENAME = "filename";
    }
}
