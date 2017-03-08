
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
				
		String[] blocks = content.split("\\||\\n");	
		for(int i=0; i<blocks.length; i++){	
			if(i%5 == 1){//her bir yeni title'da(5 adet blok var | ile ayrýlan)
								
				Movie movie = new Movie();//filmin title'ýný tutan blok wikipedia linkinin sonuna eklenecek olan uzantý olacak
				String linkExtension = blocks[i];	
				String year = blocks[i+1];//link extensiondan bir sonraki blok year bloðu		
				
				movie.setYear(Integer.parseInt(year)); //year set edildi
				movie.setId(movieArchive.size());	
				
				
				movie.setWikiURL_EN("https://en.wikipedia.org/wiki/"+linkExtension);//baþlangýç ENG wiki linki
				movie.setActiveWikiLink();//aktif olan film linki bulunur
				movie.setActiveVikiURL();//aktif linkin türkçe sayfasý bulunur
				
				InfoBox infoBox = new InfoBox(movie.getWikiURL_EN());
				movie.setInfoBox(infoBox);				
				
				movieArchive.add(movie);//tüm filmler obje olarak listeye atýldý
			}
		}
	}
	public void checkMovies(String content) throws IOException{
				
		Integer id_index=0;
		String[] movieRowsIMDB = content.split("\\n");//satýrlar enter karakterine göre ayrýlýyor
		
		for(Movie movie : this.movieArchive){
			String[] dataColumns = movieRowsIMDB[id_index].split("\\|");//satýrlardaki datalar | karakteri ile ayrýlýyor
			InfoBox comparisonInfoBox = new InfoBox();
			
			for(int i=0; i<dataColumns.length; i++){			
				switch(i%7){ //her satýrda | ile ayrýlmýþ 7 adet data bloðu var
					case 1: 
						String title = null;					
						String[] titleParts = dataColumns[i].split("_");
						for(int j=0; j<titleParts.length; j++){
							if(title==null){
								title=titleParts[j];
							}else title += " "+titleParts[j];
						}
						comparisonInfoBox.setTitle(title);
						break;
					case 2:
						String director = dataColumns[i];
						comparisonInfoBox.setDirector(director);
						break;
					case 3:
						String[] actors = dataColumns[i].split(",");//her satýrýn 3.data bloðundaki oyuncular , ile ayrýlýyor
						ArrayList<String> starring = new ArrayList<String>();
						for(int j=0; j< actors.length; j++){
							if(j!=0){//space'i almamak için substring çýkartýyoruz ilk actorden sonra
								actors[j]=actors[j].substring(1, actors[j].length());//(string - 0.char)
								starring.add(actors[j]);								
							}
							else starring.add(actors[j]);
						}
						comparisonInfoBox.setStarring(starring);
						break;
					case 4:
						String[] genres = dataColumns[i].split(",");//her satýrýn 4.data bloðundaki genre , ile ayrýlýyor
						ArrayList<String> genreList = new ArrayList<String>();
						for(int k=0; k< genres.length; k++){
							if(k!=0){//space'i almamak için substring çýkartýyoruz ilk genre'dan sonra
								genres[k]=genres[k].substring(1, genres[k].length());
								genreList.add(genres[k]);								
							}
							else genreList.add(genres[k]);
						}
						movie.setGenre(genreList);
						break;
					case 5:
						double rating = Double.parseDouble(dataColumns[i]);
						movie.setRating(rating);
				}
			}
			
			boolean verified = movie.getInfoBox().isEqual(comparisonInfoBox);//yönetmen, oyucnular, title karþýlaþtýrmasý
			movie.setVerified(verified);			
			if(verified)
				movie.setVerifySuccess(movie.getVerifySuccess()+1);	
			
			System.out.println(movie+"\nVerified: "+movie.getVerified());			
			id_index++;
		}
		
	}
	public void writeWordsToFile(String language) throws IOException{
		//dil seçeneðine göre ayný iþlem farklý kelime listeleri üzerinde yapýlýr
		for(Movie movie : this.getMovieArchive()){
			if(language.equals("TR")){
				String textBody = movie.findContext(movie.getVikiURL_TR(),language);				
				movie.splitContext(textBody, movie.getWordListTr(), language);				
			}
			else if(language.equals("ENG")){
				String textBody = movie.findContext(movie.getWikiURL_EN(),language);
				movie.splitContext(textBody, movie.getWordListEng(),language);
			}
		}
		FileIO.getFileIO().writeWordsAndFreqsToFile(language);//arþiv üzerinden tüm film nesneleri için
	}
	public void createWordRedis() throws IOException{
		
		Redis redis = new Redis();
		
		for(Movie movie : this.getMovieArchive()){			
			
			System.out.println(movie.getInfoBox().getTitle()+" Redis");
			
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