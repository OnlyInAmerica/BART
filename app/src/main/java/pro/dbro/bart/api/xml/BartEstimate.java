package pro.dbro.bart.api.xml;

import android.support.annotation.NonNull;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by davidbrodsky on 2/11/14.
 */
@Root(strict = false, name = "estimate")
public class BartEstimate implements Comparable<BartEstimate>{

    @Element(name = "minutes")
    public String minutes;

    private Date date;

    @Element(name = "platform")
    private String platform;

    @Element(name = "direction")
    private String direction;

    @Element(name = "length")
    private int length;

    @Element(name = "color")
    private String color;

    @Element(name = "hexcolor")
    private String hexColor;

    @Element(name = "bikeflag")
    private boolean bikesAllowed;

    public long getDeltaSecondsEstimate() {
        long diffInMs = getDateEstimate().getTime() - new Date().getTime();
        return TimeUnit.MILLISECONDS.toSeconds(diffInMs);
    }

    public Date getDateEstimate(){
        if (date == null) setDateEstimate();
        return date;
    }

    public void setDateEstimate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND,
                ((minutes.contains("<1")) ? 30 : Integer.parseInt(minutes) * 60));
        date = cal.getTime();
    }

    public void setDeltaMinutesEstimate(String minutes) {
        this.minutes = minutes;
    }

    public String getPlatform() {
        return platform;
    }

    public String getDirection() {
        return direction;
    }

    public int getLength() {
        return length;
    }

    public String getColor() {
        return color;
    }

    public String getHexColor() {
        return hexColor;
    }

    public boolean areBikesAllowed() {
        return bikesAllowed;
    }

    @Override
    public int compareTo(@NonNull BartEstimate another) {
        return getDateEstimate().compareTo(another.getDateEstimate());
    }
}
