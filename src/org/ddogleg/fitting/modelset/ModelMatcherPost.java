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

package org.ddogleg.fitting.modelset;

import org.ddogleg.struct.Factory;

/**
 * Extension of {@link ModelMatcher} where the models are specified after construction.
 *
 * @author Peter Abeles
 */
public interface ModelMatcherPost<Model, Point> extends ModelMatcher<Model, Point> {
	/**
	 * Specifies the internal model. Factories are provided since each thread might need its own unique instance of
	 * the generator and distance function if they are not thread safe.
	 *
	 * @param factoryGenerator {@link ModelGenerator}
	 * @param factoryDistance {@link DistanceFromModel}
	 */
	void setModel( Factory<ModelGenerator<Model,Point>> factoryGenerator,
				   Factory<DistanceFromModel<Model,Point>> factoryDistance );
}
