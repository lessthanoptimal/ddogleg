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

/// This is a {@link CRLossHuber Huber Loss} function adapted to return the error and is triggered based on the
/// F-Norm of the error vector instead of individual elements. it computes what the Hubert loss would be then divides
/// it by the L2 loss that is computed. Then scales it so that the L2 loss will equal the Hubert loss.
public class CRErrorHuberNorm implements CROperatorBackwardsJac<double[]> {
	@Getter protected double threshold;

	// Threshold squared for convenience
	private double thresholdSq;

	protected double[] residuals;

	protected final MathCRMatrixD math = new MathCRMatrixD();
	protected final CRMatrix<double[]> workJac = CRMatrix.createD(5, 5);

	public CRErrorHuberNorm( int dof, double threshold ) {
		this.residuals = new double[dof];
		setThreshold(threshold);
	}

	public CRErrorHuberNorm() {this(1, 1);}

	@Override public void forward( CRArray<double[]> output ) {
		double norm2 = 0.0;
		for (int i = 0; i < residuals.length; i++) {
			double r = residuals[i];
			norm2 += r*r;
		}

		double scale;
		if (norm2 > thresholdSq) {
			double norm = Math.sqrt(norm2);

//            // The error2 under Huber Loss
//            double huberCost = threshold*(norm - 0.5*threshold);
//            // Scale it by what the L2 loss would have been
//            scale = huberCost/norm2;
//            // square root since this is residual error and not L2 loss
//            scale = Math.sqrt(scale);

			// Simplified version
			scale = Math.sqrt(threshold*(norm - 0.5*threshold))/norm;
		} else {
			scale = 1.0;
		}

		for (int i = 0; i < residuals.length; i++) {
			output.d(i, scale*residuals[i]);
		}
	}

	@Override public void jacobian( CRMatrix<double[]> dout, List<CRMatrix<double[]>> jacobianInputs ) {
		if (jacobianInputs.get(0).isInvalid())
			return;

		workJac.reshape(getOutputSize(), getInputSize(0));
		math.zero(workJac);

		double norm2 = 0.0;
		for (int i = 0; i < residuals.length; i++) {
			double r = residuals[i];
			norm2 += r*r;
		}

		if (norm2 <= thresholdSq) {
			math.diag(1.0, workJac);
		} else {
			double norm = Math.sqrt(norm2);
			double inner = threshold*norm - 0.5*threshold*threshold;
			double left = Math.sqrt(inner);
			double scale = left/norm;

			for (int i = 0; i < residuals.length; i++) {
				for (int j = 0; j < residuals.length; j++) {
					double x = residuals[j];
					double dNormXj = x/norm;
					double dInvNormXj = -x/(norm2*norm);
					double dLeftXj = 0.5*threshold*dNormXj/left;

					if (i == j) {
						workJac.d(i, j, (dLeftXj/norm + left*dInvNormXj)*residuals[i] + scale);
					} else {
						workJac.d(i, j, (dLeftXj/norm + left*dInvNormXj)*residuals[i]);
					}
				}
			}
		}

//        System.out.println(workJac.toStringD("%.3f"));
		math.mult(dout, workJac, jacobianInputs.get(0));
	}

	public void setThreshold( double threshold ) {
		this.threshold = threshold;
		this.thresholdSq = threshold*threshold;
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

		setThreshold(Double.parseDouble(settings.getOrDefault("threshold", "1.0")));
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
