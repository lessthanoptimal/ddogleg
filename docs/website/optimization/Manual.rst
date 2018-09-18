Unconstrained Non-Linear Optimization
#####################################

Unconstrained non-linear optimization is a broad topic with a numerous to choose from for solving. DDogleg
specializes in solving small to large scale problems, where large scale is defined around 100,000 parameters in
a sparse system.

This manual will start with a summary of DDogleg's capabilities. If you have trouble understanding the summary then
the next section will be helpful. There a terse summary of the problem is defined and references papers and books
for you to read and learn more about this subject. DDogleg is designed so that a novice can easily use it, but to take
full advantage of all of its features, many of which are not available in other packages, then you need to understand
the math.

Unconstrained Minimization Methods
----------------------------------

  .. math::
    \min\limits_{\bm{x} \in \Re^N} f(\bm{x})

+-------------------------------+-------------+--------------+----------+-------------------+-------+--------+
| **Method**                    | Iteration   | Convergence  | Singular | Negative-Definite | Dense | Sparse |
+-------------------------------+-------------+--------------+----------+-------------------+-------+--------+
| Quasi-Newton BFGS             | :math:`N^2` | Super Linear | Yes      | Yes               | Yes   |        |
+-------------------------------+-------------+--------------+----------+-------------------+-------+--------+
| Trust Region BFGS Cauchy      | :math:`N^2` | Linear       | Yes      | Yes               | Yes   | Yes    |
+-------------------------------+-------------+--------------+----------+-------------------+-------+--------+
| Trust Region BFGS Dogleg      | :math:`N^2` | Super Linear | [1]      | [1]               | Yes   | Yes    |
+-------------------------------+-------------+--------------+----------+-------------------+-------+--------+

* **Iteration**: Runtime complexity of update step. N is number of parameters.
* **Convergence**: how fast it converged.
* **Singular**: indicates that it can process singular systems.
* **Negative-Definite**: indicate that it can process negative definite systems
* **Dense** and **Sparse**: indicate that dense and/or sparse matrices can be processed.

Unconstrained Least Squares Methods
-----------------------------------

  .. math::
      \min\limits_{\bm{x}} f(\bm{x})=\frac{1}{2}\sum^m_{j=1} r^2_j(\bm{x})

+-------------------------------+-------------+--------------+----------+-------+--------+--------+
| **Method**                    | Iteration   | Convergence  | Singular | Dense | Sparse | Schur  |
+-------------------------------+-------------+--------------+----------+-------+--------+--------+
| Trust Region LS Cauchy        | :math:`N^3` | Linear       | Yes      | Yes   | Yes    | Yes    |
+-------------------------------+-------------+--------------+----------+-------+--------+--------+
| Trust Region LS Dogleg        | :math:`N^3` | Super Linear | Yes [2]  | Yes   | Yes    | Yes    |
+-------------------------------+-------------+--------------+----------+-------+--------+--------+
| Levenberg-Marquardt           | :math:`N^3` | Super Linear | Yes      | Yes   | Yes    | Yes    |
+-------------------------------+-------------+--------------+----------+-------+--------+--------+

* **Schur**: If a variant is available that uses the Schur Complement

Sparse Structures
-----------------

+-----------------------------+--------------------------------------------------------------------------+
| **Name**                    |             **Description**                                              |
+-----------------------------+--------------------------------------------------------------------------+
| General                     | General purpose sparse solver. Can be prone to fill in.                  |
+-----------------------------+--------------------------------------------------------------------------+
| Schur Complement            | Huge speed up for matrices with diagonal A and D sub-matrices. [A B;C D] |
+-----------------------------+--------------------------------------------------------------------------+

* [1] Switches to Cauchy in this situation and convergence slows down.
* [2] For dense systems you can handle singular systems using a robust solver like QRP or SVD. For sparse systems it currently can't handle singular systems but a fix for this problem is planned using LDL.


Introduction
------------

This manual covers the API and is primarily example based. An (incomplete) technical report is being worked on to cover
the numerical and algorithmic implementation details. For those who are new to the subject of optimization
and for see this being an important part of their career we suggest picking up a copy of "Numerical Optimization".
It covers many of the methods included in this library. The source code is also intended to be browsed and contains
references to other source material.

If you wish to use the sparse solvers you need to be familiar with the strength and weaknesses of sparse linear
algebra. An excellent source to learn more about this subject and see how complex of a problem it is can be found
in "Direct Methods for Sparse Linear Systems".

Apologies for this manual being incomplete. Please drop of a note if you find this library useful. Knowing that
someone is using it really helps the motivation!

Recommended Reading

* Jorge Nocedal and Stephen J. Wright, "Numerical Optimization" 2nd Ed. Springer
* Timothy A. Davis, "Direct Methods for Sparse Linear Systems"  2006 SIAM


Usage Examples
--------------

* :doc:`/optimization/NonLinearMinimization` (Quasi-Newton)
* :doc:`/optimization/NonLinearLeastSquares` (Levenberg-Marquardt)
* Writing analytical Gradient
* Writing analytical Jacobian


Convergence Tests
-----------------

TODO Summarize

Numerical Derivatives
---------------------

* Forward (Default)
* Forward-Backwards


Schur Complement
----------------

TODO Link to page here

Weighted Least-Squares
----------------------

Being able to directly specify a weight vector is planned for the future. For now you
can scale the residuals directly and accomplish the same thing.

Quasi-Newton BFGS
-----------------

- Configuration
- Implementation Notes

Trust-Region Cauchy
-------------------

- Configuration
- Implementation Notes

Trust-Region Dogleg
-------------------

- Configuration
- Implementation Notes


Levenberg-Marquardt
-------------------

- Configuration
- Implementation Notes

Hessian Scaling
---------------

TODO Summarize

Tip: Input Scaling
------------------

TODO link to external website on this subject



