package gopheratl.biolock.common;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBioLock extends BlockProgrammable {
    
	static Icon textureTop;
	static Icon textureSide;
	static Icon textureBottom;
	static Icon[] texturesFront;
	
	public BlockBioLock(int id) 
	{
		super(id,TileEntityBioLock.class);
	}	
	
	public void registerIcons(IconRegister iconRegister)
	{
		textureTop=iconRegister.registerIcon("BioLock:biolock_top");
		textureBottom=iconRegister.registerIcon("BioLock:biolock_bottom");
		textureSide=iconRegister.registerIcon("BioLock:biolock_side");
		
		texturesFront=new Icon[13];
		for (int i=0; i<13; ++i)
			texturesFront[i]=iconRegister.registerIcon("BioLock:biolock_front"+i);		
		
	}

    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        if (side == 0)
            return textureBottom;

        if (side == 1)
    		return textureTop;
        
        int facingSide = blockAccess.getBlockMetadata(x,y,z);
        TileEntityBioLock tileEntity = (TileEntityBioLock)blockAccess.getBlockTileEntity(x,y,z);       
        return side == facingSide ? texturesFront[ tileEntity.faceFrameIndex ]: textureSide;
    }
   
    
    //called when rendering as block in inventory
	@Override
	public Icon getIcon(int side, int par2)
    {
        return side == 1 ? textureTop : side == 4 ? texturesFront[9] : textureSide;
    }
    
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {    	
		TileEntityBioLock tebl = (TileEntityBioLock)world.getBlockTileEntity(x,y,z);
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
