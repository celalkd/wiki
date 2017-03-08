
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
		setVikiURL_TR("Kaynak Bulunamad�");//t�rk�e kaynak bulunamazsa bu de�er b�yle kalacakt�r.
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
		 * _(YIL_film), _(film) ve uzant�s�z linkleri 404 hatas� almayana kadar dener, 
		 * eri�ilebilen linki uzant�syla birlikte movie'nin wikiURL_EN field�na set eder.
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
			setNoAnyLangSource(getNoAnyLangSource() + 1);//ingilizce kaynak yoksa t�rk�e kaynak da ��kmayaca��n� kabul ediyoruz
		}
		//System.out.println(" *active URL: "+activeLink);
		this.setWikiURL_EN(activeLink);
	}
	
	public void setActiveVikiURL(){
		/*
		 * setActiveWikiLink() methodu �al��t�ktan sonra elimizde movienin eri�ilebilir ve do�ru inglizce linki var
		 * 404 hatas� yani exception gelme ihtimali yok. Bu link jsoup ile parse edilip t�rk�e link i�in kontrol edilir
		 * yarat�l��ta movie nesneleri new Movie() constructor�nda vikiURL_TR olarak "T�RK�E KAYNAK BULUNAMADI" de�erini al�r
		 * method i�inde bu de�erin de�i�mesini bekleriz, e�er de�i�mi�se success 1 atar.
		 */
		Document doc = null;
		try {
			
			if(!this.getWikiURL_EN().equals("No Url Source")){
				
				doc = Jsoup.connect(this.getWikiURL_EN()).get();
				Elements links = doc.select("a[href*=https://tr.");//https://tr. i�eren a-href'leri bul
		    	for(Element element : links){
		    		this.setVikiURL_TR(element.attr("href"));//zaten 1 tane gelicek vikiURL'ye kaydet
		    	}	
		    	if(!this.getVikiURL_TR().equals("Kaynak Bulunamad�"))
		    		setSuccess(getSuccess() + 1);
	    	}	    	
		} catch (Exception e) {
			e.printStackTrace();
		}    	
	}
	
	public String findContext(String url, String language) throws IOException{
		//verilen url'ye g�re context bulan method(url movie objesine ait wiki ve ya viki)
		//archivede movieArchive listesindeki her bir movienin body contextini almak i�in kullan�cak
		Document doc = Jsoup.connect(url).get();
		
		//wikipedia'da i�eri�i �ekmek i�in gerekli elementin tagi "div#mw-content-text"
		String textBody = doc.select("div#mw-content-text").text();
		
		//verilen dil se�ene�ine g�re movie'ye ait context fiedlar�n� se�iyor(eng veya tr)
		if(language.equals("TR"))
			//e�er sayfan�n dili t�rk�e ise setContent_TR methodu ile context_TR field�na set edilir.
			this.setContext_TR(textBody);
		
		else if(language.equals("ENG"))
			//e�er sayfan�n dili ingilizce ise setContent_ENG methodu ile context_ENG field�na set edilir.
			this.setContext_ENG(textBody);
		
		//i�eri�in yine dile g�re dosyaya yaz�lmas� i�in textBody de�i�keni return edilir
		return textBody;
	}
	
	public void splitContext(String textBody, ArrayList<Word> wordList, String language){
		//verilen contexti(Wikipedia sayfas�n�n t�m i�eri�i) kelimelere ay�ran method	
		//archivede movieArchive listesindeki her bir movienin body contextini kelimelerini ay�rmak i�in
		//parametrelerdeki ArrayList'in kayna�� bu
		
		String[] words = textBody.split("[\\p{Punct}\\s]+");//ay�rma ko�ulu
		
		for(String word_str : words){
			if(Character.isLetter(word_str.charAt(0)) ){//eldeki string bir harfle ba�l�yorsa yani say� de�il ise
				
				//eldeki kelimeyi arama i�lemi a�a��daki method ile yap�lacak
				//daha �nce bu kelimeyi kaydetmi� miyiz?
				//kaydetmi�sek frekans� 1 artt�r�l�r.
				//kaydetmemi�sek
				searchWordAndIncFreq(word_str,wordList,language);				
			}
		}
		Collections.sort(wordList, new CustomComparator());//alfabetik s�ralama yapmak i�in �al��an comparator
	}
	
	public void searchWordAndIncFreq(String str, ArrayList<Word> wordList, String language){
		
		//t�m kelimeler k���k harfe �evrilir(�rne�in Movie ve movie kelimeleri farkl� kabul edilmemeli)
		str = str.toLowerCase();
		
		
		ArrayList<String> selectedLangStopWordList = null;
		
		if(language.equals("ENG"))
			selectedLangStopWordList = FileIO.getFileIO().stopWordListENG;
		else if (language.equals("TR"))
			selectedLangStopWordList = FileIO.getFileIO().stopWordListTR;		
		
		if(selectedLangStopWordList.contains(str)==false)//e�er stop-word de�ilse kelime de�erlenirilmeli
		{			
			boolean find=false;
	        for(Word a_word: wordList){//methoda g�nderilen string yine Methoda g�nderilen wordList'te aran�r
	            if(a_word.getWord().equals(str)){//bu kelime varsa freq artt�r
	            	a_word.incFreq();
	                find=true;
	            }
	        }
	        if(!find){//bu word bulunamad�ysa
	            Word a_word = new Word(str);//yeni yarat(freq=1 yap�ld� constructorda)
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

