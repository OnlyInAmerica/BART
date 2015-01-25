package pro.dbro.bart.db;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by davidbrodsky on 1/24/15.
 */
@Database(version = BartDatabase.VERSION)
public final class BartDatabase {

    public static final int VERSION = 1;

    @Table(LoadColumns.class) public static final String LOAD = "load";
}
