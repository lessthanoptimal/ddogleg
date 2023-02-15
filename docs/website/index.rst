DDogleg Numerics
=====================

DDogleg Numerics is a high performance Java library for non-linear optimization, clustering, robust model fitting, polynomial root finding, sorting, and more. The API is designed to be user to use, without excessive abstraction often found in other libraries. The user is provided with the capability to have tight control over memory and CPU usage. Source code is publicly available and has been released under and Apache 2.0 license.

===================   =====================
**Latest Version**    v\ |ddogleg_version|
**Released**          February 15, 2023
**Source Code**       `GitHub <https://github.com/lessthanoptimal/ddogleg>`_
**License**           `Apache 2.0 <http://www.apache.org/licenses/LICENSE-2.0>`_
===================   =====================

**Maven Central**

.. parsed-literal::
    <dependency>
        <groupId>org.ddogleg</groupId>
        <artifactId>ddogleg</artifactId>
        <version>\ |ddogleg_version|\ </version>
    </dependency>

.. toctree::
   :caption: Table of Contents
   :maxdepth: 2

   Documentation
   Download
   Support

Functionality
---------------

* **Unconstrained minimization:** Quasi-Newton BFGS, linear search More94 (MINPACK-2), line search Fletcher 86
* **Unconstrained least-squares:** Dense and Sparse, Levenberg-Marquardt, Trust-Region (Dogleg)
* **Polynomial root finding:** Sturm sequence, Eigenvalue Decomposition, and iterative root refinement.
* **Robust Model Fitting:** RANSAC, LMedS, and some strange one.
* **Nearest Neighbor:** Exhaustive, K-D Trees, Best-Bin-First K-D Trees, Random Forest, VpTree
* **Sorting:** Shell Sort, Quick Sort, and Quick Select.
* **Combinatorics:** Set combinations and permutations.
* **Clustering:** K-Means and Gaussian Mixture Models (GMM)
* **Arithmetic:** Complex and Polynomial.
* **Data Structures:** Dynamic arrays for small and large data.

Single and multi-threaded implementations of several algorithms, such as model fitting variants.

Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
