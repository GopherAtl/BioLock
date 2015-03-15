package gopheratl.biolock.common.peripheral;

import gopheratl.biolock.common.BioLock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import dan200.computercraft.api.filesystem.IMount;

public class LuaMount implements IMount {
	
	@Override
    public boolean exists(String path) throws IOException {
            File file = new File(new File(BioLock.EXTRACTED_LUA_PATH), path);
            return file.exists();
    }

    @Override
    public boolean isDirectory(String path) throws IOException {
            File file = new File(new File(BioLock.EXTRACTED_LUA_PATH), path);
            return file.isDirectory();
    }

    @Override
    public void list(String path, List<String> contents) throws IOException {
            File directory = new File(new File(BioLock.EXTRACTED_LUA_PATH), path);
            for (File file : directory.listFiles()) {
                    contents.add(file.getName());
            }
    }

    @Override
    public long getSize(String path) throws IOException {
            File file = new File(new File(BioLock.EXTRACTED_LUA_PATH), path);
            return file.length();
    }

    @Override
    public InputStream openForRead(String path) throws IOException {
            File file = new File(new File(BioLock.EXTRACTED_LUA_PATH), path);
            return new FileInputStream(file);
    }
	
}