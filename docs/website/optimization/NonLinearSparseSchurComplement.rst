Non-Linear Least Squares: Sparse Schur Complement
#################################################

As optimization problems get larger they also tend to be more sparse. A common problem with
sparse systems is fill in. What fill in refers to are zero elements becoming non-zero as a
linear system is solved. This will destroy your performance. What the Schur Complement does
is allow you to solve the problem in separate components which are structured for efficiency.

Bundle Adjustment is a classic problem from computer vision which is made possible by the Schur
Complement. What would take seconds when solved with the Schur Complement can literally take
hours or days without it. This example demonstrates the Schur Comlpement in a 2D version
of bundle adjustment.

The parameters which are optimized are camera and landmarks locations. Both are them are
described by an (x,y) coordinate. Each camera can observe each landmark with a bearings measurement.

.. math::
  \theta_{ij} = \mbox{atan}\left( \frac{l^j_y-c^i_y}{l^j_x-c^i_x}\right)

where :math:`\theta_{ij}` is camera i's observation of landmark j,
:math:`(c_x,c_y)` is a camera's location, and :math:`(l_x,l_y)` is a landmark's location.

The actual code is shown below. This is more complex than our previous examples so be sure to
read the in code comments. How the Jacobian is formulated is also different from the regular
least-squaers problem. It's broken up into a left and right side.

:gitexample:`ExampleSchurComplementLeastSquares.java`

.. literalinclude:: ../../../examples/src/org/ddogleg/example/ExampleSchurComplementLeastSquares.java
  :language: java
  :linenos:
  :start-after: public class ExampleSchurComplementLeastSquares
  :tab-width: 4
  :dedent: 4

When you run this example you should see something like the following:

.. literalinclude:: ../example_output/schurcomplement.txt