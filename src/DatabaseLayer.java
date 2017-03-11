import java.io.IOException;

public class DatabaseLayer {	
	
	MongoDB mongoDB = MongoDB.getMongoDB();
	Neo4j neo4j = new Neo4j();
	Redis redis = new Redis();
	
	Archive archieve = Archive.getArchive();
	FileIO fileIO = FileIO.getFileIO();
	
	public void build(){
		try {
			archieve.getMovies(fileIO.fileToString("top250_liste_15film"));//wikiLink, id ve yapým yýlý
			archieve.checkMovies(fileIO.fileToString("top250_imdb_15film"));//infobox(title, director, starring), genre, rating
			archieve.writeWordsToFile("TR");//wordList
			archieve.writeWordsToFile("ENG");
						
			mongoDB.createAndInsertMovieDocs(archieve.getMovieArchive());//mongodb		
			redis.createRedis(archieve.getMovieArchive());//redis
			neo4j.createGraph(archieve.getMovieArchive());//neo4j	
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
