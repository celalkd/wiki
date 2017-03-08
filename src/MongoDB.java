import java.util.ArrayList;
import com.mongodb.*;

public class MongoDB {
	private static MongoDB  mongodb   = new MongoDB();
	private ArrayList<BasicDBObject> docList ;
	//private int id;
	private MongoClient mongoClient ;	 
	private DB db ;
	private DBCollection collection;	
	
	//CONSTRUCTORS
	private MongoDB(){
		
	}	
	public static MongoDB getMongoDB() {
		return mongodb;
	}
	
	public void query_with_title(String title){
		
		DBCursor cursor = collection.find(new BasicDBObject("title", title));
		
		for(DBObject result : cursor){
			printResult(result);
		}
	}
	public void query_with_tags(String director, String yearMin, String yearMax, ArrayList<String> starring, ArrayList<String> genre, double rating ){
		
		BasicDBObject query = new BasicDBObject();
		ArrayList<BasicDBObject> obj = new ArrayList<BasicDBObject>();
		
		if(director!=null){
			query.append("director", director);
		}
		
		if(yearMin!=null && yearMax==null){
			int yearMinInt = Integer.parseInt(yearMin);
			query.append("year", new BasicDBObject("$gte", yearMinInt));
		}
		else if(yearMin==null && yearMax!=null){
			int yearMaxInt = Integer.parseInt(yearMax);				
			query.append("year", new BasicDBObject("$lte", yearMaxInt));
		}
		else if(yearMin!=null && yearMax!=null){
			int yearMinInt = Integer.parseInt(yearMin);
			int yearMaxInt = Integer.parseInt(yearMax);	

			obj.add(new BasicDBObject("year",new BasicDBObject("$gte", yearMinInt)));
			obj.add(new BasicDBObject("year",new BasicDBObject("$lte", yearMaxInt)));
			query.append("$and", obj);
		}				
		
		if(starring!=null){
			for(String star : starring){
				obj.add(new BasicDBObject("starring", star));
				query.append("$and", obj);
			}			
		}
		if(genre!=null){				
			for(String g : genre){
				obj.add(new BasicDBObject("genre", g));
				query.append("$and", obj);
			}
		}
		
		if(rating>=0 && rating<=10.0){
			query.append("rating",  new BasicDBObject("$gte", rating));
		}
		
		System.out.print("\nQUERY: "+query.toJson()+"\nRESULT: ");
		DBCursor cursor = collection.find(query);		
		for(DBObject result : cursor){			
			System.out.println("\n"+result.get("title"));
			printResult(result);
		}	
		
	}
	public void printResult(DBObject result){
		System.out.println("Directed By: " + result.get("director"));
		System.out.println("Starring: " + result.get("starring"));
		System.out.println("Year: " + result.get("year"));
		System.out.println("Genre: " + result.get("genre"));
		System.out.println("Rating: " + result.get("rating"));
	}
	
	public void createAndInsertMovieDocs(ArrayList<Movie> movieArchieve){
		init("moviesCollection");					
		for(Movie m : movieArchieve){
			if(m.getVerified()){				
				System.out.println(m.getInfoBox().getTitle()+" Mongodb");				
				BasicDBObject movieDoc = new BasicDBObject();				
				movieDoc.append("_id",m.getId())
				 .append("title", m.getInfoBox().getTitle())
				 .append("director", m.getInfoBox().getDirector())
				 .append("year", m.getYear())
				 .append("starring", m.getInfoBox().getStarring())
				 .append("genre", m.getGenre())
				 .append("rating", m.getRating())
				 .append("wikiURL", m.getWikiURL_EN())
				 .append("vikiURL", m.getVikiURL_TR())
				 .append("context_ENG", m.getContext_ENG())
				 .append("context_TR", m.getContext_TR());	
				docList.add(movieDoc);
			}
		}		
		this.collection.insert(docList);//doldurulan doc listesi collectiona insert edilir
	}
	/*public void createAndInsertContextDocs(ArrayList<Movie> movieArchieve,String language){
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
		
	}*/
	public void init(String collectionName){
		this.mongoClient = new MongoClient( "localhost" , 27017 );	 //porta baðlanýlýr
		this.db = mongoClient.getDB("moviesDatabase");//database alýnýr
		this.collection = db.getCollectionFromString(collectionName);//collection alýnýr
		this.docList = new ArrayList<BasicDBObject>();//boþ doc listesi yaratýlýr
		//this.collection.remove(new BasicDBObject());//collectioný temizlemek için
	}
}
