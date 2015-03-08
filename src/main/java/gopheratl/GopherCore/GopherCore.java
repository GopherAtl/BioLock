package gopheratl.GopherCore;

import gopheratl.biolock.common.TileEntityBioLock;
import gopheratl.biolock.common.util.BLLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
				GCLog.info("getSaveDirectory got null from getFolderName!");
			GCLog.info("world folder: %s", worldName);
			GCLog.info("base path: %s", basePath);
			saveDirectory=basePath+File.separator;
			if(!(server.isDedicatedServer()))
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
			GCLog.info("Couldn't create directory \"%s\"", path);

		try {
			return f.getCanonicalPath()+File.separator;
		} catch(Exception e) {
			//This shouldn't happen, but java will bitch if I don't handle it...
			GCLog.info("getSaveSubDirPath: Exception from getCanonicalPath?");
			return null;
		}
	}	
	
	public static void exportPackageFile(String sourcePath, String targetDir, String targetName)
	{
		
		GCLog.info("exportPackageFile(%s,%s,%s)", sourcePath, targetDir, targetName);
		InputStream stream=TileEntityBioLock.class.getClassLoader().getResourceAsStream(sourcePath);
		MinecraftServer server=MinecraftServer.getServer();
		File basePath=server.getFile("");			
		File f=new File(basePath+File.separator+"mods"+File.separator+targetDir);
		if (!f.exists())
		{
			BLLog.log(Level.DEBUG, "making dir %s", f);
			f.mkdirs();			
		}
		else if (!f.isDirectory())
		{
			GCLog.severe("file export blocked by directory with target name!");
			return;
		}
			
		File outFile=new File(basePath+File.separator+"mods"+File.separator+targetDir+targetName);
		BLLog.log(Level.DEBUG, "[BioLock] [Debug] outFile== %s", outFile);
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
				GCLog.info("FileNotFoundException extracting lua files");
				e.printStackTrace();
			} catch (IOException e) {
				GCLog.info("IOException extracting lua files");
				e.printStackTrace();
			}
		}
		
	}
}
