package pivot.demos.million;

import java.io.*;
import java.util.UUID;

public class ContactGenerator {
	public static void main(String[] args) throws Exception {
		File dbFile = new File(System.getProperty("user.home") + "/Desktop/pim-contact.csv");
		FileWriter writer = new FileWriter(dbFile);
		String seperator = ", ";

		for(int i = 0; i < 1000000; i++){
			StringBuffer record = new StringBuffer();
			record.append(getRandom(20));
			record.append(seperator);
			record.append(getRandom(12));
			record.append(seperator);
			record.append(getRandom(13));
			record.append(seperator);
			record.append(getRandom(13));
			record.append('\n');
			writer.append(record);
		}

		writer.close();
	}

	static String getRandom(int length) {
		// magic
		UUID uuid = UUID.randomUUID();
		String myRandom = uuid.toString();
		return myRandom.substring(length);
	}

}