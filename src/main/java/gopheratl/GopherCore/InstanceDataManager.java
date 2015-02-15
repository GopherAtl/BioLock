package gopheratl.GopherCore;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/****
 * Manages creating indexes and generating file paths for objects which store per-instance data
 * in the world.
 * @author GopherAtl
 *
 */
public class InstanceDataManager 
{
	protected Set<Integer> indexSet;
	protected int maxIndex;
	protected Pattern filePattern;
	protected String basePath, baseFilename;

	
	public InstanceDataManager(String directory, String baseFilename)
	{
		this.baseFilename=baseFilename;
		
		indexSet=new HashSet<Integer>();
		maxIndex=1;
		indexSet.add(1);
		
		basePath=GopherCore.getSaveSubDirPath(directory);
		if (basePath.charAt(basePath.length()-1)!=File.separatorChar)
			basePath+=File.separatorChar;
		
		File baseDir=new File(basePath);		
		File[] files=baseDir.listFiles();
		filePattern=Pattern.compile("^"+baseFilename+"([0-9]+)"+File.separator+".nbt");
		for (int i=0; i<files.length; ++i)
		{						
			Matcher match=filePattern.matcher(files[i].getName());
			if (files[i].isFile() && match.matches())
			{
				String numStr=match.group(1);
				Integer n=Integer.decode(numStr);				
				if (n<=maxIndex)
				{
					indexSet.remove(n);
					if (indexSet.size()==0)
						indexSet.add(++maxIndex);
				}
				else
				{
					while (n-1>maxIndex)
						indexSet.add(++maxIndex);
					maxIndex=n;
				}
			}
		}
	}
	
	public int getNextID()
	{
		int nextID=indexSet.iterator().next();
		indexSet.remove(nextID);
		//if the set's empty now, increment max and add the next larger one
		if (indexSet.size()==0)
			indexSet.add(++maxIndex);
		//System.out.println("getNextID assigning id "+nextID);
		return nextID;
	}
	
	public void releaseID(int id)
	{
		indexSet.add(id);
	}
	
	public String getFilePath(int id)
	{
		return basePath+baseFilename+id+".nbt";
	}
}
