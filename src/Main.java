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
		Archive archieve = Archive.getArchive();
		FileIO fileIO = FileIO.getFileIO();
		MongoDB mongoDB = MongoDB.getMongoDB();
		Neo4j neo4j = new Neo4j();
		
		
		fileIO.createStopWordList();//ENG ve TR stopwordlisteleri olu�turulur fileIO i�inde dizide saklan�r
		
		archieve.getMovies(fileIO.fileToString("top250"));//t�m movie objeleri t�m fieldlar� dolu �ekilde kaydedilir
		archieve.checkAndPrintMovies(fileIO.fileToString("top250_info"));//imdb bilgileri ile k�yaslan�p onaylan�r
		report(); fileIO.openandWritetoFile();		
		
		//archieve.writeMovieWordsToFile("TR");//t�rk�e ve inglizce kelimeler dosyaya kaydedilir(movie'nin kelime listesine)
		//archieve.writeMovieWordsToFile("ENG");
		
		//mongoDB.createAndInsertMovieDocs(archieve.getMovieArchive());//allmovies to mongo
		//mongoDB.createAndInsertContextDocs(archieve.getMovieArchive(), "ENG");//eng context to mongo
		//mongoDB.createAndInsertContextDocs(archieve.getMovieArchive(), "TR");//tr context to mongo
		//archieve.createMovieWordsStore();
		
		neo4j.createDirectedByRelationship(archieve.getMovieArchive());
		neo4j.createFilmedRelationship(archieve.getMovieArchive());
		
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
