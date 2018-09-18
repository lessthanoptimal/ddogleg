Non-Linear Least Squares
########################


The following code demonstrates non-linear optimization by finding the best fit line to a set of points. See
the `optimization manual <Manual.html>`__ for an overview of all optimization techniques available in DDogleg.

Here we will tackle a problem using dense unconstrained least-squares minimization with a numerical Jacobian.
Unconstrained least-squares minimization solves problems which can be described by a function of the form:

  .. math::

    \min\limits_{\bm{x} \in \Re^N} f(\bm{x})=\frac{1}{2}\sum^m_{i=1} r^2_i(\bm{x})

where :math:`r_i(\bm{x}) = f_i(\bm{x}) - y_i` is a scalar function which outputs the residual for function :math:`i`, predicted value subtracted the observed value.
By definition :math:`f(\bm{x}) \ge 0`. In DDogleg you don't define the :math:`f(\bm{x})` function directly but instead define the set of :math:`r_j(\bm{x})` functions
by implementing FunctionNtoM. Implementations of FunctionNtoM take in an array with N elements and output an array with M elements.

In this example we will consider a very simple unconstrained least-squares problem, fitting a line to a set of points.
To solve non-linear problems you will need to select which method to use (Levenberg-Mardquardt is usually a good choice),
define the function to minimize, and select an initial value.
You can also specify a Jacobian, an N by M matrix, which is the residual functions' derivative. If you do not
specify this function then it will be computed for you numerically.

The code below walks you through each step.

:gitexample:`ExampleNonLinearLeastSquares.java`

Here it is shown how to set up and perform the optimization. DDogleg is a bit unusual in that it allows you to invoke
each step of the optimization manually. The advantage of that is that it is very easy to abort your optimization early
if you run out of time. For sake of brevity, we use UtilProcess.optimize() to handle the iterations.

.. literalinclude:: ../../../examples/src/org/ddogleg/example/ExampleNonLinearLeastSquares.java
   :language: java
   :linenos:
   :start-after: public class ExampleNonLinearLeastSquares
   :tab-width: 4
   :dedent: 4

When you run this example you should see something like the following:

.. literalinclude:: ../example_output/nonlinearleastsquares.txt

:gitexample:`FunctionLineDistanceEuclidean.java`

This is where the function being optimized is defined. It implements FunctionNtoM, which defines a function that takes in N inputs and generates M outputs.
Each output is the residual error for function 'i'. Here a line in 2D is defined by a point in 2D, e.g. :math:`(x_0,y_0)`.
The line passes through this point and is also tangent to the point, i.e. slope = :math:`(-y_0,x_0)`. The error
is defined as the difference between the point and the closest point on the line.

The line is defined by two parameters so the function has two inputs. The number of outputs that it has is determined
by the number of points it is being fit against. These points are specified in the constructor.

.. literalinclude:: ../../../examples/src/org/ddogleg/example/FunctionLineDistanceEuclidean.java
   :language: java
   :linenos:
   :lines: 31-
   :tab-width: 4
   :dedent: 0

