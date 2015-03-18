package gopheratl.biolock.common;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPRB extends BlockProgrammable {

	static IIcon textureFrontOff;
	static IIcon textureFrontOn;
	static IIcon textureSideOff;
	static IIcon textureSideOn;
	
	public BlockPRB()
	{
		super(TileEntityPRB.class);
		
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
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

		Block block = world.getBlock(x, y, z);
		if (block instanceof BlockRedstoneWire)
		{
			//System.out.println("[BioLock] [DEBUG] redstoneWire, using metadata "+world.getBlockMetadata(x,y,z));
			return world.getBlockMetadata(x, y, z);
		}
		
		return Math.max(world.isBlockProvidingPowerTo(x, y, z, side),world.getIndirectPowerLevelTo(x, y, z, side));
	}
	
	public void updateSideInputs(World world, int x, int y, int z)
	{
		TileEntityPRB prb=(TileEntityPRB)world.getTileEntity(x, y, z);
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
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		updateSideInputs(world,x,y,z);
	}
	
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
    	int facingSide = blockAccess.getBlockMetadata(x,y,z);
        TileEntityPRB tileEntity = (TileEntityPRB)blockAccess.getTileEntity(x,y,z);               
        //System.out.println("[BioLock] [DEBUG] getBlockTexture - side "+side+", isPowering=="+tileEntity.isPowering(side)+", facing=="+facingSide);
        boolean isOn=tileEntity!=null ? tileEntity.isPowering(side) : false;
        return side == facingSide ? (isOn ? textureFrontOn : textureFrontOff) : (isOn ? textureSideOn : textureSideOff);
    }
  
    //called when rendering as block in inventory
  	@Override
  	public IIcon getIcon(int side, int par2)
    {
  		return side == 4 ? textureFrontOff : textureSideOff;
    }
}
