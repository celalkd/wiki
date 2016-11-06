import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class InfoBox {
	
	private String title;
	private String director;
	private ArrayList<String> starring = new ArrayList<String>();
	
	//CONSTRUCTORS
	public InfoBox(){
	}
	public InfoBox(String vikiURL){
		/*
		 * wikipedia kaynaðýndaki element pathleri setter methodlara gönderilir
		 */
		setTitle(vikiURL, "#mw-content-text > table.infobox.vevent > tbody > tr:nth-child(1) > th.summary");
		setDirector(vikiURL,0);
		setStarring(vikiURL,0);
	}
	
	//CUSTOM SETTER METHODS
	public void setDirector(String director){
		this.director = director;
	}
	public void setDirector(String vikiURL, int index) {
		/*  
		 * director pathindeki(path constructordan gönderilen string)
		 *  element okunur ve director fieldýna atanýr
		 */
		try {	
			Response res = Jsoup.connect(vikiURL).execute();
			String html = res.body();
			Document doc = Jsoup.parseBodyFragment(html);
			Elements elements_th = doc.getElementsByTag("th");
						
			Element th = elements_th.get(index);
			if(th.text().equals("Directed by")){					
				Element td = th.nextElementSibling();//sonraki sibling'i td öðesi oluyor
				this.director = td.text();
			}
			else{
				index++;
				setDirector(vikiURL, index);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.getCause();
		}		  	
	}
	
	public void setStarring(ArrayList<String> starring){
		this.starring = starring;
	}
	public void setStarring(String vikiURL, int index) {
		
		try {	
			Response res = Jsoup.connect(vikiURL).execute();
			String html = res.body();
			Document doc = Jsoup.parseBodyFragment(html);
			Elements elements_th = doc.getElementsByTag("th");//tüm th'leri çek
			
			Element th = elements_th.get(index);
			if(th.text().equals("Starring")){//starring th'sini bul
				
				Elements elements_td = th.siblingElements();//mevcut th'nin sibling'ini al
				String html_td = elements_td.outerHtml();//sibling td'nin htmlini çýkar
				Document doc2 = Jsoup.parseBodyFragment(html_td);
				Elements elements_stars = doc2.select("div.plainlist > ul > li");//td html'inden list öðelerine al
				if(!elements_stars.isEmpty()){//yapý liste þeklinde ise(liste boþ deðilse)
					for(Element star : elements_stars ){//list öðelerini gez
						this.starring.add(star.text());
					}		
				}					
				else {
					elements_stars = doc2.select("a");
					for(Element star : elements_stars ){//a öðelerini gez
						this.starring.add(star.text());
					}	
				}
			}else{
				index++;
				setStarring(vikiURL, index);
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		  	
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	public void setTitle(String vikiURL,String path) {
		/*
		 * title pathindeki element okunur ve title fieldýna atanýr
		 */
		Response res;
		try {	
			res = Jsoup.connect(vikiURL).execute();
			String html = res.body();
			Document doc = Jsoup.parseBodyFragment(html);
			Element element = doc.select(path).first();
			this.title = element.text();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		  	
	}
	
	//FUNCTIONS	
	@Override
	public String toString(){
		/*
		 * InfoBox nesnesinin fieldlarýný ekrana bastýran method
		 */
		String str = "\nDirector: "+this.director+"\nStarring: ";		
		for(String star : this.starring){
			str += star+", ";
		}
		return str;
	}
	public boolean isEqual(InfoBox other){
		if(!this.title.equals(other.getTitle())){
			System.out.println("Mismatch: "+this.title+"!="+other.getTitle());
			return false;
		}
			
		if(!this.director.equals(other.getDirector())){
			System.out.println("Dismatch: "+this.director+"!="+other.getDirector());
			return false;
		}
		for(String star : other.getStarring()){
			//System.out.println(star);
			if(!this.starring.contains(star)){
				System.out.println("Dismatch: '"+star+"'"+" Does Not Exist In the Starring List");
				return false;
			}
		}		
		return true;
	}
	
	//GETTER METHODLARI
	public String getTitle() {
		return title;
	}
	public String getDirector() {
		return director;
	}	
	public ArrayList<String> getStarring() {
		return starring;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * private String imdbRate;
	 * private String tomatoRate;
	 * private ArrayList<String> genre = new ArrayList<String>();
	 */
	
	/*public ArrayList<String> getGenre() {
		return genre;
	}
	public void setGenre(ArrayList<String> genre) {
		this.genre = genre;
	}
	public String getImdbRate() {
		return imdbRate;
	}
	public void setImdbRate(String imdbRate) {
		this.imdbRate = imdbRate;
	}
	public String getTomatoRate() {
		return tomatoRate;
	}
	public void setTomatoRate(String tomatoRate) {
		this.tomatoRate = tomatoRate;
	}*/

}
