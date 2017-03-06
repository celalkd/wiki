

import java.util.ArrayList;

import redis.clients.jedis.Jedis;

public class Redis {
	public Jedis jedis;
	
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

}
