import java.util.HashSet;
import java.util.Set;

/**
 * SourceSet implemented with HashSet
 * Created by hyunjun on 2015. 5. 1..
 */
public class SourceSet {
    private Set<Long> sources;

    public SourceSet() {
        sources = new HashSet<>();
    }

    public SourceSet(Long source) {
        this();
        sources.add(source);
    }

    public void add(Long source) {
        sources.add(source);
    }

    @Override
    public String toString() {
        String str = "";
        for (Long s : sources) {
            str += s + ",";
        }
        return str;
    }

    public static SourceSet valueOf(String str) {
        SourceSet sourceSet = new SourceSet();
        String[] nodeSeq = str.split(",");
        for (String nodeStr : nodeSeq) {
            sourceSet.add(Long.valueOf(nodeStr));
        }
        return sourceSet;
    }

    public boolean containsAll() {
        //TODO:
        return false;
    }
}
