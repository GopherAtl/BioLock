package gopheratl.biolock.common;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeResetProgrammable implements IRecipe {

	ItemStack output;
	
	public RecipeResetProgrammable(Block block)
	{
		output=new ItemStack(block);
	}
	
	@Override
	public boolean matches(InventoryCrafting inventorycrafting, World world) {
		boolean found=false;
		for(int i=0;i<inventorycrafting.getSizeInventory();++i)
		{
			ItemStack stack=inventorycrafting.getStackInSlot(i);
			if (stack!=null && stack.getItem()==output.getItem())
			{
				if (found)
					return false;
				found=true;
			}
		}
		return found;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
		return output.copy();
	}

	@Override
	public int getRecipeSize() {
		return 1;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return output;
	}

}
