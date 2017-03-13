import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.spi.DirStateFactory.Result;

public class Neo4j {
	public Driver driver ;
	public Session session ;
	String bookmark;
	ArrayList<MoviePoints> pointList = new ArrayList<>();
	
	public Neo4j(){
		driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "neo4jj" ) );
		session = driver.session(AccessMode.WRITE);
		
	}
	
	
	
	public boolean readGraphData(String url){
		
		StatementResult result;
		
		try ( Session session = driver.session( AccessMode.READ ) )
	    {
	        try ( Transaction tx = session.beginTransaction(bookmark) )
	        {
	        	result = tx.run( "MATCH (a:Link) " +
	        			"WHERE a.url = {url} " +
	    		        "RETURN a.url AS url",
	    		        parameters( "url", url ) );
	        	tx.success();
	            tx.close();
	        }
	        finally
	        {
	            bookmark = session.lastBookmark();
	        }
	    }
		if(result.hasNext()){
			return true;
		}
		else{
			try ( Session session = driver.session( AccessMode.READ ) )
		    {
		        try ( Transaction tx = session.beginTransaction(bookmark) )
		        {
		        	result = tx.run( "MATCH (m:Movie) " +
		        			"WHERE m.url = {url} " +
		    		        "RETURN m.url AS url",
		    		        parameters( "url", url ) );
		        	tx.success();
		            tx.close();
		        }
		        finally
		        {
		            bookmark = session.lastBookmark();
		        }
		    }
			if(result.hasNext()){
				return true;
			}
		}		
		return false;
	}
	public boolean readGraphRealitonship(String url1, String url2){
		StatementResult result;
		try ( Session session = driver.session( AccessMode.READ ) )
		{
			try ( Transaction tx = session.beginTransaction(bookmark) )
		    {
		        result = tx.run( "MATCH p =(:Link {url:{url1}})-[r:level2]-(:Link{url:{url2}}) RETURN p",
		    		       parameters( "url1", url1,"url2", url2 ) );
		        tx.success();
		        tx.close();
		     }
		     finally
		     {
		       bookmark = session.lastBookmark();
		     }
		}
		if(result.hasNext()){
			//System.out.println(result.next());
			return true;
		}
		return false;
	}
	public void writeMovie(Movie movie){
		
		try ( Session session = driver.session( AccessMode.WRITE ) )
		{
		        try ( Transaction tx = session.beginTransaction() )
		        {
		        	String url = movie.getWikiURL_EN();
		    		String id = new Integer(movie.getId()).toString();		    		
		    		
		    		tx.run( "CREATE (a:Movie {id: {id}, url: {url}})", parameters( "id", id, "url", url));
		            tx.success();
		            tx.close();
		        }
		        finally
		        {
		            bookmark = session.lastBookmark();
		        }
		}		
		
	}
	public void writeLink(String url){
		if(!readGraphData(url)){
			try ( Session session = driver.session( AccessMode.WRITE ) )
		    {
		        try ( Transaction tx = session.beginTransaction(bookmark) )
		        {		        	
		    		tx.run( "CREATE (a:Link {url: {url}})", parameters("url", url));
		            tx.success();
		            tx.close();
		        }
		        finally
		        {
		            bookmark = session.lastBookmark();
		        }
		    }
		}
	}
	
	public void connectMovie_Link(String movieUrl, String link){
		
		try ( Session session = driver.session( AccessMode.WRITE )  )
	    {
	        try ( Transaction tx = session.beginTransaction(bookmark) )
	        {		        	
	    		tx.run("MATCH (m:Movie),(l:Link) "
						+ "WHERE m.url = {movieUrl} AND l.url ={link} "
						+ "CREATE (m)-[r:level1]->(l)", parameters( "movieUrl", movieUrl, "link", link) );
	            tx.success();
	            tx.close();
	        }
	        finally
	        {
	            bookmark = session.lastBookmark();
	        }
	    }
	}
	public void connectLink_Link(String link1, String link2){
		if(!readGraphRealitonship(link1, link2)){
			try ( Session session = driver.session( AccessMode.WRITE )  )
			{
			    try ( Transaction tx = session.beginTransaction(bookmark) )
			    {		        	
					tx.run("MATCH (u:Link),(l:Link) "
							+ "WHERE u.url = {link1} AND l.url ={link2} "
							+ "CREATE (u)-[r:level2]->(l)", parameters( "link1", link1, "link2", link2) );
			        tx.success();
			        tx.close();
			    }
			    finally
			    {
			        bookmark = session.lastBookmark();
			    }
			}
		}
	}
	public void createGraph(ArrayList<Movie> movieList){
				
		ArrayList<String> links_depth_1 = new ArrayList<>();
		ArrayList<String> links_depth_2 = new ArrayList<>();
		
		this.cleanDatabase();
				
		for(Movie movie : movieList){

			System.out.println(movie.getInfoBox().getTitle()+" Neo4j");
			
			if(!movie.getWikiURL_EN().equals("No Url Source")){
				writeMovie(movie);
				links_depth_1 = collectLinks(movie.getWikiURL_EN());
				for(String link1 : links_depth_1){
									
					writeLink(link1);
					connectMovie_Link(movie.getWikiURL_EN(), link1);
					links_depth_2 = collectLinks(link1);
					for(String link2 : links_depth_2){
						writeLink(link2);
						connectLink_Link(link1, link2);
					}
				}
			}
			else {
				System.out.println(movie.getWikiURL_EN());
			}
		}				
	}
	public void cleanDatabase(){
		this.session.run("MATCH (n)" +
						"OPTIONAL MATCH (n)-[r]-() "+
						"DELETE n,r");
	}	
	public ArrayList<String> collectLinks(String wikiUrl){
		
		
		ArrayList<String> urlList = new ArrayList<>();
		
		Response res;
		try {	
			res = Jsoup.connect(wikiUrl).execute();
			String html = res.body();
			Document doc = Jsoup.parseBodyFragment(html);
			Element paragraph = doc.select("#content").first().select("p").first();
			Elements links = paragraph.select("a");
			for(Element e : links){	
				String href = e.attr("href");
				if(!href.startsWith("#") && !href.startsWith("https://")){
					urlList.add("https://en.wikipedia.org"+href);
				}
				if(href.startsWith("https://")){
					urlList.add(href);
				}
				
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(wikiUrl);
			e.printStackTrace();
		}		
		return urlList;
	}
	
	public void query(String id){
		StatementResult result;
		
		try ( Session session = driver.session( AccessMode.READ ) )
	    {
	        try ( Transaction tx = session.beginTransaction(bookmark) )
	        {
	        	result = tx.run( "MATCH (:Movie { id: {id} })"
	        			+ "-[r:level1]->(p) "
	        			+ "RETURN p.url",
	    		        parameters( "id", id ) );
	        	tx.success();
	            tx.close();
	        }
	        finally
	        {
	            bookmark = session.lastBookmark();
	        }
	    }
		while(result.hasNext()){
			System.out.println(result.next().get("p.url"));
		}	
	}
	
	public void recommend(String id_input){
		int level1_point = 10;//1. seviye
		int level2_point = 5;//2. seviye
		int level3_point = 2;//3. seviye
		
		StatementResult level_1_Links = movieAndLink_getLinkURL(id_input);//filmin ilk seviye baðlý linkleri alýnýr
		
		while(level_1_Links.hasNext()){//filmin ilk seviye baðlý linkleri gezilir
			Record a_Level_1_Link = level_1_Links.next();
			
			depth1(a_Level_1_Link, id_input, level1_point);//bu linkin direk olarak baþka bir filme baðlý olma durumu
			depth2(a_Level_1_Link, id_input, level2_point);//bu linkin baðlý olduðu ikinci bir linkin baþka bir filme baðlý olma durumu
			depth3(a_Level_1_Link, id_input, level3_point);//bu linkin baðlý olduðu ikinci bir linkin, baðlý olduðu üçüncü bir linkin baþka bir filme baðlý olma durumu
		}
		
		printPoints();
		pointList.clear();
		
	}
	public void depth1(Record theLink, String id_input, int point){
		
		String level1URL = substring(theLink.get("l.url").toString());
		StatementResult result = linkAndMovie_getMovieID(level1URL);//filme baðlý mý sorgula
		while(result.hasNext()){
			Record theMovie = result.next();			
			String level1MoiveID = substring(theMovie.get("m.id").toString());
			if(!level1MoiveID.equals(id_input)){
				addPoints(theMovie, id_input, point);//filme baðlýysa resulttan gelen filme listede puanýný ekle
			}
		}
	}
	public void depth2(Record theLink, String id_input, int point){
		String level2URL = substring(theLink.get("l.url").toString());
		StatementResult result = linkAndLink_getURL(level2URL);//linke baðlý mý sorgula
		while(result.hasNext()){
			Record r = result.next();
			depth1(r, id_input, point);//linkin baðlý olduðu link(resulttan gelen), filme baðlý mý sorgula
		}		
	}
	public void depth3(Record theLink, String id_input, int point){
		String level3URL = substring(theLink.get("l.url").toString());
		StatementResult result = linkAndLink_getURL(level3URL);//linke baðlý mý sorgula
		while(result.hasNext()){			
			Record r = result.next();
			depth2(r, id_input, point);//linkin baðlý olduðu link(resulttan gelen), baþka bir linke ve o link de baþka filme baðlý mý sorgula
		}		
	}	
	public StatementResult linkAndLink_getURL(String url_input) {
		/*
		 * eldeki Link'in level2 relationship'i ile baðlý olduðu 
		 * Link'lerin url attribute'unu almamýzý saðlayan method
		 */
		StatementResult result;
		
		try ( Transaction tx = session.beginTransaction(bookmark) )
		{
			result = tx.run( "MATCH p =(:Link {url:{url1}})-[r:level2]-(l:Link) RETURN l.url",
		    		       parameters( "url1", url_input) );
		    tx.success();
		    tx.close();
		}
		return result;
		
	}
	public StatementResult linkAndMovie_getMovieID(String url_input) {
		/*
		 * eldeki Link'in level1 relationship'i ile baðlý olduðu 
		 * Movie'lerin id attribute'unu almamýzý saðlayan method
		 */
		StatementResult result;
			
		try ( Transaction tx = session.beginTransaction(bookmark) )
		{
			result = tx.run( "MATCH p =(m:Movie)-[r:level1]-(l:Link{url:{url1}}) RETURN m.id",
			    		       parameters( "url1", url_input) );
			 tx.success();
			 tx.close();
		}		
		return result;		
	}
	public StatementResult movieAndLink_getLinkURL(String id_input) {
		/*
		 * eldeki Movie'nin level1 relationship'i ile baðlý olduðu 
		 * Link'lerin url attribute'unu almamýzý saðlayan method
		 * en baþta bu method çaðýrýlýr
		 */
		StatementResult result;
			
		try ( Transaction tx = session.beginTransaction(bookmark) )
		{
			result = tx.run( "MATCH p =(m:Movie {id:{id}})-[r:level1]-(l:Link) RETURN l.url",
			    		       parameters( "id", id_input) );
			 tx.success();
			 tx.close();
		}		
		return result;		
	}
	
	public void addPoints(Record record, String id_input, int point) {
		
			Integer newID = Integer.parseInt(substring(record.get("m.id").toString()));
			MoviePoints mp = searchArray(newID, pointList);
			if(newID != Integer.parseInt(id_input)){
				if(mp==null){
					pointList.add(new MoviePoints(newID, point));
				}
				else{
					mp.point = mp.point+point;
				}
			}
		
	}
	public MoviePoints searchArray(int id, ArrayList<MoviePoints> list){
		for(MoviePoints moviePoint:list){
			if(moviePoint.id==id){				 
				return moviePoint;
			}
		}
		return null;
		
	}
	public String substring(String str){		
		return str.substring(1, str.length()-1);		
	}

	public class MoviePoints{
		int id;
		int point;
		
		public MoviePoints(int id, int point){
			this.id = id;
			this.point = point;
		}
	}
	public void printPoints(){
		for(MoviePoints m : pointList){
			System.out.println("Movie:"+m.id+"("+m.point+" points)");
		}		
	}	
}


/*
public void deneme(String id){
		ArrayList<MoviePoints> pointList = new ArrayList<>();
		StatementResult result;
		StatementResult result2;
		StatementResult result3;
		StatementResult result4;
		StatementResult result5;
		StatementResult result6;
		try ( Session session = driver.session( AccessMode.READ ) )
		{
			try ( Transaction tx = session.beginTransaction(bookmark) )
		    {
				result = tx.run( "MATCH p =(:Movie {id:{id}})-[r:level1]-(l:Link) RETURN l.url",
		    		       parameters( "id", id) );
		        tx.success();
		        tx.close();
		    }
			while(result.hasNext()){				
				String level1URL = substring(result.next().get("l.url").toString());
				try ( Transaction tx = session.beginTransaction(bookmark) )
				{
						result2 = tx.run( "MATCH p =(:Link {url:{url1}})-[r:level1]-(m:Movie) RETURN m.id",
				    		       parameters( "url1", level1URL) );
				        tx.success();
				        tx.close();
				}
				while(result2.hasNext()){
					Integer idReturn = Integer.parseInt(substring(result2.next().get("m.id").toString()));
				
					if(idReturn != Integer.parseInt(id)){
						MoviePoints mp = searchArray(idReturn, pointList);
						if(mp==null){
							pointList.add(new MoviePoints(idReturn, 10));
						}
						else{
							mp.point = mp.point+10;
						}
					}
					else{
						try ( Transaction tx = session.beginTransaction(bookmark) )
						{
								result3 = tx.run( "MATCH p =(:Link {url:{url1}})-[r:level2]-(l:Link) RETURN l.url",
						    		       parameters( "url1", level1URL) );
						        tx.success();
						        tx.close();
						}
						while(result3.hasNext()){
							String level2URL = substring(result3.next().get("l.url").toString());
							try ( Transaction tx = session.beginTransaction(bookmark) )
							{
									result4 = tx.run( "MATCH p =(m:Movie)-[r:level1]-(l:Link{url:{url1}}) RETURN m.id",
							    		       parameters( "url1", level2URL) );
							        tx.success();
							        tx.close();
							}
							
							while(result4.hasNext()){
								
								Integer idReturn2 = Integer.parseInt(substring(result4.next().get("m.id").toString()));
								MoviePoints mp = searchArray(idReturn2, pointList);
								if(idReturn2 != Integer.parseInt(id)){
									if(mp==null){
										pointList.add(new MoviePoints(idReturn2, 5));
									}
									else{
										mp.point = mp.point+5;
									}
								}
								
						}
							try ( Transaction tx = session.beginTransaction(bookmark) )
							{
									result5 = tx.run( "MATCH p =(:Link {url:{url1}})-[r:level2]-(l:Link) RETURN l.url",
							    		       parameters( "url1", level2URL) );
							        tx.success();
							        tx.close();
							}
							while(result5.hasNext()){
								
								String level3URL = substring(result5.next().get("l.url").toString());
								try ( Transaction tx = session.beginTransaction(bookmark) )
								{
										result6 = tx.run( "MATCH p =(m:Movie)-[r:level1]-(l:Link{url:{url1}}) RETURN m.id",
								    		       parameters( "url1", level3URL) );
								        tx.success();
								        tx.close();
								}
								while(result6.hasNext()){
									Integer idReturn3 = Integer.parseInt(substring(result6.next().get("m.id").toString()));
									MoviePoints mp2 = searchArray(idReturn3, pointList);
									if(idReturn3 != Integer.parseInt(id)){
										if(mp2==null){
											pointList.add(new MoviePoints(idReturn3, 2));
										}
										else{
											mp2.point = mp2.point+2;
										}
									}
								}
									
							}	
					}
				}				
			}			
		}
		for(MoviePoints moviePoint:pointList){
			System.out.println(moviePoint.id+", "+moviePoint.point);
		}
		}
	}
*/
