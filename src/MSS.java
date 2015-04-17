import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hyunjun on 2015-04-16.
 */
public class MSS { // MinimalSourceSet
    private Set<Set<Long>> mss;

    public MSS() {
        mss = new HashSet<Set<Long>>();
    }

    // mss format 0,1,2,;3,4,;
    @Override
    public String toString() {
        String str = "";
        for (Set<Long> s : mss) {
            for (Long id : s) {
                str += id + ",";
            }
            str += ";";
        }
        return null;
    }

    public static MSS valueOf(String str) {
        MSS mss = new MSS();
        String[] setSeq = str.split(";");
        Set<Long> idSet = new HashSet<Long>();
        for (String setStr : setSeq) {
            String[] idSeq = setStr.split(",");
            for (String idStr : idSeq) {
                idSet.add(Long.valueOf(idStr));
            }
            mss.mss.add(idSet);
        }
        return mss;
    }

    public MSS cartesian(MSS other) {
        return null;
    }

    // filtering minimal set
    private void filter() {

    }
}
