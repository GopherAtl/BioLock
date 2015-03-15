package gopheratl.biolock.common.network;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerProgrammable implements IMessageHandler<PacketProgrammable, IMessage> {

	@Override
	public IMessage onMessage(PacketProgrammable message, MessageContext ctx) {
		return null;
	}
	

}
