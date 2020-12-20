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

package org.ddogleg;

import pabeles.concurrency.ConcurrencyOps;

/**
 * Used to turn on and off functions/classes which can automatically switch between concurrent operations
 *
 * @author Peter Abeles
 */
public class DDoglegConcurrency extends ConcurrencyOps {
	/** Used to toggle auto matic switching to concurrent algorithms */
	public static boolean USE_CONCURRENT = false;

	/**
	 * Sets the maximum number of threads available in the thread pool and adjusts USE_CONCURRENT. If
	 * the number of threads is less than 2 then USE_CONCURRENT will be set to false and the single thread
	 * version of code will be called. Otherwise USE_CONCURRENT will be true and the max threads in the pool
	 * set to the specified number.
	 *
	 * @param maxThreads Maximum number of threads. &le; 1 means it will not be threaded.
	 */
	public static void setMaxThreads( int maxThreads ) {
		ConcurrencyOps.setMaxThreads(maxThreads);
		USE_CONCURRENT = maxThreads > 1;
	}

	public static boolean isUseConcurrent() {
		return USE_CONCURRENT;
	}
}
