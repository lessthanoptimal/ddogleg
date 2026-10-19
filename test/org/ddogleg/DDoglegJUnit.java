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

package org.ddogleg;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for all unit tests. Enforces standards, such as no printing to stdout or stderr and setTo() copying
 * every field and returning this.
 */
public class DDoglegJUnit {
	// Always provide a random number generator since it's needed so often
	protected final Random rand = new Random(234);

	// Override output streams to keep log spam to a minimum
	protected final MirrorStream out = new MirrorStream(System.out);
	protected final MirrorStream err = new MirrorStream(System.err);
	protected final PrintStream systemOut = System.out;
	protected final PrintStream systemErr = System.err;

	@BeforeEach
	public void captureStreams() {
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	@AfterEach
	public void revertStreams() {
		System.setOut(systemOut);
		System.setErr(systemErr);
		assertFalse(out.used, "stdout was written to which is forbidden by default");
		assertFalse(err.used, "stderr was written to which is forbidden by default");
	}

	@Test public void setTo() {
		// See if the test is applicable
		Class<?> targetClass = lookupTestClass();
		if (targetClass == null || Modifier.isAbstract(targetClass.getModifiers()))
			return;
		try {
			targetClass.getMethod("setTo", targetClass);
		} catch (NoSuchMethodException e) {
			return;
		}

		// Test the function
		checkSetTo(targetClass);
	}

	/**
	 * The class being tested, found by removing the leading "Test" from this class's name, or null if there is none
	 */
	protected @Nullable Class<?> lookupTestClass() {
		String name = getClass().getSimpleName();
		if (!name.startsWith("Test"))
			return null;
		name = name.substring(4);

		// A member class might mirror the structure of the class being tested
		Class<?> enclosing = getClass().getEnclosingClass();
		if (getClass().isMemberClass() && enclosing != null && enclosing.getSimpleName().startsWith("Test")) {
			Class<?> found = forName(getClass().getPackageName() + "." +
					enclosing.getSimpleName().substring(4) + "$" + name);
			if (found != null)
				return found;
		}

		return forName(getClass().getPackageName() + "." + name);
	}

	private static @Nullable Class<?> forName( String name ) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	/** Checks that setTo() copies all fields correctly and returns 'this' */
	protected void checkSetTo( Class<?> type ) {
		try {
			// Find setTo(SameType) method
			Method setTo = null;
			for (Method m : type.getMethods()) {
				if (!m.getName().equals("setTo")) continue;
				Class<?>[] params = m.getParameterTypes();
				if (params.length == 1 && params[0].isAssignableFrom(type)) {
					setTo = m;
					break;
				}
			}
			assertNotNull(setTo, "No setTo(" + type.getSimpleName() + ") method found");

			assertNotSame(void.class, setTo.getReturnType(), "setTo() can't return void");
			assertTrue(setTo.getReturnType().isAssignableFrom(type),
					"Return type must be assignable. Found " + setTo.getReturnType().getSimpleName());

			// Create src with non-default values, and a fresh dst
			Object src = createNotDefault(type);
			Object dst = type.getConstructor().newInstance();

			// Verify src actually differs from dst (i.e. createNotDefault worked)
			for (Field f : type.getFields()) {
				if (skipField(f)) continue;
				assertFalse(Objects.deepEquals(f.get(src), f.get(dst)),
						"createNotDefault did not change field: " + f.getName());
			}

			// Invoke setTo and check it returns 'this'
			Object ret = setTo.invoke(dst, src);
			assertNotNull(ret, "setTo()");
			assertSame(dst, ret, "setTo() must return 'this' for chaining");

			// All fields must now match
			for (Field f : type.getFields()) {
				if (skipField(f)) continue;
				assertTrue(Objects.deepEquals(f.get(src), f.get(dst)),
						"Field not copied by setTo(): " + f.getName());
			}
		} catch (Exception e) {
			fail("checkSetTo failed: " + e.getMessage());
		}
	}

	/**
	 * Static fields aren't part of the state. Arrays are skipped since setTo() might only copy part of them, e.g.
	 * the elements in use and not the unused capacity
	 */
	protected boolean skipField( Field f ) {
		return Modifier.isStatic(f.getModifiers()) || f.getType().isArray();
	}

	/** Creates an instance where all non-static fields differ from their default values */
	protected Object createNotDefault( Class<?> type ) throws Exception {
		Object ret = type.getConstructor().newInstance();
		for (Field f : type.getFields()) {
			if (skipField(f)) continue;

			Object fv = f.get(ret);

			if (f.getType().isPrimitive() || f.getType() == String.class) {
				fv = notDefault(f.getType(), fv);
			} else if (fv != null && f.getType().isEnum()) {
				Object[] values = f.getType().getEnumConstants();
				fv = values[(((Enum<?>)fv).ordinal() + 1)%values.length];
			} else if (fv != null) {
				// if it has a setTo(), then create a random instance and call setTo
				try {
					Method m = findCompatibleSetTo(f.getType());
					// f.getType() could be an abstract class. To avoid that ambiguity we create
					// an object based on the specific instance's type
					m.invoke(fv, createNotDefault(fv.getClass()));
				} catch (NoSuchMethodException | SecurityException | InvocationTargetException |
						 IllegalArgumentException | IllegalAccessException ignore) {
				}
			}
			if (!Modifier.isFinal(f.getModifiers()))
				f.set(ret, fv);
		}
		return ret;
	}

	/** Returns a value of the primitive or String type which is different from 'before' */
	private Object notDefault( Class<?> type, Object before ) {
		if (boolean.class == type) {
			return !((boolean)before);
		} else if (byte.class == type) {
			return (byte)(((byte)before) + 1);
		} else if (char.class == type) {
			return (char)(((char)before) + 1);
		} else if (short.class == type) {
			return (short)(((short)before) + 1);
		} else if (int.class == type) {
			return ((int)before) + 1;
		} else if (long.class == type) {
			return ((long)before) + 1;
		} else if (float.class == type) {
			float value = rand.nextFloat();
			while (value == (float)before)
				value = rand.nextFloat();
			return value;
		} else if (double.class == type) {
			double value = rand.nextDouble();
			while (value == (double)before)
				value = rand.nextDouble();
			return value;
		} else if (String.class == type) {
			return before + "_modified";
		}
		throw new IllegalArgumentException("Unsupported type " + type.getSimpleName());
	}

	private static Method findCompatibleSetTo( Class<?> type ) throws NoSuchMethodException {
		Method[] methods = type.getMethods();
		for (Method m : methods) {
			if (!m.getName().equals("setTo"))
				continue;
			Class<?>[] params = m.getParameterTypes();
			if (params.length != 1)
				continue;

			if (params[0].isAssignableFrom(type))
				return m;
		}
		throw new NoSuchMethodException();
	}

	/** Passes everything through to the wrapped stream while recording if it was used */
	public static class MirrorStream extends OutputStream {
		public PrintStream out;
		public boolean used = false;

		public MirrorStream( PrintStream out ) {
			this.out = out;
		}

		@Override public void write( int b ) throws IOException {
			used = true;
			out.write(b);
		}

		@Override public void write( byte[] b, int off, int len ) throws IOException {
			used = true;
			out.write(b, off, len);
		}

		@Override public void flush() throws IOException {
			out.flush();
		}

		@Override public void close() throws IOException {
			out.close();
		}
	}
}
