
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.validator.PublicClassValidator;

public class Movie {
	
	static float success;
	static float verifySuccess;
	static float noAnyLangSource;		
	
	private int id;
	private ArrayList<String> genre = new ArrayList<>();
	private double rating;
	private String vikiURL_TR;
	private String wikiURL_EN;
	private int year;
	private String context_TR;
	private String context_ENG;

	
	private InfoBox infoBox;
	private boolean verified;
	
	private ArrayList<Word> wordListTr = new ArrayList<Word>();
	private ArrayList<Word> wordListEng = new ArrayList<Word>();
	
	//CONSTRUCTORS
	public Movie(){
		setVikiURL_TR("Kaynak Bulunamadý");//türkçe kaynak bulunamazsa bu deðer böyle kalacaktýr.
	}
	public Movie(int id, String wikiURL_EN, String vikiURL_TR, int year){
		setId(id);
		setWikiURL_EN(wikiURL_EN);
		setVikiURL_TR(vikiURL_TR);
		setYear(year);
	}	
	
	//FUNCTIONS
	public void setActiveWikiLink(){
		/*
		 * _(YIL_film), _(film) ve uzantýsýz linkleri 404 hatasý almayana kadar dener, 
		 * eriþilebilen linki uzantýsyla birlikte movie'nin wikiURL_EN fieldýna set eder.
		 */
		
		FileIO fileIO = FileIO.getFileIO();
		String activeLink = null;
		//System.out.println(this.id+")"+this.wikiURL_EN);
		if(fileIO.check404(this.wikiURL_EN+"_("+this.year+"_film)")){
			activeLink = this.wikiURL_EN+"_("+this.year+"_film)";
		}
		else if(fileIO.check404(this.wikiURL_EN+"_(film)")){
			activeLink = this.wikiURL_EN+"_(film)";
		}
		else if(fileIO.check404(this.wikiURL_EN)){
			activeLink = this.wikiURL_EN;
		}
		else {
			activeLink = "No Url Source";
			setNoAnyLangSource(getNoAnyLangSource() + 1);//ingilizce kaynak yoksa türkçe kaynak da çýkmayacaðýný kabul ediyoruz
		}
		//System.out.println(" *active URL: "+activeLink);
		this.setWikiURL_EN(activeLink);
	}
	
	public void setActiveVikiURL(){
		/*
		 * setActiveWikiLink() methodu çalýþtýktan sonra elimizde movienin eriþilebilir ve doðru inglizce linki var
		 * 404 hatasý yani exception gelme ihtimali yok. Bu link jsoup ile parse edilip türkçe link için kontrol edilir
		 * yaratýlýþta movie nesneleri new Movie() constructorýnda vikiURL_TR olarak "TÜRKÇE KAYNAK BULUNAMADI" deðerini alýr
		 * method içinde bu deðerin deðiþmesini bekleriz, eðer deðiþmiþse success 1 atar.
		 */
		Document doc = null;
		try {
			
			if(!this.getWikiURL_EN().equals("No Url Source")){
				
				doc = Jsoup.connect(this.getWikiURL_EN()).get();
				Elements links = doc.select("a[href*=https://tr.");//https://tr. içeren a-href'leri bul
		    	for(Element element : links){
		    		this.setVikiURL_TR(element.attr("href"));//zaten 1 tane gelicek vikiURL'ye kaydet
		    	}	
		    	if(!this.getVikiURL_TR().equals("Kaynak Bulunamadý"))
		    		setSuccess(getSuccess() + 1);
	    	}	    	
		} catch (Exception e) {
			e.printStackTrace();
		}    	
	}
	
	public String findContext(String url, String language) throws IOException{
		//verilen url'ye göre context bulan method(url movie objesine ait wiki ve ya viki)
		//archivede movieArchive listesindeki her bir movienin body contextini almak için kullanýcak
		Document doc = Jsoup.connect(url).get();
		
		//wikipedia'da içeriði çekmek için gerekli elementin tagi "div#mw-content-text"
		String textBody = doc.select("div#mw-content-text").text();
		
		//verilen dil seçeneðine göre movie'ye ait context fiedlarýný seçiyor(eng veya tr)
		if(language.equals("TR"))
			//eðer sayfanýn dili türkçe ise setContent_TR methodu ile context_TR fieldýna set edilir.
			this.setContext_TR(textBody);
		
		else if(language.equals("ENG"))
			//eðer sayfanýn dili ingilizce ise setContent_ENG methodu ile context_ENG fieldýna set edilir.
			this.setContext_ENG(textBody);
		
		//içeriðin yine dile göre dosyaya yazýlmasý için textBody deðiþkeni return edilir
		return textBody;
	}
	
	public void splitContext(String textBody, ArrayList<Word> wordList, String language){
		//verilen contexti(Wikipedia sayfasýnýn tüm içeriði) kelimelere ayýran method	
		//archivede movieArchive listesindeki her bir movienin body contextini kelimelerini ayýrmak için
		//parametrelerdeki ArrayList'in kaynaðý bu
		
		String[] words = textBody.split("[\\p{Punct}\\s]+");//ayýrma koþulu
		
		for(String word_str : words){
			if(Character.isLetter(word_str.charAt(0)) ){//eldeki string bir harfle baþlýyorsa yani sayý deðil ise
				
				//eldeki kelimeyi arama iþlemi aþaðýdaki method ile yapýlacak
				//daha önce bu kelimeyi kaydetmiþ miyiz?
				//kaydetmiþsek frekansý 1 arttýrýlýr.
				//kaydetmemiþsek
				searchWordAndIncFreq(word_str,wordList,language);				
			}
		}
		Collections.sort(wordList, new CustomComparator());//alfabetik sýralama yapmak için çalýþan comparator
	}
	
	public void searchWordAndIncFreq(String str, ArrayList<Word> wordList, String language){
		
		//tüm kelimeler küçük harfe çevrilir(örneðin Movie ve movie kelimeleri farklý kabul edilmemeli)
		str = str.toLowerCase();
		
		
		ArrayList<String> selectedLangStopWordList = null;
		
		if(language.equals("ENG"))
			selectedLangStopWordList = FileIO.getFileIO().stopWordListENG;
		else if (language.equals("TR"))
			selectedLangStopWordList = FileIO.getFileIO().stopWordListTR;		
		
		if(selectedLangStopWordList.contains(str)==false)//eðer stop-word deðilse kelime deðerlenirilmeli
		{			
			boolean find=false;
	        for(Word a_word: wordList){//methoda gönderilen string yine Methoda gönderilen wordList'te aranýr
	            if(a_word.getWord().equals(str)){//bu kelime varsa freq arttýr
	            	a_word.incFreq();
	                find=true;
	            }
	        }
	        if(!find){//bu word bulunamadýysa
	            Word a_word = new Word(str);//yeni yarat(freq=1 yapýldý constructorda)
	            wordList.add(a_word);
	        }
		}
	}
	
	public String toString(){		
		String str = "\n"+this.id+")"+this.infoBox.getTitle()+"("+this.year+")"+this.infoBox.toString()+
						("\nWiki EN: "+this.wikiURL_EN+"\nViki TR: "+this.vikiURL_TR+"\nRating: "+this.getRating())+"\nGenre: ";
		for(String genre : this.getGenre()){
			str = str + genre+", ";
		}
		return str;
	}
	
	//GETTER-STTER METHODLARI

	public ArrayList<String> getGenre() {
		return genre;
	}
	public double getRating() {
		return rating;
	}
	public void setGenre(ArrayList<String> genre) {
		this.genre = genre;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	
	public ArrayList<Word> getWordListTr() {
		return wordListTr;
	}
	public void setWordListTr(ArrayList<Word> wordListTr) {
		this.wordListTr = wordListTr;
	}
	public ArrayList<Word> getWordListEng() {
		return wordListEng;
	}
	public void setWordListEng(ArrayList<Word> wordListEng) {
		this.wordListEng = wordListEng;
	}
	public InfoBox getInfoBox() {
		return infoBox;
	}
	public void setInfoBox(InfoBox infoBox) {
		this.infoBox = infoBox;
	}	
		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getVikiURL_TR() {
		return vikiURL_TR;
	}
	public void setVikiURL_TR(String vikiURL_TR) {
		this.vikiURL_TR = vikiURL_TR;
	}
	public String getWikiURL_EN() {
		return wikiURL_EN;
	}
	public void setWikiURL_EN(String wikiURL_EN) {
		this.wikiURL_EN = wikiURL_EN;
	}
	public float getSuccess() {
		return success;
	}
	public void setSuccess(float success) {
		Movie.success = success;
	}
	public float getNoAnyLangSource() {
		return noAnyLangSource;
	}
	public void setNoAnyLangSource(float noAnyLangSource) {
		Movie.noAnyLangSource = noAnyLangSource;
	}
	public void setVerified(boolean verified){
		this.verified = verified;
	}
	public boolean getVerified(){
		return this.verified;
	}
	public float getVerifySuccess() {
		return verifySuccess;
	}
	public void setVerifySuccess(float verifySuccess) {
		Movie.verifySuccess = verifySuccess;
	}
	public String getContext_TR() {
		return context_TR;
	}
	public void setContext_TR(String context) {
		this.context_TR = context;
	}
	public String getContext_ENG() {
		return context_ENG;
	}
	public void setContext_ENG(String context) {
		this.context_ENG = context;
	}
	public String getContext(String language) {
		if(language.equals("TR"))
			return context_TR;
		else if(language.equals("ENG"))
			return context_ENG;
		return null;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	
}

