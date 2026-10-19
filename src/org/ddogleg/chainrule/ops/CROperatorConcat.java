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

package org.ddogleg.chainrule.ops;

import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.MathCRMatrixD;

import java.util.List;
import java.util.Map;

/// Concatenates its inputs into one output, in the order they were declared. Values pass through
/// untouched.
///
/// A graph has a single output, so this is how one observation contributes more than one group of
/// residuals: each group is its own chain and they are joined here.
public class CROperatorConcat implements CROperatorBackwardsJac<double[]> {
	/// Length of each input
	private int[] sizes = new int[0];

	/// Where each input starts inside the output
	private int[] offsets = new int[0];

	/// The concatenated values, filled in by [#setInput]
	private double[] values = new double[0];

	private int outputSize;

	private final CRMatrix<double[]> workJac = CRMatrix.createD(1, 1);
	private final MathCRMatrixD math = new MathCRMatrixD();

	public CROperatorConcat( int... sizes ) {
		configure(sizes);
	}

	public CROperatorConcat configure( int... sizes ) {
		if (!(sizes.length >= 1))
			throw new IllegalArgumentException("there must be at least one input");
		for (int size : sizes) {
			if (!(size >= 1))
			throw new IllegalArgumentException("every input must have at least one element");
		}

		this.sizes = sizes.clone();
		this.offsets = new int[sizes.length];

		int total = 0;
		for (int i = 0; i < sizes.length; i++) {
			offsets[i] = total;
			total += sizes[i];
		}

		this.outputSize = total;
		this.values = new double[total];
		return this;
	}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < outputSize; i++) {
			output.d(i, values[i]);
		}
	}

	/// Each input passes straight into its own run of the output, so its derivative is a selection
	/// matrix and the product is just that run's columns of `dout`
	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		for (int which = 0; which < sizes.length; which++) {
			CRMatrix<double[]> output = jacobianInputs.get(which);
			if (output.isInvalid())
				continue;

			workJac.reshape(outputSize, sizes[which]);
			math.zero(workJac);
			for (int i = 0; i < sizes[which]; i++) {
				workJac.d(offsets[which] + i, i, 1.0);
			}
			math.mult(dout, workJac, output);
		}
	}

	/// Where the specified input's values start inside the output
	public int outputOffset( int which ) {
		checkInput(which);
		return offsets[which];
	}

	private void checkInput( int which ) {
		if (which < 0 || which >= sizes.length)
			throw new IndexOutOfBoundsException("Unexpected input " + which);
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		checkInput(which);
		input.write(values, offsets[which], sizes[which]);
	}

	@Override public int getInputSize( int which ) {
		checkInput(which);
		return sizes[which];
	}

	@Override public Map<String, String> getSettings() {
		var text = new StringBuilder();
		for (int i = 0; i < sizes.length; i++) {
			if (i > 0)
				text.append(',');
			text.append(sizes[i]);
		}
		return Map.of("sizes", text.toString());
	}

	@Override public void setSettings( Map<String, String> settings ) {
		settings.forEach(( key, value ) -> {
			if (!key.equals("sizes"))
				throw new IllegalArgumentException("Unknown key: " + key);

			String[] words = value.split(",");
			var found = new int[words.length];
			for (int i = 0; i < words.length; i++) {
				found[i] = Integer.parseInt(words[i].trim());
			}
			configure(found);
		});
	}

	@Override public int getInputCount() {return sizes.length;}

	@Override public int getOutputSize() {return outputSize;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
