import java.util.ArrayList;
import com.mongodb.*;

public class MongoDB {
	private static MongoDB  mongodb   = new MongoDB();
	private ArrayList<BasicDBObject> docList ;
	private int id;
	private MongoClient mongoClient ;	 
	private DB db ;
	private DBCollection collection;	
	
	//CONSTRUCTORS
	private MongoDB(){
		
	}	
	public static MongoDB getMongoDB() {
		return mongodb;
	}
	
	public void createAndInsertMovieDocs(ArrayList<Movie> movieArchieve){
		//allmovies dok�man�n� mongodb'ye formatlamak amac�yla yaz�lan method
		init("moviesCollection");		//kullan�lacak collection init g�nderilir	
		
		
		for(Movie m : movieArchieve){//her bir movie i�in bo� bir mondogb doc yarat�l�r
			BasicDBObject movieDoc = new BasicDBObject();
			ArrayList<String> starringList = new ArrayList<String>();
			for(String actor : m.getInfoBox().getStarring()){
				starringList.add(actor);
			}
			movieDoc.append("_id",id)
			 .append("title", m.getInfoBox().getTitle())
			 .append("director", m.getInfoBox().getDirector())
			 .append("year", m.getYear())
			 .append("wikiURL", m.getWikiURL_EN() )
			 .append("vikiURL", m.getVikiURL_TR())
			 .append("starring", starringList);
			//doc'a movienin fieldlar� eklenir
			
			docList.add(movieDoc);	//bo� doc listesine eklenir		
			this.id++;//id artt�r�l�r
		}		
		this.collection.insert(docList);//doldurulan doc listesi collectiona insert edilir
	}
	public void createAndInsertContextDocs(ArrayList<Movie> movieArchieve,String language){
		//ENG ve TR dosyalar� i�indeki context d�k�mananlar�n� mnngodbye ge�irir
		
		//kullan�lacak collection dil se�ene�iyle belirlenip inite g�nderilir	
		if(language.equals("TR"))
			init("contextTR");
		else if(language.equals("ENG"))
			init("contextENG");
		
		for(Movie m : movieArchieve){//her bir movie i�in bo� bir mondogb doc yarat�l�r
			BasicDBObject contextDoc = new BasicDBObject();
			
			contextDoc.append("_id",id)
			.append("title",m.getInfoBox().getTitle())
			.append("context", m.getContext(language));	
			//doc'a movienin context title ve id fieldar� eklenir
			docList.add(contextDoc);//doc bo� listeye eklenir			
			this.id++;
		}		
		this.collection.insert(docList);//dolan liste collectiona insert edilir
		
	}
	public void init(String collectionName){
		this.mongoClient = new MongoClient( "localhost" , 27017 );	 //porta ba�lan�l�r
		this.db = mongoClient.getDB("moviesDatabase");//database al�n�r
		this.collection = db.getCollectionFromString(collectionName);//collection al�n�r
		this.docList = new ArrayList<BasicDBObject>();//bo� doc listesi yarat�l�r
		this.id=0;//id s�n�flan�r
		this.collection.remove(new BasicDBObject());//coleection� temizlemek i�in
	}
}
