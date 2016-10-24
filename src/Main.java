import java.io.IOException;



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
		
		System.out.println(
				"\nIncelenen Film Say�s�= "+archieve.movieArchive.size()
				+ "\nIngilizce Kaynak Bulunan Film Say�s�= "+(archieve.movieArchive.size()-(int)new Movie().getNoAnyLangSource())
				+ "\nIngilizce ve T�rk�e Kaynak Bulunan Film say�s�= "+(int)new Movie().getSuccess()
				+ "\nTR Link Ba�ar� Oran�= %"+(new Movie().getSuccess()*100)/archieve.movieArchive.size()
				+ "\nTR Link Onaylanma Oran�= %"+(new Movie().getVerifySuccess()*100)/new Movie().getSuccess());
		
		
	}
	
}
