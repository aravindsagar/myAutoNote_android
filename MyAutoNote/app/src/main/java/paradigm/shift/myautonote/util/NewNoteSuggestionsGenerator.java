package paradigm.shift.myautonote.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import paradigm.shift.myautonote.data_model.Directory;
import paradigm.shift.myautonote.data_model.metadata.NoteCreationTime;
import paradigm.shift.myautonote.data_util.DataReader;
import paradigm.shift.myautonote.data_util.MetadataDb;

/**
 * Generates suggestions for new note creation. The suggestions are based on previous usage.
 * Created by aravind on 12/5/17.
 */

public class NewNoteSuggestionsGenerator {
    public static final int NUM_SUGGESTIONS = 3;

    /**
     * Generate new note suggestions. Do not call from main (UI) thread!
     */
    public static List<List<Directory>> generateSuggestions(Context context) {
        List<List<Directory>> suggestions = new ArrayList<>(NUM_SUGGESTIONS);
        MetadataDb db = MetadataDb.getInstance(context);

        final int todayOfWeek = MiscUtils.getDayOfTheWeek();
        final int curSecondsSInceMidnight = MiscUtils.getSecondsSinceMidnight();

        NoteCreationTime[] noteCreationTimes = db.metadataDao().loadAllNoteCreationTimes();

        // Sort according to time of the day.
        Arrays.sort(noteCreationTimes, new Comparator<NoteCreationTime>() {
            @Override
            public int compare(NoteCreationTime o1, NoteCreationTime o2) {
                return Math.abs(o1.timeOfDay-curSecondsSInceMidnight) - Math.abs(o2.timeOfDay-curSecondsSInceMidnight);
            }
        });

        // Sort according to day of week.
        Arrays.sort(noteCreationTimes, new Comparator<NoteCreationTime>() {
            @Override
            public int compare(NoteCreationTime o1, NoteCreationTime o2) {
                int a = Math.abs(o1.dayOfWeek-todayOfWeek);
                int b = Math.abs(o2.dayOfWeek-todayOfWeek);
                if (a > 3) {
                    a = 7-a;
                }
                if (b > 3) {
                    b = 7-b;
                }
                return a-b;
            }
        });

        int i=0;
        Set<String> added = new HashSet<>();

        while (suggestions.size() < NUM_SUGGESTIONS && i < noteCreationTimes.length) {
            // Skip already added dirs.
            if (added.contains(noteCreationTimes[i].itemDirPath)) {
                i++;
                continue;
            }
            added.add(noteCreationTimes[i].itemDirPath);

            // Construct the dir path.
            List<Directory> itemDirList = MiscUtils.getCurPathList(context, noteCreationTimes[i].itemDirPath);
            if (itemDirList != null) {
                suggestions.add(itemDirList);
            }
            i++;
        }

        // If we don't have 3 suggestions, use dirs from our top dir.
        i = 0;
        Directory topDir = DataReader.getInstance(context).getTopDir();
        List<String> topSubDirs = topDir.getSubdirectoryNames();
        while (suggestions.size() < NUM_SUGGESTIONS && topSubDirs.size() > i) {
            String fullName = topDir.getName() + "/" + topSubDirs.get(i) + "/";
            if (added.contains(fullName)) {
                i++;
                continue;
            }
            added.add(fullName);
            List<Directory> result = new ArrayList<>(2);
            result.add(topDir);
            result.add(topDir.getSubDirectory(topSubDirs.get(i)));
            suggestions.add(result);
            i++;
        }
        return suggestions;
    }

}
