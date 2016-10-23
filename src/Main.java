import java.io.IOException;



public class Main {

	public static void main(String[] args) throws IOException {
		//github second oommit check	
		//another change for github
		/*
		 * Arhieve ve FileIO singleton oldu�u i�in yeni nesne yarat�yorum
		 * s�n�f�n i�inde olan static nesneyi al�p kullan�yoruz
		 */
		Archive archieve = Archive.getArchive();
		FileIO fileIO = FileIO.getFileIO();
		
		archieve.collect(fileIO.fileToString("top250"));
		archieve.list();
		System.out.println(
				"\nIncelenen Film Say�s�= "+archieve.movieArchive.size()
				+ "\nIngilizce Kaynak Bulunan Film Say�s�= "+(archieve.movieArchive.size()-(int)new Movie().getNoAnyLangSource())
				+ "\nIngilizce ve T�rk�e Kaynak Bulunan Film say�s�= "+(int)new Movie().getSuccess()
				+ "\nBa�ar� Oran�= %"+(new Movie().getSuccess()*100)/archieve.movieArchive.size());
		
		//this is the 3rd commit for github
		//last commit fot github(diff color)
		//InfoBox i = new InfoBox("https://en.wikipedia.org/wiki/Pulp_Fiction");
			
		/*Archive archieve = Archive.getArchive();
		FileIO fileIO = FileIO.getFileIO();
		archieve.collectInfoBox(fileIO.fileToString("top250_info"));*/
		
	}
	
}
