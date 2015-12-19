/**
 * This class constructs a new receive message object
 * @author jpark
 *
 */
public class ReceiveMessage {

	/**
	 * These are package protected variables,
	 * so the MismatchDetector class can directly manipulate these variables
	 * 
	 */
	int tag;
	int id;
	int source;
	int count;
	
	/**
	 * Constructor for the Message object
	 * @param tag - is the tag number
	 * @param id - is the receiving process's id
	 * @param source - is the source id
	 * @param count - the count of the message length
	 */
	public ReceiveMessage(int tag, int id, int source, int count) {
		this.tag = tag;
		this.id = id;
		this.source = source;
		this.count = count;
	}

	@Override
	public String toString() {
		return String.format("Receive: source=%d, tag=%d, dest = %d, count=%d", source, tag, id, count);
	}
	
}
