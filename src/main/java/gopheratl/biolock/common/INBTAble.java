package gopheratl.biolock.common;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public interface INBTAble {
	public boolean readFromNBT(NBTBase nbt);
	public void writeToNBT(NBTTagCompound nbt, String name);
}
