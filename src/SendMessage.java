/**
 * This class constructs a new send message object
 * @author jpark
 *
 */
public class SendMessage {

	/**
	 * These are package protected variables,
	 * so the MismatchDetector class can directly manipulate these variables
	 * 
	 */
	int tag;
	int id;
	int destination;
	int count;
	
	/**
	 * Constructor for the Message object
	 * @param tag - is the tag number
	 * @param id - is this source process's id
	 * @param destination - is the target id
	 * @param count - the count of the message length
	 */
	public SendMessage(int tag, int id, int destination, int count) {
		this.tag = tag;
		this.id = id;
		this.destination = destination;
		this.count = count;
	}
	
	@Override
	public String toString() {
		return String.format("Send: source=%d, tag=%d, dest = %d, count=%d", id, tag, destination, count);
	}
	
	
}
