import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.ArrayList;

import javax.naming.spi.DirStateFactory.Result;

public class Neo4j {
	public Driver driver ;
	public Session session ;
	
	public Neo4j(){
		driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "neo4jj" ) );
		session = driver.session(AccessMode.WRITE);
		
	}
	public void createDirectedByRelationship(ArrayList<Movie> list){
		
		this.cleanDatabase();
		for(Movie movie : list){
			cMovie(movie);
			dMovie(movie);
			//fMovie(movie);
			String bookmark;
			
			try ( Session session = driver.session( AccessMode.WRITE )  )
		    {
		        try ( Transaction tx = session.beginTransaction() )
		        {
		        	
		    		String id = new Integer(movie.getId()).toString();
		    		String directorName = movie.getInfoBox().getDirector();
		    		
		    		tx.run("MATCH (m:Movie),(d:Director) "
							+ "WHERE m.id={id} AND d.name={name} "
							+ "CREATE (m)-[r:Directed_By]->(d)", parameters( "id", id, "name", directorName) );
		    		
		    		
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
public void createFilmedRelationship(ArrayList<Movie> list){
		
		
		
			
			String bookmark;
			
			try ( Session session = driver.session( AccessMode.WRITE )  )
		    {
		        try ( Transaction tx = session.beginTransaction() )
		        {		        	
		    		tx.run("MATCH (f:Filmography),(d:Director) "
							+ "WHERE f.name = d.name "
							+ "CREATE (d)-[r:Filmed]->(f)");
		            tx.success();
		            tx.close();
		        }
		        finally
		        {
		            bookmark = session.lastBookmark();
		        }
		    }
	}
	public void cMovie(Movie movie){
		String bookmark;
		try ( Session session = driver.session( AccessMode.WRITE ) )
	    {
	        try ( Transaction tx = session.beginTransaction() )
	        {
	        	String title = movie.getInfoBox().getTitle();
	    		String id = new Integer(movie.getId()).toString();
	    		
	    		
	    		tx.run( "CREATE (a:Movie {title: {title}, id: {id}})", parameters( "title", title, "id", id));
	            tx.success();
	            tx.close();
	        }
	        finally
	        {
	            bookmark = session.lastBookmark();
	        }
	    }
	}
	public void dMovie(Movie movie){
		String bookmark;
		StatementResult result;
		
		try ( Session session = driver.session( AccessMode.READ ) )
	    {
	        try ( Transaction tx = session.beginTransaction() )
	        {
	        	String directorName = movie.getInfoBox().getDirector();
	    		String directorURL = this.getDirectorUrl(movie.getWikiURL_EN());
	    		
	    		
	    		result = tx.run( "MATCH (a:Director) WHERE a.name = {name} " +
	    		        "RETURN a.name AS name",
	    		        parameters( "name", directorName ) );
	            tx.close();
	        }
	        finally
	        {
	            bookmark = session.lastBookmark();
	        }
	    }		
		if(!result.hasNext()){
			try ( Session session = driver.session( AccessMode.WRITE ) )
		    {
		        try ( Transaction tx = session.beginTransaction(bookmark) )
		        {
		        	String directorName = movie.getInfoBox().getDirector();
		    		String directorURL = this.getDirectorUrl(movie.getWikiURL_EN());
		    		String url = getDirectorUrl(movie.getWikiURL_EN())+"#Filmography";
		    		
		    		tx.run( "CREATE (a:Director {name:{name}, url:{url}})", parameters("name", directorName,"url",directorURL) );	
		    		tx.run( "CREATE (a:Filmography {url: {url}, name:{name}})", parameters( "url", url,"name",directorName));
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
	public void fMovie(Movie movie){
			String bookmark;
			try ( Session session = driver.session( AccessMode.WRITE ) )
		    {
		        try ( Transaction tx = session.beginTransaction() )
		        {
		        	String url = getDirectorUrl(movie.getWikiURL_EN())+"#Filmography";    		
		        	String directorName = movie.getInfoBox().getDirector();
		        	
		    		tx.run( "CREATE (a:Filmography {url: {url}, name:{name}})", parameters( "url", url,"name",directorName));
		            tx.success();
		            tx.close();
		        }
		        finally
		        {
		            bookmark = session.lastBookmark();
		        }
		    }
		
	}
	public void cleanDatabase(){
		this.session.run("MATCH (n)" +
						"OPTIONAL MATCH (n)-[r]-() "+
						"DELETE n,r");
	}
	public String getDirectorUrl(String vikiURL) {
		int index = 0;
		try {	
			Response res = Jsoup.connect(vikiURL).execute();
			String html = res.body();
			Document doc = Jsoup.parseBodyFragment(html);
			Elements elements_th = doc.getElementsByTag("th");
			
			for(Element th : elements_th){
				th = elements_th.get(index);
				
				if(th.text().equals("Directed by")){					
					Element td = th.nextElementSibling();//sonraki sibling'i td öðesi oluyor
					Element link = td.select("a").get(0);
					String directorURL = "https://en.wikipedia.org"+link.attr("href");
			    	return directorURL;						
				}
				else
					index++;
			}
		} catch (IOException e) {			
			e.getCause();
		}
		return null;		  	
	}	

}
