import static org.neo4j.driver.v1.Values.parameters;

import java.beans.Statement;
import java.io.IOException;
import java.util.ArrayList;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.*;



public class Main {

	public static void main(String[] args) throws IOException {
		/*
		 * Arhieve ve FileIO singleton oldu�u i�in yeni nesne yarat�yorum
		 * s�n�f�n i�inde olan static nesneyi al�p kullan�yoruz
		 */
		/*Archive archieve = Archive.getArchive();
		FileIO fileIO = FileIO.getFileIO();
		MongoDB mongoDB = MongoDB.getMongoDB();
		Neo4j neo4j = new Neo4j();
		
		
		fileIO.createStopWordList();//ENG ve TR stopwordlisteleri olu�turulur fileIO i�inde dizide saklan�r
		
		archieve.getMovies(fileIO.fileToString("top250_10film"));//t�m movie objeleri t�m fieldlar� dolu �ekilde kaydedilir
		archieve.checkMovies(fileIO.fileToString("top250_info_10film"));//imdb bilgileri ile k�yaslan�p onaylan�r
		report();
			    
		
		fileIO.openandWritetoFile();		
		
		archieve.writeWordsToFile("TR");//t�rk�e ve inglizce kelimeler dosyaya kaydedilir(movie'nin kelime listesine)
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
				"\nIncelenen Film Say�s�= "+movieArchive.size()
				+ "\nIngilizce Kaynak Bulunan Film Say�s�= "+(int)(movieArchive.size()-Movie.noAnyLangSource)
				+ "\nIngilizce ve T�rk�e Kaynak Bulunan Film say�s�= "+(int)Movie.success
				+ "\nTR Link Ba�ar� Oran�= %"+(Movie.success*100)/movieArchive.size()
				+ "\nTR Link Onaylanma Oran�= %"+(Movie.verifySuccess*100)/new Movie().getSuccess();
		System.out.println(string);
	}
	
}
