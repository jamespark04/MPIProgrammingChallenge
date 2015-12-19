import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Main method for the MisMatch Detector program
 * Detects mismatches in a MPI log file
 * @author jpark
 *
 */
public class MismatchDetector {
	// maps where the key is tag number, and the value is a map from id number to message info
	// note: I chose ot use a hashmap mainly because for practical usage
	// its expected running time is better than a treemap
	private static Map<Integer, Map<Integer, List<SendMessage>>> send = new HashMap<Integer, Map<Integer, List<SendMessage>>>();
	private static Map<Integer, Map<Integer, List<ReceiveMessage>>> receive = new HashMap<Integer, Map<Integer, List<ReceiveMessage>>>();	
	
	/**
	 * This function creates a new Send Message object from the parameters
	 * and appropriately inserts it into the send map
	 * @param id - id of the message (source)
	 * @param count - count/length of the message
	 * @param dest - destination id of the message
	 * @param tag - tag number of the message
	 */
	private static void insertSendMessage(int id, int count, int dest, int tag) {
		SendMessage s = new SendMessage(tag, id, dest, count);
		if (send.containsKey(tag)) { // checks if the map already contains the tag
			Map<Integer, List<SendMessage>> temp = send.get(tag);
			if (temp.containsKey(id)) { // checks if the inner map already contains the source id
				// inserts the object into the list, then inserts the list into the map, 
				// then inserts the inner map into the outer map
				List<SendMessage> sm = temp.get(id);
				sm.add(s);
				temp.put(id, sm);
				send.put(tag, temp);
			}
			else {
				List<SendMessage> sm = new LinkedList<SendMessage>();
				sm.add(s);
				temp.put(id, sm);
				send.put(tag, temp);				
			}
		}
		else {
			Map<Integer, List<SendMessage>> temp = new HashMap<Integer, List<SendMessage>>();
			List<SendMessage> sm = new LinkedList<SendMessage>();
			sm.add(s);
			temp.put(id, sm);
			send.put(tag, temp);				
		}
	}
	
	/**
	 * This function operates similar to insertSendMessage,
	 * except it uses the receive map instead
	 * @param id - the id of the destination
	 * @param count - the count/length of the message
	 * @param source - the source of the message
	 * @param tag - the tag number of the message
	 */
	private static void insertReceiveMessage(int id, int count, int source, int tag) {
		ReceiveMessage s = new ReceiveMessage(tag, id, source, count);
		if (receive.containsKey(tag)) {
			Map<Integer, List<ReceiveMessage>> temp = receive.get(tag);
			if (temp.containsKey(id)) {
				List<ReceiveMessage> rm = temp.get(id);
				rm.add(s);
				temp.put(id, rm);
				receive.put(tag, temp);
			}
			else {
				List<ReceiveMessage> rm = new LinkedList<ReceiveMessage>();
				rm.add(s);
				temp.put(id, rm);
				receive.put(tag, temp);				
			}
		}
		else {
			Map<Integer, List<ReceiveMessage>> temp = new HashMap<Integer, List<ReceiveMessage>>();
			List<ReceiveMessage> rm = new LinkedList<ReceiveMessage>();
			rm.add(s);
			temp.put(id, rm);
			receive.put(tag, temp);				
		}
	}
	
	/**
	 * This method will attempt to create a message object from a 
	 * line from the MPI log file
	 * @param s - the string to be parsed
	 */
	private static void parseLogLine(String s) {
		/**
		 * First we initialize the regex patterns to parse
		 * There are only two main patterns, since we only
		 * need to parse send and receive messages
		 */
		String processPattern = "\\[(?<id>\\d+)\\]";
		String suffixDestPattern = "\\swith\\scount\\s\\=\\s(?<count>\\d+)\\,\\sdest\\s\\=\\s(?<dest>\\d+)\\,\\stag\\s\\=\\s(?<tag>\\d+)\\s\\.\\.\\.";
		String suffixSourcePattern = "\\swith\\scount\\s\\=\\s(?<count>\\d+)\\,\\ssource\\s\\=\\s(?<source>\\d+)\\,\\stag\\s\\=\\s(?<tag>\\d+)\\s\\.\\.\\.";
		String sendStringPattern = processPattern + "\\sStarting\\sMPI\\_Isend" + suffixDestPattern;
		String receiveStringPattern = processPattern + "\\sStarting\\sMPI\\_Irecv" + suffixSourcePattern;
		Pattern sendPattern = Pattern.compile(sendStringPattern);
		Pattern receivePattern = Pattern.compile(receiveStringPattern);
		Matcher sendMatcher = sendPattern.matcher(s);
		Matcher receiveMatcher = receivePattern.matcher(s);
		
		if (sendMatcher.matches()) { // if the send message regex is matched
			try {
				int id = Integer.parseInt(sendMatcher.group("id"));
				int count = Integer.parseInt(sendMatcher.group("count"));
				int dest = Integer.parseInt(sendMatcher.group("dest"));
				int tag = Integer.parseInt(sendMatcher.group("tag"));
				
				if (receive.containsKey(tag)) { // checks if the receive map contains this same tag
					Map<Integer, List<ReceiveMessage>> receiveTags = receive.get(tag);
					if (receiveTags.containsKey(dest)) { 
						// checks if the inner receive map contains 
						// the same destination id as the destination id of the
						// current send message
						List<ReceiveMessage> rml = receiveTags.get(dest);
						int index = -1;
						for (int i = 0; i < rml.size(); i++) {
							if (rml.get(i).count == count && rml.get(i).source == id) { 
								// if the count and source are the same
								// we have found a successful one to one match
								index = i;
								break;
							}
						}
						if (index > -1) {
							rml.remove(index); // remove the match from the map
							if (rml.size() == 0) {
								receiveTags.remove(dest);
								if (receiveTags.size() == 0) {
									receive.remove(tag);
								}
								else {
									receive.put(tag, receiveTags);
								}
							}
							else {
								receiveTags.put(dest, rml);
								receive.put(tag, receiveTags);
							}
						}
						else {
							insertSendMessage(id, count, dest, tag);
						}
					}
					else {
						insertSendMessage(id, count, dest, tag);	
					}
				}
				else {
					insertSendMessage(id, count, dest, tag);
				}
			}
			catch(NumberFormatException e) {
				System.out.println("One of the attributes of the message is not a number");
				e.printStackTrace();
			}
		}
		else if (receiveMatcher.matches()) { // if the receive message regex is matched
			// same logic as the previous branch of send
			// except we use the send map and match against the source id instead
			int id = Integer.parseInt(receiveMatcher.group("id"));
			int count = Integer.parseInt(receiveMatcher.group("count"));
			int source = Integer.parseInt(receiveMatcher.group("source"));
			int tag = Integer.parseInt(receiveMatcher.group("tag"));
			if (send.containsKey(tag)) {
				Map<Integer, List<SendMessage>> sendTags = send.get(tag);
				if (sendTags.containsKey(source)) {
					List<SendMessage> sml = sendTags.get(source);
					int index = -1;
					for (int i = 0; i < sml.size(); i++) {
						if (sml.get(i).count == count && sml.get(i).destination == id) {
							index = i;
							break;
						}
					}
					if (index > -1) {
						sml.remove(index);
						if (sml.size() == 0) {
							sendTags.remove(source);
							if (sendTags.size() == 0) {
								send.remove(tag);
							}
							else {
								send.put(tag, sendTags);
							}
						}
						else {
							sendTags.put(source, sml);
							send.put(tag, sendTags);
						}
					}
					else {
						insertReceiveMessage(id, count, source, tag);
					}
				}
				else {
					insertReceiveMessage(id, count, source, tag);
				}
			}
			else {
				insertReceiveMessage(id, count, source, tag);
			}
		}

	}

	
	/**
	 * Main method for the program
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please run the program with a file name");
		}
		else if (args.length > 1) {
			System.out.println("Please enter only one argument");
		}
		else {
			try {
				String filepath = args[0];
				BufferedReader br = new BufferedReader(new FileReader(filepath));
				List<String> mismatches = new LinkedList<String> ();

				String next;
				// read in the file line by line until EOF
				while((next = br.readLine()) != null) {
					parseLogLine(next);
				}
				br.close();
				
				// iterate through the maps
				// if any values are remaining in the maps
				// it means that one-to-one matchings
				// were not found for those values
				for (Integer i: send.keySet()) {
					for (Integer j: send.get(i).keySet()) {
						for (SendMessage s: send.get(i).get(j)) {
							mismatches.add(s.toString());
						}
					}
				}
				for (Integer i: receive.keySet()) {
					for (Integer j: receive.get(i).keySet()) {
						for (ReceiveMessage r: receive.get(i).get(j)) {
							mismatches.add(r.toString());
						}
					}
				}
				
				if (mismatches.size() == 0) {
					System.out.println("No mismatches in this log file.");
				}
				else {
					System.out.println("Mismatches detected");
					for (String m: mismatches) {
						System.out.println(m);
					}
				}
				
			}
			catch(IOException e) {
				// most likely a file not found exception
				// from a nonexistant filepath
				// best to just print stack trace to screen
				e.printStackTrace();
			}
		}
	}
}
