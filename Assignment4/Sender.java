import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;


public class Sender {

	// Maximum Segment Size - Quantity of data from the application layer in the segment
	public static final int MSS = 8;

	// Probability of loss during packet sending
	public static final double PROBABILITY = 0.2;

	// Window size - Number of packets sent without acking
	public static final int WINDOW_SIZE = 2;
	
	// Time (ms) before REsending all the non-acked packets
	public static final int TIMER = 10000;

    static int generateChecksum(String s)
    {
        String hex_value = new String();
        // 'hex_value' will be used to store various hex values as a string
        int x, i, checksum = 0;
        // 'x' will be used for general purpose storage of integer values
        // 'i' is used for loops
        // 'checksum' will store the final checksum
        for (i = 0; i < s.length() - 2; i = i + 2)
        {
            x = (int) (s.charAt(i));
            hex_value = Integer.toHexString(x);
            x = (int) (s.charAt(i + 1));
            hex_value = hex_value + Integer.toHexString(x);
            // Extract two characters and get their hexadecimal ASCII values
            System.out.println(s.charAt(i) + "" + s.charAt(i + 1) + " : "
                    + hex_value);
            x = Integer.parseInt(hex_value, 16);
            // Convert the hex_value into int and store it
            checksum += x;
            // Add 'x' into 'checksum'
        }
        if (s.length() % 2 == 0)
        {
            // If number of characters is even, then repeat above loop's steps
            // one more time.
            x = (int) (s.charAt(i));
            hex_value = Integer.toHexString(x);
            x = (int)(s.charAt(i + 1));
            hex_value = hex_value + Integer.toHexString(x);
            System.out.println(s.charAt(i) + "" + s.charAt(i + 1) + " : "
                    + hex_value);
            x = Integer.parseInt(hex_value, 16);
        }
        else
        {
            // If number of characters is odd, last 2 digits will be 00.
            x = (int) (s.charAt(i));
            hex_value = "00" + Integer.toHexString(x);
            x = Integer.parseInt(hex_value, 16);
            System.out.println(s.charAt(i) + " : " + hex_value);
        }
        checksum += x;
        // Add the generated value of 'x' from the if-else case into 'checksum'
        hex_value = Integer.toHexString(checksum);
        // Convert into hexadecimal string
        if (hex_value.length() > 4)
        {
            // If a carry is generated, then we wrap the carry
            int carry = Integer.parseInt(("" + hex_value.charAt(0)), 16);
            // Get the value of the carry bit
            hex_value = hex_value.substring(1, 5);
            // Remove it from the string
            checksum = Integer.parseInt(hex_value, 16);
            // Convert it into an int
            checksum += carry;
            // Add it to the checksum
        }
        checksum = generateComplement(checksum);
        // Get the complement
        return checksum;
    }
    static int generateComplement(int checksum)
    {
        // Generates 15's complement of a hexadecimal value
        checksum = Integer.parseInt("FFFF", 16) - checksum;
        return checksum;
    }
    
	public static void main(String[] args) throws Exception{

		// Sequence number of the last packet sent (rcvbase)
		int lastSent = 0;
		
		// Sequence number of the last acked packet
		int waitingForAck = 0;

		// Data to be sent (you can, and should, use your own Data-> byte[] function here)
		byte[] fileBytes = "Hello This is group-6 Roll no. BT17CSE089 and BT17CSE077".getBytes();

		System.out.println("Data size: " + fileBytes.length + " bytes");

		// Last packet sequence number
		int lastSeq = (int) Math.ceil( (double) fileBytes.length / MSS);

		System.out.println("Number of packets to send: " + lastSeq);

		DatagramSocket toReceiver = new DatagramSocket();

		// Receiver address
		InetAddress receiverAddress = InetAddress.getByName("localhost");
		
		// List of all the packets sent
		ArrayList<RDTPacket> sent = new ArrayList<RDTPacket>();
		String ss;
		while(true){

			// Sending loop
			while(lastSent - waitingForAck < WINDOW_SIZE && lastSent < lastSeq){

				// Array to store part of the bytes to send
				byte[] filePacketBytes = new byte[MSS];
				int checksum;
				// Copy segment of data bytes to array
				filePacketBytes = Arrays.copyOfRange(fileBytes, lastSent*MSS, lastSent*MSS + MSS);
				ss = new String(filePacketBytes);
				checksum = generateChecksum(ss);
				System.out.println(checksum);
				// Create RDTPacket object
				RDTPacket rdtPacketObject = new RDTPacket(lastSent, filePacketBytes, checksum, (lastSent == lastSeq-1) ? true : false);

				// Serialize the RDTPacket object
				byte[] sendData = Serializer.toBytes(rdtPacketObject);

				// Create the packet
				DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddress, 9876 );

				System.out.println("Sending packet with sequence number " + lastSent +  " and size " + sendData.length + " bytes");

				// Add packet to the sent list
				sent.add(rdtPacketObject);
				
				// Send with some probability of loss
				if(Math.random() > PROBABILITY){
					toReceiver.send(packet);
				}else{
					System.out.println("[X] Lost packet with sequence number " + lastSent);
				}

				// Increase the last sent
				lastSent++;

			} // End of sending while
			
			// Byte array for the ACK sent by the receiver
			byte[] ackBytes = new byte[40];
			
			// Creating packet for the ACK
			DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
			
			try{
				// If an ACK was not received in the time specified (continues on the catch clausule)
				toReceiver.setSoTimeout(TIMER);
				
				// Receive the packet
				toReceiver.receive(ack);
				
				// Unserialize the RDTAck object
				RDTAck ackObject = (RDTAck) Serializer.toObject(ack.getData());
				
				System.out.println("Received ACK for " + ackObject.getPacket());
				
				// If this ack is for the last packet, stop the sender (Note: gbn has a cumulative acking)
				if(ackObject.getPacket() == lastSeq){
					break;
				}
				
				waitingForAck = Math.max(waitingForAck, ackObject.getPacket());
				
			}catch(SocketTimeoutException e){
				// then send all the sent but non-acked packets
				for(int i = waitingForAck; i < lastSent; i++){
					
					// Serialize the RDTPacket object
					byte[] sendData = Serializer.toBytes(sent.get(i));

					// Create the packet
					DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddress, 9876 );
					
					// Send with some probability
					if(Math.random() > PROBABILITY){
						toReceiver.send(packet);
					}else{
						System.out.println("[X] Lost packet with sequence number " + sent.get(i).getSeq());
					}

					System.out.println("REsending packet with sequence number " + sent.get(i).getSeq() +  " and size " + sendData.length + " bytes");
				}
			}
			
		
		}
		
		System.out.println("Finished transmission");

	}

}
