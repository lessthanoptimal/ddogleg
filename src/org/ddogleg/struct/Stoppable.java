/*
 * Copyright (c) 2012-2018, Peter Abeles. All Rights Reserved.
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

/**
 * Provides a mechanism to stop the processing before completion. When an operation is stopped
 * it will either exit normally or throw the runtime exception {@link Stopped}. In either case
 * the function {@link #isStopRequested()} will return true. A class which has been stopped early
 * can <b>often</b> be called be called again, but the stop request flag is typically reset
 * internally and a few milliseconds should be provided for the process to launch and be reset.
 *
 * The only way to tell if a process has stopped is to observe that the next operation after it
 * has been called.
 *
 * @author Peter Abeles
 */
public interface Stoppable {
	/**
	 * Invoke to request that the process stop running. The process will not immediately stop
	 * and an undefined amount of time will pass before it has stopped.
	 */
	void requestStop();

	/**
	 * True if a request to stop has been sent. If a stopped process is invoked again this
	 * flag might not be immediately reset. This bit of amiguity is done to greatly simplify
	 * implementation of this interface.
	 * @return true if a stop request has been sent
	 */
	boolean isStopRequested();

	/**
	 * Exception which is thrown when a process has been stopped early.
	 */
	class Stopped extends RuntimeException {
		public Stopped() {
		}

		public Stopped(String message) {
			super(message);
		}
	}
}
