
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) throws IOException {
		
		//DatabaseLayer db = new DatabaseLayer();
		//db.build();					
				
//		ArrayList<String> starring = new ArrayList<>();
//		ArrayList<String> genre = new ArrayList<>();	
//		String director ="Christopher Nolan";
//		String yearMin = "2000";
//		String yearMax = null;
//		starring.add("Leonardo DiCaprio");		
//		genre.add("Action");
//		double rating = 8.5;
//		
//		
//		//db.redis.query("run", "forrest");
//		//db.mongoDB.query_with_tags(director, yearMin, yearMax, starring, genre, rating);
//		System.out.println("0*********");
//		db.neo4j.recommend("0");
//		System.out.println("1*********");
//		db.neo4j.recommend("1");
//		System.out.println("2*********");
//		db.neo4j.recommend("2");

		
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
