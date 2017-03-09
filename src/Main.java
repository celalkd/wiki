
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
				"\nIncelenen Film Say�s�= "+movieArchive.size()
				+ "\nIngilizce Kaynak Bulunan Film Say�s�= "+(int)(movieArchive.size()-Movie.noAnyLangSource)
				+ "\nIngilizce ve T�rk�e Kaynak Bulunan Film say�s�= "+(int)Movie.success
				+ "\nTR Link Ba�ar� Oran�= %"+(Movie.success*100)/movieArchive.size()
				+ "\nTR Link Onaylanma Oran�= %"+(Movie.verifySuccess*100)/new Movie().getSuccess();
		System.out.println(string);
	}
	
}
