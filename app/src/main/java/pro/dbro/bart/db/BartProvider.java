package pro.dbro.bart.db;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by davidbrodsky on 1/24/15.
 */
@ContentProvider(authority = BartProvider.AUTHORITY, database = BartDatabase.class)
public final class BartProvider {

    public static final String AUTHORITY = "pro.dbro.bart.BartProvider";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }


    @TableEndpoint(table = BartDatabase.LOAD)
    public static class Load {

        private static final String ENDPOINT = "load";

        @ContentUri(
                path = ENDPOINT,
                type = "vnd.android.cursor.dir/list",
                defaultSort = LoadColumns.id + " ASC")
        public static final Uri LOAD = buildUri(ENDPOINT);
    }
}