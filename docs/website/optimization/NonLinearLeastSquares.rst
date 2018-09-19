Non-Linear Least Squares
########################


The following code demonstrates non-linear least squares minimization by finding the best fit line to a set of points.
See the `optimization manual <Manual.html>`__ for an overview of all optimization techniques available in DDogleg.
Unconstrained least-squares minimization solves problems which can be described by a function of the form:

  .. math::

    \min\limits_{\boldsymbol{x} \in \Re^N} f(\boldsymbol{x})=\frac{1}{2}\sum^m_{i=1} r^2_i(\boldsymbol{x})

where :math:`r_i(\boldsymbol{x}) = f_i(\boldsymbol{x}) - y_i` is a scalar function which outputs the residual for function :math:`i`, predicted value subtracted the observed value.
By definition :math:`f(\boldsymbol{x}) \ge 0`. In DDogleg you don't define the :math:`f(\boldsymbol{x})` function directly but instead define the set of :math:`r_j(\bm{x})` functions
by implementing FunctionNtoM. Implementations of FunctionNtoM take in an array with N elements and output an array with M elements.

In this example we will consider a very simple unconstrained least-squares problem, fitting a line to a set of points.
To solve non-linear problems you will need to select which method to use (Levenberg-Mardquardt is usually a good choice),
define the function to minimize, and select an initial value.
You can also specify a Jacobian, an N by M matrix, which is the residual functions' derivative. If you do not
specify this function then it will be computed for you numerically.

The specific function being optimized is below:

  .. math::
    t_i = \frac{-p_1(x_i-p_0) + p_0(y_i-p_1)}{p_0^2 + p_i^2}

  .. math::
    f_{2i}(\boldsymbol{p}) &= x_i -p_0 + t_i p_1 \\
    f_{2i+1}(\boldsymbol{p}) &= y_i -p_1 - t_i p_0 \\
where :math:`(x_i,y_i)` is a point being fit to and :math:`\boldsymbol{p}=(p_0,p_1)` is the line being fit to. The line has been
parameterized using the closest point on the line to the origin. Thus the line's slope is :math:`\boldsymbol{p}=(-p_1,p_0)`.

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

The java code for the function being optimized is shown below. It was mathematically described above previously.

.. literalinclude:: ../../../examples/src/org/ddogleg/example/FunctionLineDistanceEuclidean.java
   :language: java
   :linenos:
   :lines: 31-
   :tab-width: 4
   :dedent: 0

