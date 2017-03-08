import static org.neo4j.driver.v1.Values.parameters;

import java.beans.Statement;
import java.io.IOException;
import java.util.ArrayList;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.*;



public class Main {

	public static void main(String[] args) throws IOException {
		/*
		 * Arhieve ve FileIO singleton olduðu için yeni nesne yaratýyorum
		 * sýnýfýn içinde olan static nesneyi alýp kullanýyoruz
		 */
		/*Archive archieve = Archive.getArchive();
		FileIO fileIO = FileIO.getFileIO();
		MongoDB mongoDB = MongoDB.getMongoDB();
		Neo4j neo4j = new Neo4j();
		
		
		fileIO.createStopWordList();//ENG ve TR stopwordlisteleri oluþturulur fileIO içinde dizide saklanýr
		
		archieve.getMovies(fileIO.fileToString("top250_10film"));//tüm movie objeleri tüm fieldlarý dolu þekilde kaydedilir
		archieve.checkMovies(fileIO.fileToString("top250_info_10film"));//imdb bilgileri ile kýyaslanýp onaylanýr
		report();
			    
		
		fileIO.openandWritetoFile();		
		
		archieve.writeWordsToFile("TR");//türkçe ve inglizce kelimeler dosyaya kaydedilir(movie'nin kelime listesine)
		archieve.writeWordsToFile("ENG");
		
		
		//mongoDB.createAndInsertContextDocs(archieve.getMovieArchive(), "ENG");//eng context to mongo
		//mongoDB.createAndInsertContextDocs(archieve.getMovieArchive(), "TR");//tr context to mongo
		
		mongoDB.createAndInsertMovieDocs(archieve.getMovieArchive());//mongodb		
		archieve.createWordRedis();//redis
		//neo4j.createGraph(archieve.getMovieArchive());//neo4j
		*/
		
		ArrayList<String> starring = new ArrayList<>();
		ArrayList<String> genre = new ArrayList<>();
		
		
		starring.add("Al Pacino");
		starring.add("Diane Keaton");
		starring.add("Robert De Niro");
		//starring.add("Marlon Brando");
		//starring.add("Morgan Freeman");
		//starring.add("Tim Robbins");
		
		genre.add("Crime");
		genre.add("Drama");
		//genre.add("Action");
		//genre.add("History");
		
		MongoDB mongoDB = MongoDB.getMongoDB();
		mongoDB.query_with_tags(null, "1950","2010", starring, genre, 8.5);
		mongoDB.query_with_title("Pulp Fiction");
	}
		
	
	public static void report(){
		Archive archieve = Archive.getArchive();
		ArrayList<Movie> movieArchive = archieve.getMovieArchive();
		String string = 
				"\nIncelenen Film Sayýsý= "+movieArchive.size()
				+ "\nIngilizce Kaynak Bulunan Film Sayýsý= "+(int)(movieArchive.size()-Movie.noAnyLangSource)
				+ "\nIngilizce ve Türkçe Kaynak Bulunan Film sayýsý= "+(int)Movie.success
				+ "\nTR Link Baþarý Oraný= %"+(Movie.success*100)/movieArchive.size()
				+ "\nTR Link Onaylanma Oraný= %"+(Movie.verifySuccess*100)/new Movie().getSuccess();
		System.out.println(string);
	}
	
}
