
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) throws IOException {
		
		DatabaseLayer db = new DatabaseLayer();
		//db.build();					
				
		ArrayList<String> starring = new ArrayList<>();
		ArrayList<String> genre = new ArrayList<>();	
		String director ="Christopher Nolan";
		String yearMin = "2000";
		String yearMax = null;
		starring.add("Leonardo DiCaprio");		
		genre.add("Action");
		double rating = 8.5;
		
		
		//db.redis.query("run", "forrest");
		//db.mongoDB.query_with_tags(director, yearMin, yearMax, starring, genre, rating);
		db.neo4j.deneme("0");
		
		//redis query'den key listesi d�ner
		//mongodb sorgusuna bu key'ler OR'lanarak append edilir
		//daha specific bir arama yap�lm�� olur
		//mesela "ring", "frodo" 3 filimde bulunu ben 2000'den son olan diye k�s�tlama koyup bulabilirim 
		
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
