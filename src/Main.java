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
		
		archieve.getMovies(fileIO.fileToString("top250"));
		archieve.checkAndPrintMovies(fileIO.fileToString("top250_info"));
		report(); fileIO.openandWritetoFile();		
		
		archieve.writeMovieWordsToFile("TR");
		archieve.writeMovieWordsToFile("ENG");
		
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
