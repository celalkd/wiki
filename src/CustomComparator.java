import java.util.Comparator;

public class CustomComparator implements Comparator<Word> {
    @Override
    public int compare(Word o1, Word o2) {
        Integer f1 = o1.getFreq();
        Integer f2 = o2.getFreq();
        
        return f2.compareTo(f1);
    }
}
