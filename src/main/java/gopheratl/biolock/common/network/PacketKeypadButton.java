package gopheratl.biolock.common.network;

import io.netty.buffer.ByteBuf;

public class PacketKeypadButton extends PacketProgrammable {
	int button;
	
	public PacketKeypadButton() {
		//intentionally empty
	}
	
	public PacketKeypadButton(short instance, int dim, int x, int y, int z, int button) {
		this.id = 1;
		this.instance = instance;
		this.dimension = dim;
		this.x = x;
		this.y = y;
		this.z = z;
		this.button = button;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(button);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		button = buf.readInt();
	}
}
