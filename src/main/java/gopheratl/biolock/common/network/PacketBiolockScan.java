package gopheratl.biolock.common.network;

import io.netty.buffer.ByteBuf;

public class PacketBiolockScan extends PacketProgrammable {
	int frame;
	
	public PacketBiolockScan() {
		//intentionally empty
	}
	
	public PacketBiolockScan(short instance, int x, int y, int z, int frame) {
		this.id = 2;
		this.instance = instance;
		this.x = x;
		this.y = y;
		this.z = z;
		this.frame = frame;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(frame);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		frame = buf.readInt();
	}
}
