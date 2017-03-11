

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



import redis.clients.jedis.*;

public class Redis {
	public Jedis jedis;
	public Connection connection;
	public ArrayList<Integer> keyList = new ArrayList<>();
	
	public Redis(){
		jedis = new Jedis("localhost");
	}
	
	public void createWordFreqStore(Movie movie, ArrayList<Word> wordList){
		
		String key = new Integer(movie.getId()).toString();
		
		for(Word word : wordList){
			if(word.getFreq()>=10){
				String freqStr = new Integer(word.getFreq()).toString();			
				jedis.lpush(key,(word.getWord()+" "+freqStr) );
			}			
		}		
	}
	public void createRedis(ArrayList<Movie> movieList) throws IOException{
		
		jedis.flushAll();
		
		for(Movie movie : movieList){			
			
			System.out.println(movie.getInfoBox().getTitle()+" Redis");
			movie.setWordLists();//movie'lerin word'lerini kelime-frekans olarak kaydeder
			
			jedis.select(0);
			createWordFreqStore(movie, movie.getWordListTr());	
			
			jedis.select(1);
			createWordFreqStore(movie, movie.getWordListEng());
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
	public int searchFreq(String searchWord, ArrayList<Word> wordList){
		//int control=0;
		for(Word w : wordList){
			String wordString = w.getWord();	
				if(wordString.equals(searchWord)){
				return w.getFreq();			}
		}
		return 0;
		/*if(!wordString.equals(searchWord)){
		//
	}
	else */
	}
	public void getKeyList(){
		Iterator<String> iterator = jedis.keys("*").iterator();
		while(iterator.hasNext()){
			Integer i = Integer.parseInt(iterator.next());
			this.keyList.add(i);
		}
	}
	public void query(String... words){
		jedis.select(1);		
		this.getKeyList();
		for(String word: words){
			for(Integer keyInt : this.keyList){
				if(keyInt!=-1){					
					String keyStr = keyInt.toString();
					ArrayList<Word> wordList = collectValues(keyStr);
					int freq = searchFreq(word, wordList);
					if(freq==0){
						int index =keyList.indexOf(keyInt);
						keyList.set(index, new Integer(-1));
					}
				}					
			}	
		}
		MongoDB mongoDB = MongoDB.getMongoDB();
		Neo4j neo4j = new Neo4j();
		for(Integer keyInt : keyList){
			if(keyInt!=-1){
				System.out.println("Movie ID: "+keyInt);
				mongoDB.query_with_id(keyInt);
				neo4j.query(keyInt.toString());
			}
		}
	}
	
	
}

