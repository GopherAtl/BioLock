package gopheratl.biolock.common;

import gopheratl.GopherCore.GCLog;
import gopheratl.GopherCore.GopherCore;
import gopheratl.GopherCore.InstanceDataManager;
import gopheratl.biolock.client.BiolockRenderer;
import gopheratl.biolock.common.network.PacketHandler;
import gopheratl.biolock.common.util.BLLog;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;



















import org.apache.logging.log4j.Level;

import com.example.examplemod.ExampleMod;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Mod(modid = BioLock.MODID, version = BioLock.VERSION)
public class BioLock
{
	public static final String MODID="biolock";
	public static final String VERSION="2.2";
	
	public static class Blocks {
		public static BlockBioLock bioLock;
		public static BlockPRB prb;
		public static BlockKeypadLock keypadLock;
	}
	
	public static class Config {
		public static int internalMemorySize;
	}
	
	/*public static CreativeTabs tab = new CreativeTabs("tabBiolocks") {
		public ItemStack getIconItemStack() {
			return new ItemStack(BioLock.Blocks.bioLock);
		}

		@Override
		public Item getTabIconItem() {
			return new ItemStack(BioLock.Blocks.bioLock).getItem();
		}
	};*/
	
	@Instance(value = "BioLock")
	public static BioLock instance;
			
	public class PeripheralProvider implements IPeripheralProvider {
		@Override
		public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
			TileEntity entity = world.getTileEntity(x, y, z);
			if (entity instanceof TileEntityProgrammable) {
				return (IPeripheral)entity;
			}
			return null;
	   }
	}
	
	//public int[] foo={1,2,3};
	
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
		BLLog.debug("Resetting instance managers..");
		instanceManagers.put(TileEntityBioLock.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityBioLock.getBaseInstanceFileName()));
		instanceManagers.put(TileEntityPRB.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityPRB.getBaseInstanceFileName()));
		instanceManagers.put(TileEntityKeypadLock.class.getSimpleName(), new InstanceDataManager("BioLocks",TileEntityKeypadLock.getBaseInstanceFileName()));

		TileEntityBioLock.loadClassDataForWorld();

	}
	
	@EventHandler
	public void preInit( FMLPreInitializationEvent evt )
	{
		long time = System.nanoTime();
		BLLog.init();
		GCLog.init();
		BLLog.debug("Starting pre-init");
				
		Configuration configFile = new Configuration(evt.getSuggestedConfigurationFile());
		
		Property prop = configFile.get( "general", "bioLock_InternalMemorySize", 16);
		prop.comment = "Max number of prints that can be stored in BioLock's internal memory";
		Config.internalMemorySize=prop.getInt(16);
		
		configFile.save();
		
		proxy.preInit();
		
		BLLog.debug("Finished pre-init in %d ms", (System.nanoTime() - time) / 1000000);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		long time = System.nanoTime();
		BLLog.debug("Starting init");
		
		ComputerCraftAPI.registerPeripheralProvider(new PeripheralProvider());
		
		PacketHandler.init();
		
		proxy.init();
		
		BLLog.debug("Finished init in %d ms", (System.nanoTime() - time) / 1000000);
	}	
	
}
