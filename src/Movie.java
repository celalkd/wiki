
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Movie {
	
	static float success;
	static float verifySuccess;
	static float noAnyLangSource;		
	
	private int id;
	private String vikiURL_TR;
	private String wikiURL_EN;
	private String year;
	private String context_TR;
	private String context_ENG;

	
	private InfoBox infoBox;
	private boolean verified;
	
	private ArrayList<Word> wordListTr = new ArrayList<Word>();
	private ArrayList<Word> wordListEng = new ArrayList<Word>();
	
	//CONSTRUCTORS
	public Movie(){
		setVikiURL_TR("TÜRKÇE KAYNAK BULUNAMADI");//türkçe kaynak bulunamazsa bu deðer böyle kalacaktýr.
	}
	public Movie(int id, String wikiURL_EN, String vikiURL_TR, String year){
		setId(id);
		setWikiURL_EN(wikiURL_EN);
		setVikiURL_TR(vikiURL_TR);
		setYear(year);
	}	
	
	//FUNCTIONS
	public String findContext(String url, String language) throws IOException{
		Document doc = Jsoup.connect(url).get();
		String textBody = doc.select("div#mw-content-text").text();
		if(language.equals("TR"))
			this.setContext_TR(textBody);
		
		else if(language.equals("ENG"))
			this.setContext_ENG(textBody);
		return textBody;
	}
	public void splitContext(String textBody, ArrayList<Word> wordList){
		/*String[] eklerTR={"ta","te","da","de","a","e","ý","i","dan","den","ten","tan"};
		ArrayList<String> eklerTRList = new ArrayList<>();
		eklerTRList.addAll(Arrays.asList(eklerTR));*/
		
		String[] words = textBody.split("[\\p{Punct}\\s]+");
		for(String w : words){
			if(Character.isLetter(w.charAt(0))){//word sayý deðil ise
				searchWordAndIncFreq(w,wordList);
			}
		}
		Collections.sort(wordList, new CustomComparator());
	}
	public void searchWordAndIncFreq(String str, ArrayList<Word> wordList){
		str = str.toLowerCase();
		boolean find=false;
        for(Word obj: wordList){
            if(obj.getWord().equals(str)){//bu word varsa freq arttýr
                obj.incFreq();
                find=true;
            }
        }
        if(!find){//bu word bulunamadýysa
            Word obj = new Word(str);//yeni yarat(fre1=1 yapýldý constructorda)
            wordList.add(obj);
        }
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
			
			doc = Jsoup.connect(this.getWikiURL_EN()).get();
			Elements links = doc.select("a[href*=https://tr.");//https://tr. içeren a-href'leri bul
	    	for(Element element : links){
	    		this.setVikiURL_TR(element.attr("href"));//zaten 1 tane gelicek vikiURL'ye kaydet
	    			
	    	}
	    	if(!this.getVikiURL_TR().equals("TÜRKÇE KAYNAK BULUNAMADI"))
	    		setSuccess(getSuccess() + 1);
		} catch (Exception e) {
			
		}    	
	}
	public void setActiveWikiLink(){
		/*
		 * _(YIL_film), _(film) ve uzantýsýz linkleri 404 hatasý almayana kadar dener, 
		 * eriþilebilen linki uzantýsyla birlikte movie'nin wikiURL_EN fieldýna set eder.
		 */
		FileIO fileIO = FileIO.getFileIO();
		String activeLink = null;
		System.out.println(this.id+")"+this.wikiURL_EN);
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
			activeLink = "INGLIZCE KAYNAK BULUNAMADI";
			setNoAnyLangSource(getNoAnyLangSource() + 1);
		}
		System.out.println(" *active URL: "+activeLink);
		this.setWikiURL_EN(activeLink);
	}
	@Override
	public String toString(){		
		return "\n"+this.id+")"+this.infoBox.getTitle()+"("+this.year+")"+this.infoBox.toString()+
		("\nWiki EN: "+this.wikiURL_EN+"\nViki TR: "+this.vikiURL_TR);
	}
	
	//GETTER-STTER METHODLARI
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
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
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
	
	
}

