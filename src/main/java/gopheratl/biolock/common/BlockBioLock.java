package gopheratl.biolock.common;

import gopheratl.biolock.client.BiolockRenderer;
import gopheratl.biolock.common.util.BLLog;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBioLock extends BlockProgrammable {
	
	static IIcon textureTop;
	static IIcon textureSide;
	static IIcon textureBottom;
	static IIcon[] texturesFront;
	
	public BlockBioLock() 
	{
		super(TileEntityBioLock.class);
		setCreativeTab(CreativeTabs.tabMisc);
	}	
	
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		textureTop=iconRegister.registerIcon("biolock:biolock_top");
		textureBottom=iconRegister.registerIcon("biolock:biolock_bottom");
		textureSide=iconRegister.registerIcon("biolock:biolock_side");
		
		texturesFront=new IIcon[13];
		for (int i=0; i<13; ++i)
			texturesFront[i]=iconRegister.registerIcon("biolock:biolock_front"+i);		
		
	}

	public int getRenderType() {
		return BiolockRenderer.model;
	}

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
  
        if (side == 0)
            return textureBottom;

        if (side == 1)
    		return textureTop;
        
        int facingSide = blockAccess.getBlockMetadata(x,y,z);
        TileEntityBioLock tileEntity = (TileEntityBioLock)blockAccess.getTileEntity(x,y,z);
        BLLog.debug("Block frame index: %d", tileEntity.clientFrameIndex);
        return side == facingSide ? texturesFront[ tileEntity.clientFrameIndex ]: textureSide;
    }
   
    
    //called when rendering as block in inventory
	@Override
	public IIcon getIcon(int side, int par2)
    {
        return side == 1 ? textureTop : side == 4 ? texturesFront[9] : textureSide;
    }
    
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {    	
		TileEntityBioLock tebl = (TileEntityBioLock)world.getTileEntity(x,y,z);
	 	if (player.isSneaking())
	 		return false;
	 	
	 	if (world.getBlockMetadata(x, y, z)==side)
	 	{
	 		tebl.doActivation(player);
	 		return true;
	 	}
	
        return false;
    }
    
        
}
