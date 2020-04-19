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

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.Set;

/**
 * Generic interface for implementing verbose output to a {@link PrintStream}.
 *
 * @author Peter Abeles
 */
public interface VerbosePrint {
	/**
	 * If set to a non-null output then extra information will be printed to the specified stream.
	 *
	 * @param out Stream that is printed to. Set to null to disable
	 * @param configuration (Future use) Set which specifies flags that can be used to turn on and off different output
	 */
	void setVerbose(@Nullable PrintStream out , @Nullable Set<String> configuration );
}
