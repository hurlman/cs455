package cs455.overlay.wireformats;

import cs455.overlay.wireformats.Protocol.MessageType;
import java.io.IOException;

public interface Event {
	MessageType getType();

	byte[] getBytes() throws IOException;
}
