package gopheratl.biolock.common;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gopheratl.GopherCore.InstanceDataManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemBlockProgrammable extends ItemBlock {

	public ItemBlockProgrammable(int id)
	{
		super(id);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}
	
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
		boolean result=super.placeBlockAt(stack, player, world, x, y, z, side, hitX,hitY, hitZ, metadata);
		if (result && !world.isRemote)
        {
        	TileEntity te=world.getBlockTileEntity(x,y,z);
        	if (te==null)
        		System.out.println("[BioLock] [PRB] TE is null!");
        	
        	if (te instanceof TileEntityProgrammable)
        	{
        		TileEntityProgrammable tep=(TileEntityProgrammable)te;
        		if (stack.getItemDamage()!=0)
        			tep.setInstanceID(stack.getItemDamage());
        		else
        		{
        			InstanceDataManager instanceManager=BioLock.getInstanceManager(te.getClass());
        			if (instanceManager!=null)
        				System.out.println("[BioLock] [PRB] instanceManager is null!");
                	tep.setInstanceID(instanceManager.getNextID());
        		}
        	}
        }
        return result;
    }

	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List tipList, boolean par4)
    {
		int dmg=itemStack.getItemDamage();
		if (dmg>0)
		{
			tipList.add("#"+dmg);
		}
    }

    @SideOnly(Side.SERVER)
    @Override
    public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int par4) 
    {
    	par3EntityPlayer.sendChatToPlayer("You banged the ... uhm, whichever kind of programmable block this is... "+par4+" times.");
    	System.out.println("[BioLock] BANG BANG BANG");
    }

}
