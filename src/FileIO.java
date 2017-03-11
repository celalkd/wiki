import java.io.*;
import java.util.ArrayList;
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
	
	ArrayList<String> stopWordListENG = new ArrayList<>();
	ArrayList<String> stopWordListTR = new ArrayList<>();
	//CONSTRUCTORS
	private FileIO(){
		
	}
	public static FileIO getFileIO() {
		return fileIO;
	}
	
	//FUNCTIONS
	public void createStopWordList(){
		
		//inglizce ve türkçe etkisiz kelimeleri dosyadan okuyup diziye yazan method
		String content;
		try {
			content = this.fileToString("stopWordListENG");
			String[] blocks = content.split(("\\r?\\n?\\s+"));
			for(int i=0; i<blocks.length; i++){	
				stopWordListENG.add(blocks[i]);
			}
			content = this.fileToString("stopWordListTR");
			blocks = content.split(("\\r?\\n?\\s+"));
			for(int i=0; i<blocks.length; i++){	
				stopWordListTR.add(blocks[i]);
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void openandWritetoFile() throws IOException{
		
		File file = new File("C:\\Users\\celalkd\\workspace_\\WikiMiningMaven\\resources\\movieArchieve.txt");
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
			String fileName = item.getInfoBox().getTitle();
			if(fileName.contains(":")){
				String[] parts = fileName.split(":");
				fileName = parts[0]+parts[1];
			}
			fileName = fileName+".txt";
			
			
			File file_freq = new File("C:\\Users\\celalkd\\workspace_\\WikiMiningMaven\\resources\\"+langFolder+"\\FREQ\\"+fileName);
			File file_context = new File("C:\\Users\\celalkd\\workspace_\\WikiMiningMaven\\resources\\"+langFolder+"\\CONTEXT\\"+fileName);

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
	public String fileToString(String fileName) throws IOException{
		/*
		 * dosya adýný alýp, bu dosyanýn içerðini string olarak döndüren method
		 */
		FileReader fileReader = null;
	    String content = null;
	    
	    try {
	         fileReader = new FileReader("C:\\Users\\celalkd\\workspace_\\WikiMiningMaven\\resources\\"+fileName+".txt");
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
			//System.out.println(" *404: "+URL);
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
