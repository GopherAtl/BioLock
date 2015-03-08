package gopheratl.biolock.common.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import gopheratl.biolock.common.network.*;

public class PacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("biolock");
	private static int id = 0;
	
	public static void init() {
		INSTANCE.registerMessage(HandlerKeypadButton.class, PacketKeypadButton.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(HandlerBiolockScan.class, PacketBiolockScan.class, id++, Side.CLIENT);
		
	}
}
