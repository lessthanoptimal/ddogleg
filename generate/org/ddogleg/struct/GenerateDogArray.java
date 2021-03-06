/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ddogleg.struct;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses the double[] implementation as a template to create all the others
 *
 * @author Peter Abeles
 */
public class GenerateDogArray {
	public static String[] templates = new String[]{
			"src/org/ddogleg/struct/DogArray_F64.java",
			"test/org/ddogleg/struct/TestDogArray_F64.java"};


	public static class WordSwaps {
		public String dataType;
		public String suffix;

		public WordSwaps( String dataType, String suffix ) {
			this.dataType = dataType;
			this.suffix = suffix;
		}
	}

	public static void main( String[] args ) {
		List<WordSwaps> swaps = new ArrayList<>();
		swaps.add( new WordSwaps("float","_F32"));
		swaps.add( new WordSwaps("byte","_I8"));
		swaps.add( new WordSwaps("int","_I32"));
		swaps.add( new WordSwaps("long","_I64"));
		swaps.add( new WordSwaps("boolean","_B"));

		for (String template : templates ) {
			File templateFile = new File(template);

			try {
				String templateString = FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
				for( WordSwaps swap : swaps) {
					String modified = templateString.replace("double",swap.dataType);
					modified = modified.replace("_F64",swap.suffix);

					String fileName = templateFile.getName().replace("_F64",swap.suffix);

					FileUtils.write(new File(templateFile.getParent(),fileName),modified,StandardCharsets.UTF_8);
				}
			} catch( IOException e ) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
