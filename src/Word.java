public class Word{

	private String word;
	private int freq;
	
	public Word(String word){
		setWord(word);
		setFreq(1);
	}	
	public Word(){
		
	}
	public String getWord() {
		return this.word;
	}
	public int getFreq() {
		return this.freq;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public void setFreq(int freq) {
		this.freq = freq;
	}
	public void incFreq() {
		this.freq++;
	}
}

