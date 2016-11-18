
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
		 * dosya içeriðini çevirdiðimiz stringi | 'a göre ayýrýp fieldlarý doldurduðumuz
		 * ve sonra movieArchieve'e eklediðimiz method
		 * burada önce wikiLink alýnýr sonra bu linkten aktif ve doðru wikiLink bulunur
		 * bu aktif wikiLink'ten vikiLink_TR çýkarýlýr. yine bu aktif wikiLink'ten infoBox
		 * deðerleri alýnýr. Yýl, id de bulunduktan sonra
		 * movie nesnesine bu deðerler verilir(constructor çaðýrdýðýmýzda bu deðerler veriliyor)
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
				 * title oluþturuldu, burdan okuduðumuz title sadece link bulmak için
				 * asýl title'ý infobox'tan çekip movie.infoBox nesnesinde saklayacaðýz
				 */
				String year = blocks[i+1];//link extensiondan bir sonraki blok year bloðu				
				movie.setYear(year); //year set edildi
				movie.setId(movieArchive.size()+1);							
				movie.setWikiURL_EN("https://en.wikipedia.org/wiki/"+linkExtension);//baþlangýç EN wiki link
				movie.setActiveWikiLink();
				movie.setActiveVikiURL();
				movie.setInfoBox(new InfoBox(movie.getWikiURL_EN()));
				movieArchive.add(movie);//tüm filmler arþivlendi, indexleri sýralý
			}
		}
	}
	public void checkAndPrintMovies(String content) throws IOException{
		System.out.println("------------------------------------------------------------------------");
		/*
		 * imdb'den alýnmýþ bilgileri içeren dosya satýr satýr ayrýlýr
		 * top250 ve top250_info dosyalarýnda filmlerin sýrasýnýn ayný olduðu düþünürsek
		 * arþivdeki index ile satýr indexi eþit olan filmlerin ayný olmasýný bekleriz
		 * satýr atlamalarý int id ile String dizisinde gezerek yapýyoruz
		 * her bir satýrý | delimiterýna göre bloklara ayýrýp datalarý alýyoruz
		 * daha sonra bu datalarý bir infobox öðesine atýp arþivdeki filmin infoboxýyla kýyaslýyoruz
		 * sonuca göre filmin onaylanmasýna karar veriyoruz
		 * onay bilgisi her filmin kendisi tarafýndan saklanacak
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
							if(j!=0){//space'i almamak için substring çýkartýyoruz ilk actorden sonra
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
		
		// movieArchive listesindeki her bir movienin body contextini alýnýt
		//daha sonra context kelimelerini ayrýþtýrýlýr
		//ayrýþtýrýlan kelimeler movie sýnýfnfa bir listeyde saklanýr(splitContext methodu bunu yapar)
		//daha sonra fileIo sýnýfýnýn
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
		//en son fileIO sýnýfý methodu ile her br movienin kelime listesi dökümana yazýlýr
		FileIO.getFileIO().writeWordsAndFreqsToFile(language);
	}
}