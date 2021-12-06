import com.fazecast.jSerialComm.SerialPort;

import java.util.logging.Logger;

public class SenseairS8Driver {
    private static final Logger LOGGER = Logger.getLogger(SenseairS8Driver.class.getSimpleName());

    private static final byte[] READ_COMMAND = {
            (byte) 0xFE,
            (byte) 0x44,
            (byte) 0x00,
            (byte) 0x08,
            (byte) 0x02,
            (byte) 0x9F,
            (byte) 0x25 };

    public Integer measure() {
        SerialPort port = SerialPort.getCommPort("/dev/ttyAMA0");

        if (!port.openPort()) {
            LOGGER.warning("Failed to open port.");
            return null;
        }

        port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
                500,
                500);

        port.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

        Integer value = null;

        try {
            if (port.writeBytes(READ_COMMAND, READ_COMMAND.length) != READ_COMMAND.length) {
                LOGGER.warning("Failed to write bytes.");
                return null;
            }

            byte[] responseBytes = new byte[7];

            if (port.readBytes(responseBytes, 7) == 7)
                value = (Byte.toUnsignedInt(responseBytes[3]) << 8) + (Byte.toUnsignedInt(responseBytes[4]));
            else
                LOGGER.warning("Failed to read bytes.");
        }
        finally {
            if (!port.closePort())
                LOGGER.warning("Failed to close port.");
        }

        return value;
    }

}
