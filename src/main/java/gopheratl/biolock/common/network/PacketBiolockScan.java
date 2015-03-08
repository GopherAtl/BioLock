package gopheratl.biolock.common.network;

import io.netty.buffer.ByteBuf;

public class PacketBiolockScan extends PacketProgrammable {
	
	public PacketBiolockScan() {
		//intentionally empty
	}
	
	public PacketBiolockScan(short instance, int x, int y, int z) {
		this.id = 2;
		this.instance = instance;
		this.x = x;
		this.y = y;
		this.z = z;
	}
		
}
