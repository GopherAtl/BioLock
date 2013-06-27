package gopheratl.biolock.common;

import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPRB extends BlockProgrammable {

	static Icon textureFrontOff;
	static Icon textureFrontOn;
	static Icon textureSideOff;
	static Icon textureSideOn;
	
	public BlockPRB(int id)
	{
		super(id,TileEntityPRB.class);
		
	}
	
	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		textureFrontOff=iconRegister.registerIcon("BioLock:plc_front");
		textureFrontOn=iconRegister.registerIcon("BioLock:plc_front2");

		textureSideOff=iconRegister.registerIcon("BioLock:plc_side");
		textureSideOn=iconRegister.registerIcon("BioLock:plc_side2");
				
	}

	public int getInputFrom(World world, int x, int y, int z, int side)
	{
		//System.out.println("[BioLock] [DEBUG] BlockPRB:getInputFrom - side="+side);
		
		//System.out.println("[BioLock] [DEBUG] isBlockProvidingPowerTo -> "+world.isBlockProvidingPowerTo(x, y, z, side)+", getIndirectPowerLevelTo -> "+world.getIndirectPowerLevelTo(x, y, z, side));

		int blockID=world.getBlockId(x, y, z);
		if (blockID==Block.redstoneWire.blockID)
		{
			//System.out.println("[BioLock] [DEBUG] redstoneWire, using metadata "+world.getBlockMetadata(x,y,z));
			return world.getBlockMetadata(x, y, z);
		}
		
		return Math.max(world.isBlockProvidingPowerTo(x, y, z, side),world.getIndirectPowerLevelTo(x, y, z, side));
	}
	
	public void updateSideInputs(World world, int x, int y, int z)
	{
		TileEntityPRB prb=(TileEntityPRB)world.getBlockTileEntity(x, y, z);
		//up/down
		prb.setSideInput(0, getInputFrom(world,x,y-1,z,1));
		prb.setSideInput(1, getInputFrom(world,x,y+1,z,0));
		//north/south
		prb.setSideInput(3, getInputFrom(world,x,y,z+1,2));
		prb.setSideInput(2, getInputFrom(world,x,y,z-1,3));
		//east/west
		prb.setSideInput(4, getInputFrom(world,x-1,y,z,5));
		prb.setSideInput(5, getInputFrom(world,x+1,y,z,4));
		
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		updateSideInputs(world,x,y,z);
	}
	
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
    	int facingSide = blockAccess.getBlockMetadata(x,y,z);
        TileEntityPRB tileEntity = (TileEntityPRB)blockAccess.getBlockTileEntity(x,y,z);               
        //System.out.println("[BioLock] [DEBUG] getBlockTexture - side "+side+", isPowering=="+tileEntity.isPowering(side));
        boolean isOn=tileEntity!=null ? tileEntity.isPowering(side) : false;
        return side == facingSide ? (isOn ? textureFrontOn : textureFrontOff) : (isOn ? textureSideOn : textureSideOff);
    }
  
    //called when rendering as block in inventory
  	@Override
  	public Icon getIcon(int side, int par2)
    {
  		return side == 4 ? textureFrontOff : textureSideOff;
    }
}
