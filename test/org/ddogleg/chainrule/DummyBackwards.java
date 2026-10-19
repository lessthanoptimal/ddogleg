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

package org.ddogleg.chainrule;

import java.util.List;
import java.util.Map;

/**
 * Implements all the functions but does nothing
 */
public class DummyBackwards<T> implements CROperatorBackwardsGrad<T>, CROperatorBackwardsJac<T> {
	@Override public void gradient( CRArray<T> dout, List<CRArray<T>> gradientInputs ) {}

	@Override public void jacobian( CRMatrix<T> dout, List<CRMatrix<T>> jacobianInputs ) {}

	@Override public void setInput( int which, CRArray<T> input ) {}

	@Override public int getInputSize( int which ) {return 0;}

	@Override public int getInputCount() {return 0;}

	@Override public Map<String, String> getSettings() {return Map.of();}

	@Override public void forward( CRArray<T> output ) {}

	@Override public int getOutputSize() {return 0;}

	@Override public Class<T> getArrayType() {return null;}
}
