
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
		 * dosya i�eri�ini �evirdi�imiz stringi | 'a g�re ay�r�p fieldlar� doldurdu�umuz
		 * ve sonra movieArchieve'e ekledi�imiz method
		 * burada �nce wikiLink al�n�r sonra bu linkten aktif ve do�ru wikiLink bulunur
		 * bu aktif wikiLink'ten vikiLink_TR ��kar�l�r. yine bu aktif wikiLink'ten infoBox
		 * de�erleri al�n�r. Y�l, id de bulunduktan sonra
		 * movie nesnesine bu de�erler verilir(constructor �a��rd���m�zda bu de�erler veriliyor)
		 * 
		 */
		String[] blocks = content.split("\\||\\n");	
		for(int i=0; i<blocks.length; i++){	
			if(i%5 == 1){//her bir yeni title'da
				String title = null;				
				Movie movie = new Movie();				
				String linkExtension = blocks[i];				
				String[] titleParts = linkExtension.split("_");
				for(int j=0; j<titleParts.length; j++){
					if(title==null){
						title=titleParts[j];
		        	 }else title += " "+titleParts[j];
				}
				/*
				 * title olu�turuldu, burdan okudu�umuz title sadece link bulmak i�in
				 * as�l title'� infobox'tan �ekip movie.infoBox nesnesinde saklayaca��z
				 */
				String year = blocks[i+1];//link extensiondan bir sonraki blok year blo�u				
				movie.setYear(year); //year set edildi
				movie.setId(movieArchive.size()+1);							
				movie.setWikiURL_EN("https://en.wikipedia.org/wiki/"+linkExtension);//ba�lang�� EN wiki link
				movie.setActiveWikiLink();
				movie.setActiveVikiURL();
				movie.setInfoBox(new InfoBox(movie.getWikiURL_EN()));
				movieArchive.add(movie);//t�m filmler ar�ivlendi, indexleri s�ral�
			}
		}
	}
	public void checkAndPrintMovies(String content) throws IOException{
		System.out.println("------------------------------------------------------------------------");
		/*
		 * imdb'den al�nm�� bilgileri i�eren dosya sat�r sat�r ayr�l�r
		 * top250 ve top250_info dosyalar�nda filmlerin s�ras�n�n ayn� oldu�u d���n�rsek
		 * ar�ivdeki index ile sat�r indexi e�it olan filmlerin ayn� olmas�n� bekleriz
		 * sat�r atlamalar� int id ile String dizisinde gezerek yap�yoruz
		 * her bir sat�r� | delimiter�na g�re bloklara ay�r�p datalar� al�yoruz
		 * daha sonra bu datalar� bir infobox ��esine at�p ar�ivdeki filmin infobox�yla k�yasl�yoruz
		 * sonuca g�re filmin onaylanmas�na karar veriyoruz
		 * onay bilgisi her filmin kendisi taraf�ndan saklanacak
		 */
		String[] movieRowsIMDB = content.split("\\n");
		Integer id=0;
		for(Movie movie : this.movieArchive){
			String[] dataColumns = movieRowsIMDB[id].split("\\|");
			InfoBox comparisonInfoBox = new InfoBox();
			
			for(int i=0; i<dataColumns.length; i++){			
				switch(i%7){
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
						String[] actors = dataColumns[i].split(",");
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
				String textBody = movie.findContext(movie.getVikiURL_TR(),"TR");
				
				movie.splitContext(textBody, movie.getWordListTr());
			}
			else if(language.equals("ENG")){
				String textBody = movie.findContext(movie.getWikiURL_EN(),"ENG");
				movie.splitContext(textBody, movie.getWordListEng());
			}
		}
		//en son fileIO s�n�f� methodu ile her br movienin kelime listesi d�k�mana yaz�l�r
		FileIO.getFileIO().writeWordsAndFreqsToFile(language);
	}
}