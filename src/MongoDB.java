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
		//allmovies dokümanýný mongodb'ye formatlamak amacýyla yazýlan method
		init("moviesCollection");		//kullanýlacak collection init gönderilir	
		
		
		for(Movie m : movieArchieve){//her bir movie için boþ bir mondogb doc yaratýlýr
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
			//doc'a movienin fieldlarý eklenir
			
			docList.add(movieDoc);	//boþ doc listesine eklenir		
			this.id++;//id arttýrýlýr
		}		
		this.collection.insert(docList);//doldurulan doc listesi collectiona insert edilir
	}
	public void createAndInsertContextDocs(ArrayList<Movie> movieArchieve,String language){
		//ENG ve TR dosyalarý içindeki context dökümananlarýný mnngodbye geçirir
		
		//kullanýlacak collection dil seçeneðiyle belirlenip inite gönderilir	
		if(language.equals("TR"))
			init("contextTR");
		else if(language.equals("ENG"))
			init("contextENG");
		
		for(Movie m : movieArchieve){//her bir movie için boþ bir mondogb doc yaratýlýr
			BasicDBObject contextDoc = new BasicDBObject();
			
			contextDoc.append("_id",id)
			.append("title",m.getInfoBox().getTitle())
			.append("context", m.getContext(language));	
			//doc'a movienin context title ve id fieldarý eklenir
			docList.add(contextDoc);//doc boþ listeye eklenir			
			this.id++;
		}		
		this.collection.insert(docList);//dolan liste collectiona insert edilir
		
	}
	public void init(String collectionName){
		this.mongoClient = new MongoClient( "localhost" , 27017 );	 //porta baðlanýlýr
		this.db = mongoClient.getDB("moviesDatabase");//database alýnýr
		this.collection = db.getCollectionFromString(collectionName);//collection alýnýr
		this.docList = new ArrayList<BasicDBObject>();//boþ doc listesi yaratýlýr
		this.id=0;//id sýnýflanýr
		this.collection.remove(new BasicDBObject());//coleectioný temizlemek için
	}
}
