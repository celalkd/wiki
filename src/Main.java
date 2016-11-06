import java.io.IOException;
import java.util.ArrayList;



public class Main {

	public static void main(String[] args) throws IOException {
		/*
		 * Arhieve ve FileIO singleton oldu�u i�in yeni nesne yarat�yorum
		 * s�n�f�n i�inde olan static nesneyi al�p kullan�yoruz
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
				"\nIncelenen Film Say�s�= "+movieArchive.size()
				+ "\nIngilizce Kaynak Bulunan Film Say�s�= "+(int)(movieArchive.size()-Movie.noAnyLangSource)
				+ "\nIngilizce ve T�rk�e Kaynak Bulunan Film say�s�= "+(int)Movie.success
				+ "\nTR Link Ba�ar� Oran�= %"+(Movie.success*100)/movieArchive.size()
				+ "\nTR Link Onaylanma Oran�= %"+(Movie.verifySuccess*100)/new Movie().getSuccess();
		System.out.println(string);
	}
	
}
