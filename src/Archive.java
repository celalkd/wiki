
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Archive {
	
	/*
	 * Singleton Class
	 */
	private static Archive  archive   = new Archive();
	private ArrayList<Movie> movieArchive = new ArrayList<Movie>();
	
	//CONSTRUCTORS
	private Archive(){
		
	}	
	public static Archive getArchive() {
		return archive;
	}
	
	//GETTER_SETTER METHODS
	public void setMovieArchive(ArrayList<Movie> movieArchive) {
		this.movieArchive = movieArchive;
	}
	public ArrayList<Movie> getMovieArchive() {
		return this.movieArchive;
	}
	
	//FUNCTIONS
	
	public void getMovies(String content) throws IOException{
		/*
		 * 
		 * content stringi film k�nyesini tutan dosyan�n i�eri�ini temsil ediyor
		 * bu dosyada k�nye i�erikleri | ile ayr�lm��
		 * bunlar� tek tek ay�r�p film ad�n� �ekece�iz
		 * film ad�yla da aktif wikipedia(eng) linkini arayaca��z
		 * eng wikipedia linkini bulduktan sonra da tr vikipedi linkini var m� bakaca��z
		 * 
		 * linkler elde ediltikten sonra(eng olan as�l kaynak kabul edildi) sayfadan InfoBox'a eri�ilir
		 * InfoBox'tan elde edilen de�erler movie nesnesine verilir(Movie constructor bu i�i yapar)
		 * 
		 */
		
		String[] blocks = content.split("\\||\\n");	
		for(int i=0; i<blocks.length; i++){	
			if(i%5 == 1){//her bir yeni title'da(5 adet blok var | ile ayr�lan)
								
				Movie movie = new Movie();
				//filmin title'�n� tutan blok wikipedia linkinin sonuna eklenecek olan uzant� olacak
				String linkExtension = blocks[i];				
				String[] titleParts = linkExtension.split("_");
				
				/*
				 * gerek olmayabilir
				 * 
				 * String title = null;
				for(int j=0; j<titleParts.length; j++){
					if(title==null){
						title=titleParts[j];
		        	 }else title += " "+titleParts[j];
				}
				*
				*/
				
				String year = blocks[i+1];//link extensiondan bir sonraki blok year blo�u				
				movie.setYear(year); //year set edildi
				movie.setId(movieArchive.size());	
				
				//ba�lang�� ENG wiki linki
				movie.setWikiURL_EN("https://en.wikipedia.org/wiki/"+linkExtension);
				//aktif olan film linki bulunur
				movie.setActiveWikiLink();
				//aktif linkin t�rk�e sayfas� bulunur
				movie.setActiveVikiURL();
				
				//infoBox constructor�nda linkin infoBox elementine gidilir
				//Bu elementten filmin title'�, y�netmeni ve oyuncular� al�n�r.
				//y�l� zaten yola ��kt���m�z dosyada yazd��� i�in InfoBox'tan okumam�za gerek yok
				InfoBox infoBox = new InfoBox(movie.getWikiURL_EN());
				movie.setInfoBox(infoBox);
				
				//t�m fieldlar� doldurulan movie nesnesi movie listesine(movieArhive) eklenir
				movieArchive.add(movie);//t�m filmler ar�ivlendi, indexleri s�ral�
			}
		}
	}
	public void checkAndPrintMovies(String content) throws IOException{
		System.out.println("------------------------------------------------------------------------");
		/*
		 * imdb'den al�nm�� bilgileri i�eren dosya sat�r sat�r ayr�l�r(content bunu i�eriyor)
		 * 
		 * top250 ve top250_info dosyalar�nda filmlerin s�ras�n�n ayn� oldu�u d���n�rsek
		 * ar�ivdeki index ile sat�r indexi e�it olan filmlerin ayn� olmas�n� bekleriz
		 * 
		 * sat�r atlamalar� id integer� ile String dizisinde gezerek yap�yoruz
		 * 
		 * her bir sat�r� | delimiter�na g�re bloklara ay�r�p datalar� al�yoruz
		 * 
		 * daha sonra bu datalar� bir infobox ��esine at�p ar�ivdeki filmin infobox�yla k�yasl�yoruz
		 * 
		 * sonuca g�re filmin onaylanmas�na karar veriyoruz
		 * 
		 * onay bilgisi her filmin kendisi taraf�ndan saklanacak
		 */
		
		String[] movieRowsIMDB = content.split("\\n");//sat�rlar enter'a g�re ayr�l�yor
		Integer id=0;
		for(Movie movie : this.movieArchive){
			String[] dataColumns = movieRowsIMDB[id].split("\\|");//sat�rlardaki datalar | ile ayr�l�yor
			InfoBox comparisonInfoBox = new InfoBox();
			
			for(int i=0; i<dataColumns.length; i++){			
				switch(i%7){ //her sat�rda 7 adet data blo�u var
					case 1: 
						String title = null;					
						String[] titleParts = dataColumns[i].split("_");
						for(int j=0; j<titleParts.length; j++){
							if(title==null){
								title=titleParts[j];
							}else title += " "+titleParts[j];
						}
						comparisonInfoBox.setTitle(title);
						//System.out.println("\n"+title);
						break;
					case 2:
						String director = dataColumns[i];
						comparisonInfoBox.setDirector(director);
						//System.out.println(director);
						break;
					case 3:
						String[] actors = dataColumns[i].split(",");//her sat�r�n 3.data blo�undaki oyuncular , ile ayr�l�yor
						ArrayList<String> starring = new ArrayList<String>();
						for(int j=0; j< actors.length; j++){
							//actor ekle
							if(j!=0){//space'i almamak i�in substring ��kart�yoruz ilk actorden sonra
								actors[j]=actors[j].substring(1, actors[j].length());
								starring.add(actors[j]);								
							}
							else starring.add(actors[j]);
							//System.out.println(actors[j]);
						}
						comparisonInfoBox.setStarring(starring);
						break;
				}
			}
			System.out.println(movie);
			
			//imdb bilgileri ile vikipedia bilgileri uyu�uyor mu
			boolean verified = movie.getInfoBox().isEqual(comparisonInfoBox);
			movie.setVerified(verified);
			
			System.out.println("Verified: "+movie.getVerified());
			if(verified)
				movie.setVerifySuccess(movie.getVerifySuccess()+1);
			
			id++;
		}
		
	}
	public void writeMovieWordsToFile(String language) throws IOException{
		
		// movieArchive listesindeki her bir movienin body contextini al�n�t
		//daha sonra context kelimelerini ayr��t�r�l�r
		//ayr��t�r�lan kelimeler movie s�n�fnfa bir listeyde saklan�r(splitContext methodu bunu yapar)
		//daha sonra fileIo s�n�f�n�n
		for(Movie movie : this.getMovieArchive()){
			if(language.equals("TR")){
				String textBody = movie.findContext(movie.getVikiURL_TR(),language);				
				movie.splitContext(textBody, movie.getWordListTr(), language);
				//splitcontext fonksiyonunda �u anda dil kontrol� eksik
				//dil kontrol� yap�l�r stop-word list dilinin belirlenmesi laz�m
				//�u anda sadece ingilizce olan stop-wordlerin kontrol�n� yap�yor
			}
			else if(language.equals("ENG")){
				String textBody = movie.findContext(movie.getWikiURL_EN(),language);
				movie.splitContext(textBody, movie.getWordListEng(),language);
			}
		}
		//en son fileIO s�n�f� methodu ile her br movienin kelime listesi d�k�mana yaz�l�r
		FileIO.getFileIO().writeWordsAndFreqsToFile(language);
	}
	public void createMovieWordsStore() throws IOException{
		
		Redis redis = new Redis();
		
		for(Movie movie : this.getMovieArchive()){			
			
			redis.jedis.select(0);
			String textBody = movie.findContext(movie.getVikiURL_TR(),"TR");				
			movie.splitContext(textBody, movie.getWordListTr(), "TR");
			redis.createWordFreqStore(movie, movie.getWordListTr());	
			
			redis.jedis.select(1);
			textBody = movie.findContext(movie.getWikiURL_EN(),"ENG");
			movie.splitContext(textBody, movie.getWordListEng(),"ENG");	
			redis.createWordFreqStore(movie, movie.getWordListEng());
		}		
	}
	
	
}