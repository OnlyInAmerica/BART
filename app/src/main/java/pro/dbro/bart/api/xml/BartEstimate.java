package pro.dbro.bart.api.xml;

import android.support.annotation.NonNull;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by davidbrodsky on 2/11/14.
 */
@Root(strict = false, name = "estimate")
public class BartEstimate implements Comparable<BartEstimate>{

    @Element(name = "minutes")
    private String minutes;

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

    public String getDeltaMinutesEstimate() {
        return minutes;
    }

    public Date getDateEstimate(){

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE,
                Integer.parseInt(((getDeltaMinutesEstimate().contains("<1")) ? "0" : getDeltaMinutesEstimate())));
        return cal.getTime();
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
        int thisMinutes;
        if(getDeltaMinutesEstimate().contains("<1")){
            thisMinutes = 0;
        } else {
            thisMinutes = Integer.parseInt(getDeltaMinutesEstimate());
        }
        int anotherMinutes;
        if(another.getDeltaMinutesEstimate().contains("<1")){
            anotherMinutes = 0;
        } else {
            anotherMinutes = Integer.parseInt(getDeltaMinutesEstimate());
        }
        return thisMinutes - anotherMinutes;
    }
}
