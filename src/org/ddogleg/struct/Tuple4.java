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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/**
 * Simple data structure for passing a quad of data.
 *
 * @author Peter Abeles
 */
public class Tuple4<A,B,C,D> {
	public @Nullable @Getter @Setter A d0;
	public @Nullable @Getter @Setter B d1;
	public @Nullable @Getter @Setter C d2;
	public @Nullable @Getter @Setter D d3;

	public Tuple4(@Nullable A d0, @Nullable B d1, @Nullable C d2, @Nullable D d3) {
		this.d0 = d0;
		this.d1 = d1;
		this.d2 = d2;
		this.d3 = d3;
	}

	public Tuple4() {
	}
}

