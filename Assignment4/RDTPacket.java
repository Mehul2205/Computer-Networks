import java.io.Serializable;
import java.util.Arrays;


public class RDTPacket implements Serializable {

	public int seq;
	
	public byte[] data;
	public int checksum;
	public boolean last;

	public RDTPacket(int seq, byte[] data,int checksum, boolean last) {
		super();
		this.seq = seq;
		this.data = data;
		this.checksum = checksum;
		this.last = last;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}
	
	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public String tostring() {
		return ("UDPPacket [seq=" + seq + ", data=" + Arrays.toString(data)+" Checksum = " + checksum + ", last=" + last + "]");
	}
	
}
