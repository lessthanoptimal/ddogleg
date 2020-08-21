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

package org.ddogleg.optimization.quasinewton;

import org.ddogleg.optimization.LineSearch;
import org.ddogleg.optimization.OptimizationException;
import org.ddogleg.optimization.functions.CoupledDerivative;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;

/**
 * <p>
 * Line search algorithm that provides a guaranteed sufficient decrease according to the Wolfe condition.
 * This is the same algorithm by Minpack-2 [2].   The logic in the code below is based off of [2] but the original
 * paper [1] was being looked at too.
 * </p>
 *
 *
 * <p>
 * Wolfe condition<br>
 * &phi;(&alpha;) &le; &phi;(0) + ftol*&alpha;&phi;'(0)<br>
 * |&phi;'(&alpha;)| &le; gtol*|&phi;'(0)|<br>
 * where ftol and gtol determine the precision needed to terminate the search..
 * </p>
 *
 * <p>
 * This is basically a "cleaned up" version of the code from:.
 * MINPACK-2 Project. November 1993.<br>
 * Argonne National Laboratory and University of Minnesota.<br>
 * Brett M. Averick, Richard G. Carter, and Jorge J. More'.<br>
 * <br>
 * Permission has been given by Jorge Moré (after he emerged from underwater) to redistribute csrch on
 * July 8th 2012 via e-mail.
 * </p>
 *
 * <p>
 * [1] Jorge J. More and David J. Thuente, "Line Search Algorithms with Guaranteed Sufficient Decrease"
 * ACM Transactions of Mathematical Software, Vol 20 , No. 3, September 1994, Pages 286-307<br>
 * [2] MINPACK-2 source code http://ftp.mcs.anl.gov/pub/MINPACK-2/csrch/
 * </p>
 * @author Peter Abeles
 */
@SuppressWarnings("NullAway.Init")
public class LineSearchMore94 implements LineSearch {

	// Various magic numbers.  See [1]
	private static final double p5=0.5,p66=0.66,xtrapl=1.1,xtrapu=4.0;

	// computes the function value and derivative
	private CoupledDerivative function;

	// thresholds to test for convergence
	private double ftol=-1; // tolerance for sufficient decrease.  ftol > 0
	private double gtol;    // tolerance for curvature condition. gtol >= 0
	private double xtol;    // relative tolerance for an acceptable step. xtol >= 0
					        // Only a warning is provided if xtol has not been meet

	// value of the function and derivative at 0
	private double finit;
	private double ginit;
	// Equal to ftol*ginit.
	private double gtest;
	// size of the bracketed region
	private double width;
	private double width1;

	// flag indicating if the step size has been bracketed yet
	private boolean bracket;
	// which stage in the optimization process is it
	private int stage;
	
	// stx and sty define the closed interval that the solution must lie inside of.
	// - Before a bound/bracket is found stx < sty.
	// - After the bracket has been found stx and sty defined the allowed interval's end points.
	//   The relative magnitude of stx and sty is variable.

	// stx is alpha_l in the paper and is the best possible step found so far
	private double stx,fx,gx;
	// sty = alpha_u in the paper
	private double sty,fy,gy;
	// Adaptively found lower and upper bounds used to bracket the solution
	private double stmin,stmax;
	// Current trial value for the step.  Corresponds to alpha_t in the paper
	private double stp, fp, gp;
	// User specified lower and upper bound for the step stp
	private double stpmin,stpmax;

	// Verbose output if not null
	private @Nullable PrintStream verbose;

	// Indicates if this iit is the first iteration and indicates if what values need to be computed at stp
	private boolean firstIteration;
	// Indicates if it converged to a solution
	private boolean converged;

	// if true the estimated parameters have been updated
	boolean updated;

	/**
	 * Configures the line search.
	 *
	 * @param ftol Tolerance for sufficient decrease. ftol {@code >} 0. Smaller value for loose tolerance.  Try 1e-4
	 * @param gtol Tolerance for curvature condition. gtol &ge; 0. Larger value for loose tolerance.  Try 0.9
	 * @param xtol Relative tolerance for acceptable step. xtol &ge; 0. Larger value for loose tolerance.  Try 1e-4.
	 *
	 * @return Reference to this class to allow for command chaining.
	 */
	public LineSearchMore94 setConvergence(double ftol, double gtol, double xtol) {
		if( ftol < 0 )
			throw new IllegalArgumentException("ftol must be >= 0 ");
		if( gtol < 0 )
			throw new IllegalArgumentException("gtol must be >= 0 ");
		if( xtol < 0 )
			throw new IllegalArgumentException("xtol must be >= 0 ");

		this.ftol = ftol;
		this.gtol = gtol;
		this.xtol = xtol;
		return this;
	}

	@Override
	public void setFunction( CoupledDerivative function, double fmin) {
		this.function = function;
	}

	@Override
	public void setConvergence(double ftol, double gtol) {
		if( ftol < 0 )
			throw new IllegalArgumentException("ftol must be >= 0 ");
		if( gtol < 0 )
			throw new IllegalArgumentException("gtol must be >= 0 ");
		this.ftol = ftol;
		this.gtol = gtol;
		this.xtol = 0.1; // default value
	}

	@Override
	public void init(double funcAtZero, double derivAtZero, double funcAtInit, double stepInit ,
					 double stepMin, double stepMax )
	{
		if( ftol < 0 )
			throw new IllegalArgumentException("Must call setParameters first before init");
		if( stepMax < stepMin )
			throw new IllegalArgumentException("stpmin must be < stpmax");
		if( stepMin < 0 )
			throw new IllegalArgumentException("stpmin must be > 0");
		if( stepInit < stepMin)
			throw new IllegalArgumentException("Initial step is less than the minimum allowed step.");
		if( stepInit > stepMax)
			throw new IllegalArgumentException("Initial step is more than the maximum allowed step.");
		if( derivAtZero >= 0 )
			throw new IllegalArgumentException("Initial derivative is >= 0");

		this.stpmin = stepMin;
		this.stpmax = stepMax;

		this.bracket = false;
		this.stage = 0;
		this.finit = funcAtZero;
		this.ginit = derivAtZero;
		this.gtest = ftol*ginit;
		this.width = stpmax - stpmin;
		this.width1 = width/p5;
		
		this.stp = stepInit;
		this.fp = funcAtInit;
		
		stx = 0;
		fx = finit;
		gx = ginit;
		sty = 0;
		fy = finit;
		gy = ginit;
		stmin = 0;
		stmax = stp + xtrapu*stp;

		firstIteration = true;
		converged = false;
		updated = false;
	}

	@Override
	public boolean iterate() throws OptimizationException {

		// update the function and derivative values at stp
		function.setInput(stp);
		gp = function.computeDerivative();
		if( !firstIteration ) {
			fp = function.computeFunction();
		} else {
			firstIteration = false;
		}

		// Enter the second stage if a point has been found which meets the first wolfe condition
		// and is known to be past the optimal condition by having a positive derivative
		double ftest = finit + stp*gtest;
		if( stage == 0 && fp <= ftest && gp >= 0 )
			stage = 1;
		
		// check warning conditions
		boolean processBlocked = false;
		if (bracket && stmax - stmin <= xtol * stmax) {
			converged = true;
			if( verbose != null )
				verbose.println("XTOL test satisfied");
		}
		if (bracket && (stp <= stmin || stp >= stmax)) {
			processBlocked = true;
			if( verbose != null )
				verbose.println("Rounding error preventing progress.");
		}
		if (stp == stpmax && fp <= ftest && gp <= gtest) {
			processBlocked = true;
			if( verbose != null )
				verbose.println("stp == stpmax");
		}
		if (stp == stpmin && (fp > ftest || gp >= gtest)) {
			processBlocked = true;
			if( verbose != null )
				verbose.println("stp == stpmin");
		}

		// Check for convergence using the Wolfe conditions
		if( fp <= ftest && Math.abs(gp) <= -gtol*ginit) {
			converged = true;
			return true;
		}

		// See if progressBlocked or if it has converged
		if( processBlocked || converged )
			return true;

		// See if it is searching for the upper bound still
		if( stage == 0 && fp <= fx && fp > ftest ) {
			// Transform values using the function phi(x).  See pg 291
			// This function has been modified from what's in the paper by removing phi(0) since it
			// did not effect the outcome of the inequalities.
			// phi(x) = f(x) - ftol*f'(0)*x
			fp -= stp*gtest;  // fm
			fx -= stx*gtest;  // fxm
			fy -= sty*gtest;  // fym
			gp -= gtest;      // gm
			gx -= gtest;      // gxm
			gy -= gtest;      // gym

			// compute the new step
			dcstep();

			// transform these values back into the original function
			fx += stx*gtest;
			fy += sty*gtest;
			gx += gtest;
			gy += gtest;
		} else {
			// A upper bound has been found and the step is being refined
			dcstep();
		}
		
		// decide if a bisection step is needed
		if( bracket ) {
			if( Math.abs(sty-stx) >= p66*width1 )
				stp = stx + p5*(sty-stx);
			width1 = width;
			width = Math.abs(sty-stx);
		}
		
		// set the minimum and maximum steps allowed for stp
		if( bracket ) {
			// if a bracket has been found then stx and sty define the lower and upper bounds, but
			// their order is variable
			stmin = Math.min(stx,sty);
			stmax = Math.max(stx,sty);
		} else {
			stmin = stp + xtrapl*(stp-stx);
			stmax = stp + xtrapu*(stp-stx);
		}

		// force the stp to be within the user specified bound
		stp = Math.max(stp, stpmin);
		stp = Math.min(stp, stpmax);

		// see if further progress can be made. If not set stp to be equal to
		// the best point obtained so far
		if( (bracket && (stp <= stmin || stp >= stmax )) || (bracket && stmax-stmin <= xtol*stmax))
			stp=stx;

		updated = true;
		return false;
	}

	/**
	 * Computes the new step and updates fx,fy,gx,dy.
	 */
	private void dcstep() {
		double sgnd = gp*Math.signum(gx);

		// the new step
		double stpf;

		if( fp > fx ) {
			stpf = handleCase1();
		} else if( sgnd < 0 ) {
			stpf = handleCase2();
		} else if( Math.abs(gp) < Math.abs(gx) ) {
			stpf = handleCase3();
		} else {
			stpf = handleCase4();
		}

		// update the bracket which contains the solution
		if( fp > fx ) {
			// found an upper bound
			sty = stp;
			fy = fp;
			gy = gp;
		} else {
			// see if its on the other side of the dip
			if( sgnd < 0 ) {
				sty = stx;
				fy = fx;
				gy = gx;
			}
			stx = stp;
			fx = fp;
			gx = gp;
		}
		
		stp = stpf;
		updated = true;
	}

	/**
	 * stp has a higher value than stx.  The minimum must be between these two values.
	 * Pick a point which is close to stx since it has a lower value.
	 *
	 * @return The new step.
	 */
	private double handleCase1() {

		double stpc = SearchInterpolate.cubic2(fx, gx, stx, fp, gp, stp);
		double stpq = SearchInterpolate.quadratic(fx,gx,stx,fp,stp);

		// If the cubic step is closer to stx than the quadratic step take that
		bracket = true;
		if( Math.abs(stpc-stx) < Math.abs(stpq-stx)) {
			return stpc;
		} else {
			// otherwise go with the average of the twp
			return stpc + (stpq-stpc)/2.0;
		}
	}

	/**
	 * The sign of the derivative has swapped, indicating that the function is on the other
	 * side of the dip and that a bracket has been found.
	 *
	 * @return The new step.
	 */
	private double handleCase2() {
		double stpc = SearchInterpolate.cubic2(fp, gp, stp, fx, gx, stx);
		double stpq = SearchInterpolate.quadratic2(gp,stp,gx,stx);

		// use which ever is closest to stp since it is lower than stx
		bracket = true;
		if( Math.abs(stpc - stp) > Math.abs(stpq - stp)) {
			return stpc;
		} else {
			return stpq;
		}
	}

	/**
	 * The derivative at stp has a smaller magnitude than at stx and is likely to be closer to the minimum.  However
	 * there are special cases that need to be dealt with here.
	 *
	 * @return The new step.
	 */
	private double handleCase3() {
		double stpf;
		// use cubic interpolation only if it is safe
		double stpc = SearchInterpolate.cubicSafe(fp, gp, stp, fx, gx, stx, stmin, stmax);
		double stpq = SearchInterpolate.quadratic2(gp,stp,gx,stx);

		if( bracket ) {
			// Use which ever step is closer to stp
			if( Math.abs(stpc-stp) < Math.abs(stpq-stp)){
				stpf = stpc;
			} else {
				stpf = stpq;
			}
			if( stp > stx ) {
				stpf = Math.min(stp+p66*(sty-stp),stpf);
			} else {
				stpf = Math.max(stp+p66*(sty-stp),stpf);
			}
		} else {
			// because a bracket has not been found, take whichever step is farther from stp
			if( Math.abs(stpc-stp) > Math.abs(stpq-stp)) {
				stpf = stpc;
			} else {
				stpf = stpq;
			}
			stpf = Math.min(stmax,stpf); // this really is stmax/stmin and not stpmax/stpmin
			stpf = Math.max(stmin,stpf); // In the for tran can stmax and stmin is passed in for stpmax and stpmin
		}
		return stpf;
	}

	/**
	 * Lower function value. On the same side of the dip because the gradients have the same sign. The magnitude
	 * of the derivative did not decrease.
	 *
	 * @return The new step.
	 */
	private double handleCase4() {
		if( bracket ) {
			return SearchInterpolate.cubic2(fp, gp, stp, fy, gy, sty);
		} else if( stp > stx ) {
			return stmax;  // see comment in case 3 about stmax and stmin
		} else {
			return stmin;
		}
	}

	@Override
	public boolean isConverged() {
		return converged;
	}

	@Override
	public void setVerbose(@Nullable PrintStream out, int level) {
		this.verbose = out;
	}

	@Override
	public double getStep() {
		return stp;
	}

	@Override
	public double getFunction() {
		return fp;
	}

	@Override
	public double getGTol() {
		return gtol;
	}

	@Override
	public boolean isUpdated() {
		return updated;
	}
}
