package paradigm.shift.myautonote.util;

import java.util.Calendar;
import java.util.List;

import paradigm.shift.myautonote.data_model.Directory;

/**
 * Created by aravind on 12/5/17.
 */

public class MiscUtils {
    public static String constructFullName(List<Directory> dir) {
        StringBuilder sb = new StringBuilder(dir.size()*2);
        for (Directory d : dir) {
            sb.append(d.getName());
            sb.append("/");
        }
        return sb.toString();
    }

    public static int getDayOfTheWeek() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_WEEK);
    }

    public static int getSecondsSinceMidnight() {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long passed = now - c.getTimeInMillis();
        return (int) passed / 1000;
    }
}
