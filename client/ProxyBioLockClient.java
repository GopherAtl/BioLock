package gopheratl.biolock.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import gopheratl.biolock.common.BioLock;
import gopheratl.biolock.common.ProxyBioLock;

public class ProxyBioLockClient extends ProxyBioLock 
{
	
	@Override
    public void registerRenderInformation()
    {
		CreativeTabs computerTab=CreativeTabs.tabAllSearch;
		CreativeTabs[] tabs=CreativeTabs.creativeTabArray;
		for(int i=0; i<tabs.length; ++i)
		{
			if (tabs[i].getTabLabel()=="ComputerCraft")
			{
				computerTab=tabs[i];
				break;
			}			
		}
		
		//IItemRenderer bioLockRenderer;
		//MinecraftForgeClient.registerItemRenderer( BioLock.Config.bioLockBlockID,bioLockRenderer);
				
		BioLock.bioLock.setCreativeTab(computerTab);		
		BioLock.prb.setCreativeTab(computerTab);		
    }
	
}
