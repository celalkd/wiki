import java.io.*;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FileIO {
	
	/*
	 * Singleton Class
	 */
	private static FileIO  fileIO   = new FileIO();	
	private Document doc;
	Scanner scanner;
	String content;
	
	//CONSTRUCTORS
	private FileIO(){
		
	}
	public static FileIO getFileIO() {
		return fileIO;
	}
	
	//FUNCTIONS
	public void openandWritetoFile() throws IOException{
		
		File file = new File("C:\\Users\\celalkd\\workspace_\\WikiMining\\resources\\AllMovies.txt");
		file.createNewFile();
		FileWriter fos= new FileWriter(file);
		for(Movie item: Archive.getArchive().getMovieArchive()){
			fos.write(item.toString()+"\n");
		}
		fos.flush();
		fos.close();
		
	}
	public void writeWordsAndFreqsToFile(String langFolder) throws IOException{
		for(Movie item: Archive.getArchive().getMovieArchive()){
			File file_freq = new File("C:\\Users\\celalkd\\workspace_\\WikiMining\\resources\\"+langFolder+"\\FREQ\\"+item.getInfoBox().getTitle()+".txt");
			File file_context = new File("C:\\Users\\celalkd\\workspace_\\WikiMining\\resources\\"+langFolder+"\\CONTEXT\\"+item.getInfoBox().getTitle()+".txt");

			file_freq.createNewFile();
			file_context.createNewFile();
			
			FileWriter fos_freq= new FileWriter(file_freq);
			FileWriter fos_context= new FileWriter(file_context);
			if(langFolder.equals("TR")){
				for(Word w : item.getWordListTr()){
					fos_freq.write("WORD: "+w.getWord()+", FREQ: "+w.getFreq()+"\n");
				}
				fos_context.write(item.getContext_TR());
				
			}
			else if(langFolder.equals("ENG")){
				for(Word w : item.getWordListEng()){
					fos_freq.write("WORD: "+w.getWord()+", FREQ: "+w.getFreq()+"\n");
				}
				fos_context.write(item.getContext_ENG());
			}
			fos_freq.flush();
			fos_freq.close();
			
			fos_context.flush();
			fos_context.close();
		}
		
	}
	public void appendtoFile(String str) throws IOException{
		File file = new File("C:\\Users\\celalkd\\workspace_\\WikiMining\\resources\\AllMovies.txt");
		try{
			FileWriter fw = new FileWriter(file);	
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw);
		    out.println(str);
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}		
	}
	public String fileToString(String fileName) throws IOException{
		/*
		 * dosya adýný alýp, bu dosyanýn içerðini string olarak döndüren method
		 */
		FileReader fileReader = null;
	    String content = null;
	    
	    try {
	         fileReader = new FileReader("C:\\Users\\celalkd\\workspace_\\WikiMining\\resources\\"+fileName+".txt");
	         int c;	         
	         while ((c = fileReader.read()) != -1) {	        	 
	        	 if(content==null){
	        		 content = Character.toString ((char) c);
	        	 }else content += Character.toString ((char) c);
	         }
	      }finally {
	         if (fileReader != null) {
	        	 fileReader.close();
	         }	         
	    }
	    return content;
	}
	public boolean check404(String URL){
		setDoc(null);		
		try {
			setDoc(Jsoup.connect(URL).get());//404 gelirse catche düþer
		} catch (IOException e) {
			System.out.println(" *404: "+URL);
			return false;
		}	
		System.out.println(" *checked: "+URL);
    	return true;
	}
	
	//GETTER_SETTER METHODLAR
	public Document getDoc() {
		return doc;
	}
	public void setDoc(Document doc) {
		this.doc = doc;
	}
}
