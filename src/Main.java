import java.io.IOException;
import java.util.ArrayList;



public class Main {

	public static void main(String[] args) throws IOException {
		/*
		 * Arhieve ve FileIO singleton olduðu için yeni nesne yaratýyorum
		 * sýnýfýn içinde olan static nesneyi alýp kullanýyoruz
		 */
		Archive archieve = Archive.getArchive();
		FileIO fileIO = FileIO.getFileIO();
		MongoDB mongoDB = MongoDB.getMongoDB();
		
		fileIO.createStopWordList();//stopwordlisteleri oluþturulur
		
		archieve.getMovies(fileIO.fileToString("top250"));//tüm movie objeleri tüm fieldlarý dolu þekilde kaydedilir
		archieve.checkAndPrintMovies(fileIO.fileToString("top250_info"));//imdb bilgileri ile kýyaslanýp onaylanýr
		report(); fileIO.openandWritetoFile();		
		
		archieve.writeMovieWordsToFile("TR");//türkçe ve inglizce kelimeler dosyaya kaydedilir(movie'nin kelime listesine)
		archieve.writeMovieWordsToFile("ENG");
		
		mongoDB.createAndInsertMovieDocs(archieve.getMovieArchive());//allmovies to mongo
		mongoDB.createAndInsertContextDocs(archieve.getMovieArchive(), "ENG");//eng context to mongo
		mongoDB.createAndInsertContextDocs(archieve.getMovieArchive(), "TR");//tr context to mongo
		
		
		
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
