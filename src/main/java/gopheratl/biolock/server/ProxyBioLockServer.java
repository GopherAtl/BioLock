package gopheratl.biolock.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;












import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import gopheratl.biolock.common.BioLock;
import gopheratl.biolock.common.BlockBioLock;
import gopheratl.biolock.common.BlockKeypadLock;
import gopheratl.biolock.common.BlockPRB;
import gopheratl.biolock.common.ItemBlockProgrammable;
import gopheratl.biolock.common.ProxyBioLock;
import gopheratl.biolock.common.RecipeResetProgrammable;
import gopheratl.biolock.common.TileEntityBioLock;
import gopheratl.biolock.common.TileEntityKeypadLock;
import gopheratl.biolock.common.TileEntityPRB;
import gopheratl.biolock.common.BioLock.Blocks;
import gopheratl.biolock.common.util.BLLog;
import gopheratl.biolock.common.util.ResourceExtractingUtils;

public class ProxyBioLockServer extends ProxyBioLock 
{
	@Override
	public World getWorld(int dimId) {
		return MinecraftServer.getServer().worldServerForDimension(dimId);
	}
}
