
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) throws IOException {
		
		DatabaseLayer db = new DatabaseLayer();
		db.build();					
				
		ArrayList<String> starring = new ArrayList<>();
		ArrayList<String> genre = new ArrayList<>();		
		starring.add("Al Pacino");
		starring.add("Diane Keaton");		
		genre.add("Crime");
		genre.add("Drama");		
		
		db.mongoDB.init("moviesCollection");
		db.redis.query("godfather", "corleone", "niro");
		db.mongoDB.query_with_tags(null, "1973","1975", starring, genre, 8.5);		
		db.mongoDB.query_with_title("The Godfather Part II");	
		
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
