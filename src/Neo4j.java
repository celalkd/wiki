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
	String bookmark;
	
	public Neo4j(){
		driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "neo4jj" ) );
		session = driver.session(AccessMode.WRITE);
		
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
		
		String bookmark = null;
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

	public class MoviePoints{
		int id;
		int point;
		public MoviePoints(int id, int point){
			this.id = id;
			this.point = point;
		}
	}
}


/*
public ArrayList<String> collectLinks(String wikiUrl){
		
		ArrayList<String> urlList = new ArrayList<>();
		String tagPath = "#mw-content-text > p";
		
		Response res;
		try {	
			res = Jsoup.connect(wikiUrl).execute();
			String html = res.body();
			Document doc = Jsoup.parseBodyFragment(html);
			Element paragraph = doc.select(tagPath).first();
			Elements links = paragraph.select("a");
			for(Element e : links){				
				urlList.add("https://en.wikipedia.org"+e.attr("href"));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return urlList;
public void writeLink(String url, String bookmark){
System.out.println(url);
boolean found = false;
bookmark = readGraph(url, found, bookmark);		

if(!found){
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
public void createGraph(ArrayList<Movie> movieList){
		String bookmark = null;
		ArrayList<String> links = new ArrayList<>();
		cleanDatabase();
		
		for(Movie movie : movieList){
			String movieUrl = movie.getWikiURL_EN();			
			writeMovie(movie,bookmark);
			
			links = collectLinks(movieUrl);
			
			for(String url: links){
				writeLink(url, bookmark);
				connectMovie_Link(movieUrl, url, bookmark);
			}
		}
		try ( Session session = driver.session( AccessMode.WRITE )  )
	    {
	        try ( Transaction tx = session.beginTransaction(bookmark) )
	        {		        	
	    		tx.run("MATCH (n) RETURN n" );
	            tx.success();
	            tx.close();
	        }
	        finally
	        {
	            bookmark = session.lastBookmark();
	        }
	    }
	}
*/



/*public void writeLinks(String firstUrl, String bookmark, int depth){

boolean found = false;
ArrayList<String> newLinks = new ArrayList<>();		
bookmark = readGraph(firstUrl, found, bookmark);		

if(!found && depth<=1){
	System.out.println(depth);
	try ( Session session = driver.session( AccessMode.WRITE ) )
    {
        try ( Transaction tx = session.beginTransaction(bookmark) )
        {		        	
    		tx.run( "CREATE (a:Link {url: {url}})", parameters("url", firstUrl));
            tx.success();
            tx.close();
            
            newLinks = collectLinks(firstUrl);
            
            System.out.println("current link: "+firstUrl);
            for(String newUrl : newLinks){
            	System.out.println("sublinks: "+newUrl);
            }
           
            depth++;
            for(String newUrl : newLinks){
            	connectLink_Link(firstUrl, newUrl, bookmark);
            	writeLinks(newUrl, bookmark, depth);
            }					
        }
        finally
        {
        	bookmark = session.lastBookmark();
			
        }
    }
}		

}*/


/*public void createGraph2(ArrayList<Movie> movieList){
cleanDatabase();
String bookmark = null;

Movie movie = movieList.get(0);
//for(Movie movie : movieList){
	int depth = 0;
	String movieUrl = movie.getWikiURL_EN();			
	//writeMovie(movie,bookmark);
	writeLinks(movieUrl, bookmark, depth);
	
	
//}
}*/


/*public void connectLink_Link(String link1, String link2, String bookmark){
try ( Session session = driver.session( AccessMode.WRITE )  )
{
    try ( Transaction tx = session.beginTransaction(bookmark) )
    {		        	
		tx.run("MATCH (u:Link),(l:Link) "
				+ "WHERE u.url = {link1} AND l.url ={link2} "
				+ "CREATE (u)-[r:contains]->(l)", parameters( "link1", link1, "link2", link2) );
        tx.success();
        tx.close();
    }
    finally
    {
        bookmark = session.lastBookmark();
    }
}
}*/




/*
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
*/
