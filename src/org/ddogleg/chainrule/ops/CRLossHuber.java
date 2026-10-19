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

import lombok.Getter;
import org.ddogleg.chainrule.CRArray;
import org.ddogleg.chainrule.CRMatrix;
import org.ddogleg.chainrule.CROperatorBackwardsJac;
import org.ddogleg.chainrule.MathCRMatrixD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Huber Loss is a robust loss function that is less sensitive to outliers than the squared error loss. For values
/// less than a threshold it returns the squared error, for values greater than it returns an error that grows linearly
/// instead of quadratic.
public class CRLossHuber implements CROperatorBackwardsJac<double[]> {
	@Getter protected double threshold;

	// Threshold squared for convenience
	private double thresholdSq;

	protected double[] residuals;

	protected final MathCRMatrixD math = new MathCRMatrixD();
	protected final CRMatrix<double[]> workJac = CRMatrix.createD(5, 5);

	public CRLossHuber( int dof, double threshold ) {
		this.residuals = new double[dof];
		this.threshold = threshold;
		this.thresholdSq = threshold*threshold;
	}

	public CRLossHuber() {this(1, 1);}

	@Override public void forward( CRArray<double[]> output ) {
		for (int i = 0; i < residuals.length; i++) {
			double r = residuals[i];
			double rr = r*r;
			if (rr <= thresholdSq) {
				output.d(i, 0.5*rr);
			} else {
				output.d(i, threshold*(Math.abs(r) - 0.5*threshold));
			}
		}
	}

	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		if (jacobianInputs.get(0).isInvalid())
			return;

		workJac.reshape(getOutputSize(), getInputSize(0));
		math.zero(workJac);

		for (int i = 0; i < residuals.length; i++) {
			double r = residuals[i];
			double rr = r*r;
			if (rr <= thresholdSq) {
				workJac.d(i, i, r);
			} else {
				workJac.d(i, i, threshold*(r > 0 ? 1 : -1));
			}
		}

		math.mult(dout, workJac, jacobianInputs.get(0));
	}

	@Override public Map<String, String> getSettings() {
		var map = new HashMap<String, String>();
		if (residuals.length != 1)
			map.put("dof", String.valueOf(residuals.length));
		if (threshold != 1.0)
			map.put("threshold", String.valueOf(threshold));
		return map;
	}

	@Override public void setSettings( Map<String, String> settings ) {
		int dof = Integer.parseInt(settings.getOrDefault("dof", "1"));
		if (dof != this.residuals.length)
			this.residuals = new double[dof];

		threshold = Double.parseDouble(settings.getOrDefault("threshold", "1.0"));
		thresholdSq = threshold*threshold;
	}

	@Override public void setInput( int which, CRArray<double[]> input ) {
		if (which != 0)
			throw new IllegalArgumentException("Unexpected input");
		input.write(residuals, 0, residuals.length);
	}

	@Override public int getInputSize( int which ) {
		if (which != 0)
			throw new IllegalArgumentException("Unexpected input");
		return residuals.length;
	}

	@Override public int getInputCount() {return 1;}

	@Override public int getOutputSize() {return residuals.length;}

	@Override public Class<double[]> getArrayType() {return double[].class;}
}
