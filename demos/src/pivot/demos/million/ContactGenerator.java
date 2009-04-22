/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
