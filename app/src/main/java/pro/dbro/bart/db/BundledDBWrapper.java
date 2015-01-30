package pro.dbro.bart.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * This class is used to bootstrap the application database
 * from one stored in the assets directory.
 *
 * Usage:
 *
 * <pre>
 * {@code
 * BundledDBWrapper wrapper = new BundledDBWrapper(context);
 * wrapper.getReadableDatabase();
 * </pre>
 *
 * Created by davidbrodsky on 1/29/15.
 */
public class BundledDBWrapper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "bartDatabase.db";
    private static final int DATABASE_VERSION = 2;

    public BundledDBWrapper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
