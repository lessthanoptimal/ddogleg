/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

/**
 * Can be used to create new instances of a model and copy the value of one model into another
 *
 * @author Peter Abeles
 */
public interface ModelManager<Model> {

	/**
	 * Creates a new instance of the model
	 *
	 * @return New model instance
	 */
	public Model createModelInstance();

	/**
	 * Turns 'dst' into an exact copy of 'src'.  If the model has a variable structure
	 * then it is assumed that the two models have the same structure.
	 *
	 * @param src Original model.  Not modified.
	 * @param dst Where the copy is written to.  Modified.
	 */
	public void copyModel( Model src, Model dst );
}
