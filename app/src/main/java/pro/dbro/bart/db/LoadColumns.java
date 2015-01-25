package pro.dbro.bart.db;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.DefaultValue;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by davidbrodsky on 1/24/15.
 */
public interface LoadColumns {

    /** SQL type        Modifiers                   Reference Name            SQL Column Name */
    @DataType(INTEGER)  @PrimaryKey @AutoIncrement  String id               = "_id";
    @DataType(TEXT)     @NotNull                    String station          = "station";
    @DataType(INTEGER)                              String route            = "route";
    @DataType(INTEGER)                              String train            = "train";
    @DataType(INTEGER)                              String load             = "load";
    @DataType(TEXT)                                 String time             = "time";
}
