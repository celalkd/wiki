import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public class TestMovie {

	@Test
	public void testBodyContent() throws IOException {
		Movie movie = new Movie();
		//String context = movie.findContext("https://en.wikipedia.org/wiki/Toy_Story");
		//System.out.println(context);
		ArrayList<Word> wordList = new ArrayList<>();
		wordList.add(new Word("abc"));
		wordList.add(new Word("qwe"));
		String[] strings ={"ABc","xyz","xyz","dfg","abc","den","xyz","dan"};
		String[] eklerTR={"ta","te","da","de","a","e","ý","i","dan","den","ten","tan"};
		ArrayList<String> eklerTRList = new ArrayList<>();
		eklerTRList.addAll(Arrays.asList(eklerTR));
		for(String s : strings){
				if(Character.isLetter(s.charAt(0)) && !eklerTRList.contains(s)){//word sayý deðil ise
					movie.searchWordAndIncFreq(s,wordList);
				}
			}
		for(Word w : wordList){
			System.out.println(w.getWord()+", "+w.getFreq());
		}
	}

}
