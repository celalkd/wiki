
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
		 * content stringi film künyesini tutan dosyanýn içeriðini temsil ediyor
		 * bu dosyada künye içerikleri | ile ayrýlmýþ
		 * bunlarý tek tek ayýrýp film adýný çekeceðiz
		 * film adýyla da aktif wikipedia(eng) linkini arayacaðýz
		 * eng wikipedia linkini bulduktan sonra da tr vikipedi linkini var mý bakacaðýz
		 * 
		 * linkler elde ediltikten sonra(eng olan asýl kaynak kabul edildi) sayfadan InfoBox'a eriþilir
		 * InfoBox'tan elde edilen deðerler movie nesnesine verilir(Movie constructor bu iþi yapar)
		 * 
		 */
		
		String[] blocks = content.split("\\||\\n");	
		for(int i=0; i<blocks.length; i++){	
			if(i%5 == 1){//her bir yeni title'da(5 adet blok var | ile ayrýlan)
								
				Movie movie = new Movie();
				//filmin title'ýný tutan blok wikipedia linkinin sonuna eklenecek olan uzantý olacak
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
				
				String year = blocks[i+1];//link extensiondan bir sonraki blok year bloðu				
				movie.setYear(year); //year set edildi
				movie.setId(movieArchive.size());	
				
				//baþlangýç ENG wiki linki
				movie.setWikiURL_EN("https://en.wikipedia.org/wiki/"+linkExtension);
				//aktif olan film linki bulunur
				movie.setActiveWikiLink();
				//aktif linkin türkçe sayfasý bulunur
				movie.setActiveVikiURL();
				
				//infoBox constructorýnda linkin infoBox elementine gidilir
				//Bu elementten filmin title'ý, yönetmeni ve oyuncularý alýnýr.
				//yýlý zaten yola çýktýðýmýz dosyada yazdýðý için InfoBox'tan okumamýza gerek yok
				InfoBox infoBox = new InfoBox(movie.getWikiURL_EN());
				movie.setInfoBox(infoBox);
				
				//tüm fieldlarý doldurulan movie nesnesi movie listesine(movieArhive) eklenir
				movieArchive.add(movie);//tüm filmler arþivlendi, indexleri sýralý
			}
		}
	}
	public void checkAndPrintMovies(String content) throws IOException{
		System.out.println("------------------------------------------------------------------------");
		/*
		 * imdb'den alýnmýþ bilgileri içeren dosya satýr satýr ayrýlýr(content bunu içeriyor)
		 * 
		 * top250 ve top250_info dosyalarýnda filmlerin sýrasýnýn ayný olduðu düþünürsek
		 * arþivdeki index ile satýr indexi eþit olan filmlerin ayný olmasýný bekleriz
		 * 
		 * satýr atlamalarý id integerý ile String dizisinde gezerek yapýyoruz
		 * 
		 * her bir satýrý | delimiterýna göre bloklara ayýrýp datalarý alýyoruz
		 * 
		 * daha sonra bu datalarý bir infobox öðesine atýp arþivdeki filmin infoboxýyla kýyaslýyoruz
		 * 
		 * sonuca göre filmin onaylanmasýna karar veriyoruz
		 * 
		 * onay bilgisi her filmin kendisi tarafýndan saklanacak
		 */
		
		String[] movieRowsIMDB = content.split("\\n");//satýrlar enter'a göre ayrýlýyor
		Integer id=0;
		for(Movie movie : this.movieArchive){
			String[] dataColumns = movieRowsIMDB[id].split("\\|");//satýrlardaki datalar | ile ayrýlýyor
			InfoBox comparisonInfoBox = new InfoBox();
			
			for(int i=0; i<dataColumns.length; i++){			
				switch(i%7){ //her satýrda 7 adet data bloðu var
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
						String[] actors = dataColumns[i].split(",");//her satýrýn 3.data bloðundaki oyuncular , ile ayrýlýyor
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
			
			//imdb bilgileri ile vikipedia bilgileri uyuþuyor mu
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
				String textBody = movie.findContext(movie.getVikiURL_TR(),language);				
				movie.splitContext(textBody, movie.getWordListTr(), language);
				//splitcontext fonksiyonunda þu anda dil kontrolü eksik
				//dil kontrolü yapýlýr stop-word list dilinin belirlenmesi lazým
				//þu anda sadece ingilizce olan stop-wordlerin kontrolünü yapýyor
			}
			else if(language.equals("ENG")){
				String textBody = movie.findContext(movie.getWikiURL_EN(),language);
				movie.splitContext(textBody, movie.getWordListEng(),language);
			}
		}
		//en son fileIO sýnýfý methodu ile her br movienin kelime listesi dökümana yazýlýr
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