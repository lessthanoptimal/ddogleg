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
  \min\limits_{\boldsymbol{x} \in \Re^N} f(\boldsymbol{x})

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
    \min\limits_{\boldsymbol{x}} f(\boldsymbol{x})=\frac{1}{2}\sum^m_{j=1} r^2_j(\boldsymbol{x})

+-------------------------------+-------------+--------------+----------+-------+--------+--------+
| **Method**                    | Iteration   | Convergence  | Singular | Dense | Sparse | Schur  |
+-------------------------------+-------------+--------------+----------+-------+--------+--------+
| Trust Region LS Cauchy        | :math:`N^3` | Linear       | Yes      | Yes   | Yes    | Yes    |
+-------------------------------+-------------+--------------+----------+-------+--------+--------+
| Trust Region LS Dogleg        | :math:`N^3` | Super Linear | Yes [2]  | Yes   | Yes    | Yes    |
+-------------------------------+-------------+--------------+----------+-------+--------+--------+
| Levenberg-Marquardt           | :math:`N^3` | Super Linear | Yes      | Yes   | Yes    | Yes    |
+-------------------------------+-------------+--------------+----------+-------+--------+--------+

* **Schur**: Indicates if a variant is available that uses the Schur Complement

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
* [2] For dense problems you can handle singular systems using a robust solver like QRP or SVD. For sparse systems it currently can't handle singular systems but a fix for this is planned using LDL.


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

* Kaj Madsen, Hans Bruun Nielsen, Ole Tingleff, "Methods for Non-Linear Least Squares Problems" 2nd ed., 2004 Lecture Notes
* Jorge Nocedal and Stephen J. Wright, "Numerical Optimization" 2nd Ed. Springer
* Timothy A. Davis, "Direct Methods for Sparse Linear Systems"  2006 SIAM


Usage Examples
--------------

* :doc:`/optimization/NonLinearMinimization` (Quasi-Newton)
* :doc:`/optimization/NonLinearLeastSquares` (Levenberg-Marquardt)
* :doc:`/optimization/NonLinearSparseSchurComplement` (Sparse Schur Complement)

Convergence Tests
-----------------

Deciding when an optimization is done searching and should be stopped can be a tricky issue. If you make the criteria
too strict then it can take an excessive amount of time. Too loose and the solution can be poor. DDogleg attempts to
provide two parameters across all of its routines, F-Test and G-Test.

.. math::
  \mbox{F-test} &\qquad& ftol \cdot f(x)  \leq f(x) - f(x+p) \\
  \mbox{G-test} &\qquad& gtol \leq \left\lVert g(x) \right\Vert_\infty \\


F-Test checks to see when the cost function stops changing significantly and the G-Test when the gradient no longer
significant. Some libraries omit the F-Test when it is known that the optimal solution will have a cost of zero,
e.g. least squares. It is true that in this scenario the f-test will never be true. However, DDogleg
keeps the f-test no matter what since it is inexpensive to compute and when fitting real-world data there is almost
always noise and the minimum isn't zero.

When you look through the low level implementations there are sometimes other parameters available. If you don't mind
writing code for that specific algorithm only you can have direct access to those. In general though, they are not
necissary and reasonable defaults are selected.

Numerical Derivatives
---------------------

* Forward (Default)
* Forward-Backwards


Schur Complement
----------------

The Schur Complement is a "trick" which enables you to avoid decompose an entire matrix when solving a linear
system. Instead only much smaller internal submatrices are decomposed. In sparse systems this trick also reduces
fill in by carefully taking advantage of the matrice's structure.

https://en.wikipedia.org/wiki/Schur_complement

Schur Complement based optimization routines are implemented by extending the
`SchurJacobian <../javadoc/org/ddogleg/optimization/functions/SchurJacobian.html>`_ class. The SchurJacobian
will compute the left and right hand side of the Jacobian. Internally this when be converted into an approximate
Hessian.

.. math::
    H &= J'J = \left[\begin{array}[cc] A A & B\\ B& D\end{array}\right] \\
    J &= [L,R] \\
    A &= L^T L \\
    B &= L^T R \\
    D &= R^T R

where H is the approximate Hessian, J is the full Jacobian matrix, and L and R are the left and right outputs from your Jacobian calculation.
All the other implementation details are handled internally. See the JavaDoc for additional details.

Weighted Least-Squares
----------------------

Being able to directly specify a weight vector is planned for the future. For now you
can scale the residuals directly and accomplish the same thing.

Configuring
-----------

The easiest and strongly recommend way to create a new instance of any optimization routine is by using one of
the following factors:

* `FactoryOptimization <../javadoc/org/ddogleg/optimization/FactoryOptimization.html>`_
* `FactoryOptimizationSparse <../javadoc/org/ddogleg/optimization/FactoryOptimizationSparse.html>`_

Each function will create a different algorithm and takes in a configuration class. These configuration classes
enable you to change most important parameters. The JavaDoc describes what each parameter does.


* `ConfigQuasiNewton <../javadoc/org/ddogleg/optimization/quasinewton/ConfigQuasiNewton.html>`_
* `ConfigTrustRegion <../javadoc/org/ddogleg/optimization/trustregion/ConfigTrustRegion.html>`_
* `ConfigLevenbergMarquardt <../javadoc/org/ddogleg/optimization/lm/ConfigLevenbergMarquardt.html>`_

Customizing
-----------

Whether or not it's a good idea, there are time you want to customize the behavior of an optimization. For example,
you might want to normalize parameters every iteration or print out aditional debugging information. The code has
been intentionally written to enable you to do this.

This is an advance feature and will require browsing through the source code and being very familiar with how
these algorithms work. If after some effort you're not sure how to do this post a question on the user forum
and someone will try to help.

Hessian Scaling
---------------

TODO Summarize

Tip: Input Scaling
------------------

Seems like every discussion on non-linear optimization beats into you the absolute need for scaling your parameters
so that they are approximately the same order of magnitude. For example, one variable should be around 1e12 and another
1e-12. That's difficult for solvers to handle and can cause imprecation. It can even cause the parameter search
to get stuck as it over emphasizes variables!

TODO Flush this out more



