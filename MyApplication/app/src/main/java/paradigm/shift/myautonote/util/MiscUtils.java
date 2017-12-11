package paradigm.shift.myautonote.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import paradigm.shift.myautonote.data_model.DataItem;
import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_util.DataReader;

/**
 * Miscellaneous utility methods.
 * Created by aravind on 12/5/17.
 */

@SuppressWarnings("WeakerAccess")
public class MiscUtils {
    public static String constructFullName(List<? extends DataItem> dir) {
        StringBuilder sb = new StringBuilder(dir.size()*2);
        for (DataItem d : dir) {
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

    public static String[] getCurPathStr(List<? extends DataItem> path) {
        return getCurPathStr(path, true);
    }

    public static String[] getCurPathStr(List<? extends DataItem> path, boolean includeLastItem) {
        int sub = 0;
        if (!includeLastItem) { sub = 1; }

        String[] curPath = new String[path.size() - sub];
        for (int i = 0; i < path.size() - sub; i++) {
            curPath[i] = path.get(i).getName();
        }
        return curPath;
    }

    public static List<Directory> getCurPathList(Context context, String[] path) {
        return getCurPathList(context, path, true);
    }

    public static List<Directory> getCurPathList(Context context, String[] path, boolean includeLastItem) {
        int sub = 0;
        if (!includeLastItem) { sub = 1; }

        Directory curDir = DataReader.getInstance(context).getTopDir();
        List<Directory> itemDirList = new ArrayList<>(path.length - sub);
        itemDirList.add(curDir);
        for (int j = 1; j < path.length - sub; j++) {
            curDir = curDir.getSubDirectory(path[j]);
            if (curDir == null) {
                return null;
            }
            itemDirList.add(curDir);
        }
        return itemDirList;
    }

    public static List<Directory> getCurPathList(Context context, String pathStr) {
        return getCurPathList(context, pathStr, true);
    }

    public static List<Directory> getCurPathList(Context context, String pathStr, boolean includeLastItem) {
        return getCurPathList(context, pathStr.split("/"), includeLastItem);
    }
}
