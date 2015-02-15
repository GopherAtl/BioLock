package gopheratl.GopherCore;

import gopheratl.biolock.common.TileEntityBioLock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

public class GopherCore {
    static String saveDirectory=null;
    		
	public static String getSaveDirectory()
	{
		//on first access, build it
		if (saveDirectory==null)
		{
			MinecraftServer server=MinecraftServer.getServer();
			File basePath=server.getFile("");			
			String worldName=server.getFolderName();
			if (worldName==null)
				System.out.println("getSaveDirectory got null from getFolderName!");
			saveDirectory=basePath+File.separator;
			if(!(server instanceof DedicatedServer))
				saveDirectory+="saves"+File.separator;
			saveDirectory+=worldName+File.separator;
		}
		//return it
		return saveDirectory;
	}
	
	public static String getSaveSubDirPath(String folderName)
	{
		String path=getSaveDirectory()+folderName;
		File f=new File(path);
		if ((f.exists() && !f.isDirectory()) || (!f.exists()  && !new File(path).mkdirs()))			
			System.out.println("[GopherCore] Couldn't create directory \""+path+"\"");

		try {
			return f.getCanonicalPath()+File.separator;
		} catch(Exception e) {
			//This shouldn't happen, but java will bitch if I don't handle it...
			System.out.println("[GopherCore] getSaveSubDirPath: Exception from getCanonicalPath?");
			return null;
		}
	}	
	
	public static void exportPackageFile(String sourcePath, String targetDir, String targetName)
	{
		
		//System.out.println("[GopherCore] exportPackageFile("+sourcePath+","+targetDir+","+targetName+")");
		InputStream stream=TileEntityBioLock.class.getClassLoader().getResourceAsStream(sourcePath);
		MinecraftServer server=MinecraftServer.getServer();
		File basePath=server.getFile("");			
		File f=new File(basePath+File.separator+"mods"+File.separator+targetDir);
		if (!f.exists())
		{
			//System.out.println("[BioLock] [Debug] making dir "+f);
			f.mkdirs();			
		}
		else if (!f.isDirectory())
		{
			System.out.println("[GopherCore] [ERROR] file export blocked by directory with target name!");
			return;
		}
			
		File outFile=new File(basePath+File.separator+"mods"+File.separator+targetDir+targetName);
		//System.out.println("[BioLock] [Debug] outFile== "+outFile);
		if (outFile.exists()==false)
		{
			try {
				OutputStream outStream=new FileOutputStream(outFile);
				byte[] buffer=new byte[256];
				int bytesRead;
				while((bytesRead=stream.read(buffer)) !=-1)
					outStream.write(buffer,0,bytesRead);		
				outStream.close();
				stream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("[GopherCore] FileNotFoundException extracting lua files");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("[GopherCore] IOException extracting lua files");
				e.printStackTrace();
			}
		}
		
	}
}
