/*
 * Copyright (c) 2012-2023, Peter Abeles. All Rights Reserved.
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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Simple data structure for passing a triple of data.
 *
 * @author Peter Abeles
 */
@Getter @Setter
public class Tuple3<A,B,C> {
	public @Nullable A d0;
	public @Nullable B d1;
	public @Nullable C d2;

	public Tuple3(@Nullable A d0, @Nullable B d1, @Nullable C d2) {
		this.d0 = d0;
		this.d1 = d1;
		this.d2 = d2;
	}

	public Tuple3() {}

	/** Returns d0 but performs a null check first */
	public A getCheckD0() {
		return Objects.requireNonNull(d0);
	}

	/** Returns d1 but performs a null check first */
	public B getCheckD1() {
		return Objects.requireNonNull(d1);
	}

	/** Returns d2 but performs a null check first */
	public C getCheckD2() {
		return Objects.requireNonNull(d2);
	}
}

