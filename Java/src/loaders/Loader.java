package loaders;

import java.util.Set;

import model.database.PubMedDatabase;
import model.database.PubMedDatabaseChunk;
import model.database.TopicsDatabase;

public abstract class Loader {
	protected PubMedDatabase pmDatabase;
	protected TopicsDatabase tpDatabase;
	
	protected PubMedDatabase pmWorkingSet;
	
	public Loader (PubMedDatabase pmDatabase, TopicsDatabase tpDatabase)
	{
		this.pmDatabase = pmDatabase;
		this.tpDatabase = tpDatabase;
		
		this.pmWorkingSet = pmDatabase;
	}
	
	public void setWorkingSet(PubMedDatabase pmWorkingSet)
	{
		this.pmWorkingSet = pmWorkingSet;
	}
	
	public void load()
	{
		long startTime = System.currentTimeMillis();
		this.loadMethod();
		long endTime = System.currentTimeMillis();
	    System.out.println("Time in "+this.getClass().toString() +": " + (endTime-startTime) + "ms"); 
	}
	
	public abstract void loadMethod();
}
