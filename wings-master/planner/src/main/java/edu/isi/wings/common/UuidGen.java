/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.isi.wings.common;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UuidGen {
	/**
	 * current uuids are generated by: <prefix>-UUID.randomUUID()
	 */
	static int MAXIMUM_LENGTH_OF_UUID_PREFIX = 16;
	static Pattern pattern = Pattern.compile("^(.+?)\\-[a-f0-9]+\\-");

	/**
	 * returns a uuid with queryIndicator at the prefix e.g.
	 * generateAUuid("q4.2") ==> q4.2-a6d237a7-1ca6-4a63-86d0-2334a83448e8 max
	 * length of prefix is 27 characters if the prefix has length > 27, then the
	 * prefix is truncated to 27 characters
	 * 
	 * @param prefix
	 *            a prefix e.g. q4.2, q3.1 etc.
	 * @return a uuid prepended with prefix and a hyphen
	 */
	public static String generateAUuid(String prefix) {
		int prefixLength = prefix.length();
		if (prefixLength > MAXIMUM_LENGTH_OF_UUID_PREFIX) {
			prefix = prefix.subSequence(0, MAXIMUM_LENGTH_OF_UUID_PREFIX).toString();
		}
		Matcher m = pattern.matcher(prefix);
		if(m.find())
			prefix = m.group(1);
		
		StringBuilder result = new StringBuilder();
		UUID uuid = UUID.randomUUID();
		if (!prefix.equals("")) {
			result.append(prefix);
			result.append("-");
		}
		result.append(uuid.toString());
		return result.toString();
	}

	public static String generateURIUuid(URIEntity oldentity) {
		String name = generateAUuid(oldentity.getName());
		String ns = oldentity.getNamespace();
		ns = ns.replace("/"+oldentity.getName(), "/"+name);
		return ns + name;
	}

}