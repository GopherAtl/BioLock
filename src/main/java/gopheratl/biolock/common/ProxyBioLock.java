package gopheratl.biolock.common;

import java.io.File;
import java.io.IOException;

import gopheratl.biolock.common.BioLock.Blocks;
import gopheratl.biolock.common.util.BLLog;
import gopheratl.biolock.common.util.ResourceExtractingUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.FMLLaunchHandler;

public class ProxyBioLock {
    
    public void preInit() {
    	registerBlocks();
    }
    
    public void init() {
    	registerRecipes();
    	registerRenderInformation();
    	setupLuaFiles();
    }
	
	protected void registerRenderInformation() {
		//overriden in client proxy.
    }
	
	private void registerBlocks() {
		BLLog.debug("Registering blocks");
		BioLock.Blocks.bioLock=new BlockBioLock();
		BioLock.Blocks.bioLock.setBlockName("biolock.biolock");

		GameRegistry.registerBlock(Blocks.bioLock,ItemBlockProgrammable.class,"biolock");
		GameRegistry.registerTileEntity(TileEntityBioLock.class, "BioLockPeripheral"); 

		BioLock.Blocks.prb=new BlockPRB();
		BioLock.Blocks.prb.setBlockName("biolock.prb");
		
		GameRegistry.registerBlock(Blocks.prb,ItemBlockProgrammable.class,"prb");
		GameRegistry.registerTileEntity(TileEntityPRB.class, "PRBPeripheral");

		BioLock.Blocks.keypadLock=new BlockKeypadLock();
		BioLock.Blocks.keypadLock.setBlockName("biolock.keypadLock");

		GameRegistry.registerBlock(Blocks.keypadLock,ItemBlockProgrammable.class,"keypad");
		GameRegistry.registerTileEntity(TileEntityKeypadLock.class,"KeypadLockPeripheral");
	}
	
	private void registerRecipes() {
		BLLog.debug("Registering recipes");
		ItemStack stone = new ItemStack((Block)Block.blockRegistry.getObject("stone"));
		ItemStack redstone = new ItemStack((Item)Item.itemRegistry.getObject("redstone"));
		ItemStack redstone_block = new ItemStack((Block)Block.blockRegistry.getObject("redstone_block"));
		ItemStack glass_pane = new ItemStack((Block)Block.blockRegistry.getObject("glass_pane"));
		ItemStack stone_button = new ItemStack((Block)Block.blockRegistry.getObject("stone_button"));
		
		GameRegistry.addRecipe(new ItemStack(Blocks.bioLock), new Object[] { "SGS","SRS","SGS",'S',stone,'R',redstone,'G',glass_pane});
		GameRegistry.addRecipe(new RecipeResetProgrammable(Blocks.bioLock));
		
		GameRegistry.addRecipe(new ItemStack(Blocks.prb),new Object[] { "SRS","RBR","SRS",'S',stone,'R',redstone,'B',redstone_block});		
		GameRegistry.addRecipe(new RecipeResetProgrammable(Blocks.prb));
		
		GameRegistry.addRecipe(new ItemStack(Blocks.keypadLock), new Object[] { "BBB", "BBB", "BBB", 'B', stone_button});
		GameRegistry.addRecipe(new RecipeResetProgrammable(Blocks.keypadLock));
	}

	public File getBase() {
		if (FMLLaunchHandler.side().isClient()) {
            return Minecraft.getMinecraft().mcDataDir;
		} else {
			return new File(".");
		}
	}
	
	public boolean setupLuaFiles() {
		BLLog.debug("Extracting Lua files");
		ModContainer container = FMLCommonHandler.instance().findContainerFor(BioLock.instance);
		File modFile = container.getSource();
		File baseFile = getBase();
		if (modFile.isDirectory()) {
			File srcFile = new File(modFile, BioLock.LUA_PATH);
			File destFile = new File(baseFile, BioLock.EXTRACTED_LUA_PATH);
			if (destFile.exists()) {
				return false;
			}
			try {
				ResourceExtractingUtils.copy(srcFile, destFile);
			} catch (IOException e) {
			}
		} else {
			File destFile = new File(BioLock.proxy.getBase(), BioLock.EXTRACTED_LUA_PATH);
			if (destFile.exists()) {
				return false;
			}
			ResourceExtractingUtils.extractZipToLocation(modFile, BioLock.LUA_PATH, BioLock.EXTRACTED_LUA_PATH);
		}
		return true;
	}

	public World getWorld(int dimId) {
		//overridden separately for client and server.
		return null;
	}

}
