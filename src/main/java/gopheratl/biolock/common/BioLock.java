package gopheratl.biolock.common;

import gopheratl.GopherCore.GopherCore;
import gopheratl.GopherCore.InstanceDataManager;
import gopheratl.biolock.common.util.BLLog;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;





import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid="BioLock", name="BioLock", version="2.0b")
public class BioLock
{
	public static class Blocks {
		public static BlockBioLock bioLock;
		public static BlockPRB prb;
		public static BlockKeypadLock keypadLock;
	}
	
	public static class Config {
		public static int internalMemorySize;
	}
	
	@Instance(value = "Biolock")
	public static BioLock instance;
	
	public int[] foo={1,2,3};
	
	private static HashMap<String,InstanceDataManager> instanceManagers=new HashMap<String,InstanceDataManager>();	
	
	public static InstanceDataManager getInstanceManager(Class c)
	{
		String className=c.getSimpleName();
		if (!instanceManagers.containsKey(className))
			try {
				instanceManagers.put(className, new InstanceDataManager("BioLocks", (String)c.getMethod("getBaseInstanceFileName").invoke(null) ));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("[BioLock] Failed to create InstanceManager for class "+c.getName()+"! is getBaseInstanceFileName() defined?");
				e.printStackTrace();
			}			

		return instanceManagers.get(className);			
	}
	@SidedProxy(clientSide = "gopheratl.biolock.client.ProxyBioLockClient", serverSide= "gopheratl.biolock.server.ProxyBioLockServer")
    public static ProxyBioLock proxy;


	public void resetInstanceManagers()
	{
		//System.out.println("[BioLock] [DEBUG] Resetting instance managers..");
		instanceManagers.put(TileEntityBioLock.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityBioLock.getBaseInstanceFileName()));
		instanceManagers.put(TileEntityPRB.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityPRB.getBaseInstanceFileName()));
		instanceManagers.put(TileEntityKeypadLock.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityKeypadLock.getBaseInstanceFileName()));

		TileEntityBioLock.loadClassDataForWorld();

	}
	
	public void preInit( FMLPreInitializationEvent evt )
	{
		BLLog.init();
		
		Configuration configFile = new Configuration(evt.getSuggestedConfigurationFile());
		
		Property prop = configFile.get( "general", "bioLock_InternalMemorySize", 16);
		prop.comment = "Max number of prints that can be stored in BioLock's internal memory";
		Config.internalMemorySize=prop.getInt(16);
		
		configFile.save();
	}

	public void load(FMLInitializationEvent event)
	{				
		ItemStack stone = new ItemStack((Block)Block.blockRegistry.getObject("stone"));
		ItemStack redstone = new ItemStack((Item)Item.itemRegistry.getObject("redstone"));
		ItemStack redstone_block = new ItemStack((Block)Block.blockRegistry.getObject("redstone_block"));
		ItemStack glass_pane = new ItemStack((Block)Block.blockRegistry.getObject("glass_pane"));
		ItemStack stone_button = new ItemStack((Block)Block.blockRegistry.getObject("stone_button"));
		
		BioLock.Blocks.bioLock=new BlockBioLock();
		BioLock.Blocks.bioLock.setBlockName("BioLockBlock");
		
		GameRegistry.registerBlock(Blocks.bioLock,ItemBlockProgrammable.class,"blockBioLock");
		GameRegistry.registerTileEntity(TileEntityBioLock.class, "BioLockPeripheral"); 
		GameRegistry.addRecipe(new ItemStack(Blocks.bioLock), new Object[] { "SGS","SRS","SGS",'S',stone,'R',redstone,'G',glass_pane});
		GameRegistry.addRecipe(new RecipeResetProgrammable(Blocks.bioLock));

		BioLock.Blocks.prb=new BlockPRB();
		BioLock.Blocks.prb.setBlockName("PRB");
		
		GameRegistry.registerBlock(Blocks.prb,ItemBlockProgrammable.class,"blockPRB");
		GameRegistry.registerTileEntity(TileEntityPRB.class, "PRBPeripheral");
		GameRegistry.addRecipe(new ItemStack(Blocks.prb),new Object[] { "SRS","RBR","SRS",'S',stone,'R',redstone,'B',redstone_block});		
		GameRegistry.addRecipe(new RecipeResetProgrammable(Blocks.prb));
		
		BioLock.Blocks.keypadLock=new BlockKeypadLock();
		BioLock.Blocks.keypadLock.setBlockName("KeypadLockBlock");
		
		GameRegistry.registerBlock(Blocks.keypadLock,ItemBlockProgrammable.class,"blockKeypadLockBlock");
		GameRegistry.registerTileEntity(TileEntityKeypadLock.class,"KeypadLockPeripheral");
		GameRegistry.addRecipe(new ItemStack(Blocks.keypadLock), new Object[] { "BBB", "BBB", "BBB", 'B', stone_button});
		GameRegistry.addRecipe(new RecipeResetProgrammable(Blocks.keypadLock));
		
		proxy.registerRenderInformation();		
	}	
	
	public void serverStarted(FMLServerStartedEvent event)
	{
	}

	
	
	
}
