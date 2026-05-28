/*
 * Copyright (c) 2026, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.util;

import org.ddogleg.struct.VerbosePrint;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/// Various utility functions for use with {@link VerbosePrint}
public class VerboseUtils {
	/// Pass along these arguments to all fields that support verbose printing too
	public final static String RECURSIVE = "recursive";

	/// Print verbose information on runtime profiling
	public final static String RUNTIME = "runtime";

	/// Number of characters in the class name prefix in verbose printing
	public static int VERBOSE_PREFIX_LENGTH = 6;

	/// Prints a table explaining what the prefixes are
	public static boolean VERBOSE_PRINT_TABLE = true;

	public static @Nullable PrintStream addPrefix( VerbosePrint owner, @Nullable PrintStream out ) {
		return addPrefix(owner, 1, out);
	}

	public static @Nullable PrintStream addPrefix( VerbosePrint owner, int numIndents, @Nullable PrintStream out ) {
		if (out == null || out instanceof PrintStreamInjectIndent)
			return out;

		// Temporary work around for duplicated code in BoofCV. Once the migration is done delete the line below
		if (out.getClass().getSimpleName().equals("PrintStreamInjectIndent"))
			return out;

		String simpleName = owner.getClass().getSimpleName();
		String pre = nameToShort(simpleName, VERBOSE_PREFIX_LENGTH);
		if (VERBOSE_PRINT_TABLE)
			out.println("Verbose: " + pre + " " + simpleName);
		return new PrintStreamInjectIndent(pre, numIndents, out);
	}

	/// Assumes names are camel case and that the capital letters are important. Same for numbers
	public static String nameToShort( String name, int length ) {
		String text = "";
		for (int i = 0; i < name.length() && text.length() < length; i++) {
			char c = name.charAt(i);
			if (Character.isUpperCase(c) || Character.isDigit(c)) {
				text += c;
			}
		}

		if (text.length() < length) {
			// Fill in unused characters with a dash. Makes it visually easier to see spacing between indents.
			for (int i = text.length(); i < length; i++) {
				text += "-";
			}
		}

		return text;
	}

	/// Function which handles boilerplate for support recursive verbose print
	public static void verboseChildren( @Nullable PrintStream out, @Nullable Set<String> configuration,
										@Nullable VerbosePrint... children ) {
		// See how many tabs have already been added
		int numIndents = 0;
		PrintStream originalOut = out;
		if (out instanceof PrintStreamInjectIndent o ) {
			numIndents = o.getIndentCount();
			// This will keep the tabs, but remove previous modifications
			originalOut = ((PrintStreamInjectIndent)out).getOriginalStream();
		}

		// If not cursive then do nothing
		if (children == null || configuration == null || !configuration.contains(RECURSIVE)) {
			return;
		}

		// If the output is null then its turning off print
		if (out == null) {
			for (int i = 0; i < children.length; i++) {
				if (children[i] == null)
					continue;
				children[i].setVerbose(null, configuration);
			}
			return;
		}

		// Add tabs to children when in verbose mode
		numIndents += 1;
		for (int i = 0; i < children.length; i++) {
			if (children[i] == null)
				continue;
			PrintStream tabbed = addPrefix(children[i], numIndents, originalOut);
			children[i].setVerbose(tabbed, configuration);
		}
	}

	public static <T> Set<T> hashSet( T... values ) {
		HashSet<T> ret = new HashSet<>();
		for (int i = 0; i < values.length; i++) {
			ret.add(values[i]);
		}
		return ret;
	}
}
