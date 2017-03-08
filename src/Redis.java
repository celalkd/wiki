

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.*;

public class Redis {
	public Jedis jedis;
	public Connection connection;
	
	public Redis(){
		jedis = new Jedis("localhost");
	}
	public void createWordFreqStore(Movie movie, ArrayList<Word> wordList){
		
		String key = new Integer(movie.getId()).toString();
		
		for(Word word : wordList){
			
			String freqStr = new Integer(word.getFreq()).toString();
			
			jedis.lpush(key,(word.getWord()+" "+freqStr) );
		}
		
	}
	public Word parseValue(String value){
		Word word = new Word();
		String[] strings = value.split(" ");
		word.setWord(strings[0]);
		int freq = Integer.parseInt(strings[1]);
		word.setFreq(freq);
		return word;
	}
	public ArrayList<Word> collectValues(String key){
		long end = lastIndex(key);
		List<String> valueList = jedis.lrange(key, 0, end);
		ArrayList<Word> wordList = new ArrayList<>();
		for(String value : valueList){
			Word word = parseValue(value);
			wordList.add(word);
		}		
		return wordList;
	}
	public long lastIndex(String key){
		Pipeline pipeline = null;
		Response response = null;
		try {
			pipeline = jedis.pipelined();
			response = pipeline.llen(key);
			pipeline.sync();
			pipeline.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return (long) response.get();
	}
	public int searchFreq(String value, ArrayList<Word> wordList){
		for(Word w : wordList){
			if(w.getWord().equals(value)){
				System.out.println(value+" "+w.getWord());
				return w.getFreq();
			}
		}
		return 0;
	}
	public void query(String... words){
		jedis.select(1);
		ArrayList<Integer> keyList = new ArrayList<Integer>();
		for(int i=0; i<=9; i++){
			keyList.add(new Integer(i));
		}
		//boolean bitti = false;
		
		
		for(String word: words){
			System.out.println("yeni kelime:"+word);
			for(Integer key : keyList){
				//System.out.println("yeni key:"+key);
				if(key!=-1){
					//System.out.println("baktýk");
					String keyStr = key.toString();
					ArrayList<Word> wordList = collectValues(keyStr);
					System.out.println("Key:"+key+" "+wordList.get(2).getWord());

					int freq = searchFreq(word, wordList);
					if(freq!=0){
						System.out.println("Key:"+key+" valueWord:"+word+" valueFreq:"+freq);
					}	
					else {
						System.out.println("Key:"+key+" removed");
						int value =keyList.indexOf(key);
						keyList.set(value, new Integer(-1));						
					}
				}	
				else 
					System.out.println("bakmadýk");
			}
			for(Integer key : keyList){
				System.out.println("Key:"+key);
			}
			
		}
		
	}
	/*public boolean deneme(String word, ArrayList<Integer> keyList, boolean bitti){
		for(Object key : keyList){
			
			String keyStr = key.toString();
			ArrayList<Word> wordList = collectValues(keyStr);
			
			int freq = searchFreq(word, wordList);
			if(freq!=0){
				System.out.println("Key:"+key+" valueWord:"+word+" valueFreq:"+freq);
			}	
			else {
				System.out.println("Key:"+key+" removed");	
				keyList.remove(key);
				bitti = true;
			}					
		}
		return bitti;
	}*/
}
/*for(Object key : keyList){

String keyStr = key.toString();
ArrayList<Word> wordList = collectValues(keyStr);

int freq = searchFreq(word, wordList);
if(freq!=0){
	System.out.println("Key:"+key+" valueWord:"+word+" valueFreq:"+freq);
}	
else {
	System.out.println("Key:"+key+" removed");	
	
}					
}*/
