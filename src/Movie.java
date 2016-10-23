
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Movie {
	
	private static float success;
	private static float noAnyLangSource;
	
	private int id;
	private String title;
	private String vikiURL_TR;
	private String wikiURL_EN;
	private String year;
	private InfoBox infoBox;
	

	public Movie(){
		setVikiURL_TR("T�RK�E KAYNAK BULUNAMADI");//t�rk�e kaynak bulunamazsa bu de�er b�yle kalacakt�r.
	}
	public Movie(int id, String title, String wikiURL_EN, String vikiURL_TR, String year){
		setId(id);
		setTitle(title);
		setWikiURL_EN(wikiURL_EN);
		setVikiURL_TR(vikiURL_TR);
		setYear(year);
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
			
			doc = Jsoup.connect(this.getWikiURL_EN()).get();
			Elements links = doc.select("a[href*=https://tr.");//https://tr. i�eren a-href'leri bul
	    	for(Element element : links){
	    		this.setVikiURL_TR(element.attr("href"));//zaten 1 tane gelicek vikiURL'ye kaydet
	    			
	    	}
	    	if(!this.getVikiURL_TR().equals("T�RK�E KAYNAK BULUNAMADI"))
	    		setSuccess(getSuccess() + 1);
		} catch (Exception e) {
			
		}    	
	}
	
	public void setActiveWikiLink(){
		/*
		 * _(YIL_film), _(film) ve uzant�s�z linkleri 404 hatas� almayana kadar dener, 
		 * eri�ilebilen linki uzant�syla birlikte movie'nin wikiURL_EN field�na set eder.
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
	
	public void printInfoBOX(){
		/*
		 * Movie nesnesine ait InfoBox nesnesinin fieldlar�n� ekrana bast�ran method
		 */
		System.out.print("\nTitle: "+infoBox.getTitle()+"\nDirector: "+infoBox.getDirector()+"\nStarring: ");
		for(String star : infoBox.getStarring()){
			System.out.print(star+", ");
		}
		System.out.println("\n");
	}
	
	
	//GETTER-STTER METHODLARI
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
	
	
}

