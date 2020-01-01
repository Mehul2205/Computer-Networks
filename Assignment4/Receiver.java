import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;


public class Receiver {
	
	// Probability of ACK loss
	public static final double PROBABILITY = 0.2;

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
            x = (int) (s.charAt(i + 1));
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
    
	
    static boolean receive(String s, int checksum)
    {
        int generated_checksum = generateChecksum(s);
        // Calculate checksum of received data
        generated_checksum = generateComplement(generated_checksum);
        // Then get its complement, since generated checksum is complemented
        int syndrome = generated_checksum + checksum;
        // Syndrome is addition of the 2 checksums
        syndrome = generateComplement(syndrome);
        // It is complemented
        System.out.println("Syndrome = " + Integer.toHexString(syndrome));
        if (syndrome == 0)
        {
            return(true);
        }
        else
        {
        	return(false);
        }
    }	
	
	public static void main(String[] args) throws Exception{
		
		DatagramSocket fromSender = new DatagramSocket(9876);
		
		// 83 is the base size (in bytes) of a serialized RDTPacket object 
		byte[] receivedData = new byte[102];
		
		int waitingFor = 0;
		
		ArrayList<RDTPacket> received = new ArrayList<RDTPacket>();
		
		boolean end = false;
		String ss;
		while(!end){
			
			System.out.println("Waiting for packet");
			
			// Receive packet
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			fromSender.receive(receivedPacket);
			
			// Unserialize to a RDTPacket object
			RDTPacket packet = (RDTPacket) Serializer.toObject(receivedPacket.getData());
			
			System.out.println("Packet with sequence number " + packet.getSeq() + " received (last: " + packet.isLast() + " )");
			ss = new String(packet.getData());
			if(receive(ss, packet.getChecksum()))
			{
				if(packet.getSeq() == waitingFor && packet.isLast()){
					
					waitingFor++;
					received.add(packet);
					
					System.out.println("Last packet received");
					
					end = true;
					
				}else if(packet.getSeq() == waitingFor){
					waitingFor++;
					received.add(packet);
					System.out.println("Packed stored in buffer");
				}else{
					System.out.println("Packet discarded (not in order)");
				}

			}else {
				System.out.println("Packet discarded because checksum not right");
			}
			// Create an RDTAck object
			RDTAck ackObject = new RDTAck(waitingFor);
			
			// Serialize
			byte[] ackBytes = Serializer.toBytes(ackObject);
			
			
			DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, receivedPacket.getAddress(), receivedPacket.getPort());
			
			// Send with some probability of loss
			if(Math.random() > PROBABILITY){
				fromSender.send(ackPacket);
			}else{
				System.out.println("[X] Lost ack with sequence number " + ackObject.getPacket());
			}
			
			System.out.println("Sending ACK to seq " + waitingFor + " with " + ackBytes.length  + " bytes");
			

		}
		
		// Print the data received
		System.out.println(" ------------ DATA ---------------- ");
		
		for(RDTPacket p : received){
			for(byte b: p.getData()){
				System.out.print((char) b);
			}
		}
		
	}
	
	
}
