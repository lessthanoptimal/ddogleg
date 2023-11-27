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

package org.ddogleg.example;

import org.ddogleg.fitting.modelset.ModelManager;

public class LineManager implements ModelManager<Line2D> {
	@Override
	public Line2D createModelInstance() {
		return new Line2D();
	}

	@Override
	public void copyModel( Line2D src, Line2D dst ) {
		dst.x = src.x;
		dst.y = src.y;
	}
}
