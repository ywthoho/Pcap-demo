package com.github.username;

import java.io.IOException;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.PcapDumper;

/**
 * Hello world!
 *
 */
public class App 
{
    static PcapNetworkInterface getNetworkDevice() {
        PcapNetworkInterface device = null;
        try {
            device = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return device;
    }
    public static void main( String[] args ) {
        PcapNetworkInterface device = getNetworkDevice();
        System.out.println("You chose: " + device);

        if (device == null) {
            System.out.println("No device chosen.");
            System.exit(1);
        }

        // Open the device and get a handle
        int snapshotLength = 65536; // in bytes
        int readTimeout = 50; // in milliseconds
        final PcapHandle handle;
        handle = device.openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);

        // Create a listener that defines what to do with the received packets
        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                try {
                    dumper.dump(packet, handle.getTimestamp());
                } catch (NotOpenException e) {
                    e.printStackTrace();
                }
                dumper.close();

                // Override the default gotPacket() function and process packet
                System.out.println(handle.getTimestamp());
                System.out.println(packet);
            }
        };

        try {
            int maxPackets = 50;
            handle.loop(maxPackets, listener);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Cleanup when complete
        handle.close();
    }
}
