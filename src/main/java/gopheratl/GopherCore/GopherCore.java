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
}
