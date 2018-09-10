Optimization
#######################

The following code demonstrates non-linear optimization by finding the best fit line to a set of points. This problem can be solved exactly using linear algebra and is solved using a non-linear method for demonstration purposes only.

When performing non-linear optimization you need to specify the function that you are optimizing.
In this situation a non-linear least squares solver is used which specified the residual error for N functions. Residual
error for function 'i' is defined as :math:`r_i(x) = f_i(x) - y_i`.


`ExampleMinimization.java <https://github.com/lessthanoptimal/ddogleg/blob/v0.15/examples/src/org/ddogleg/example/ExampleMinimization.java>`_

Here it is shown how to set up and perform the optimization. DDogleg is a bit unusual in that it allows you to invoke
each step of the optimization manually. Since this is more verbose a helper function (UtilOptimize.process) is used instead.

.. literalinclude:: ../../examples/src/org/ddogleg/example/ExampleMinimization.java
   :language: java
   :linenos:
   :start-after: public class ExampleMinimization
   :tab-width: 4
   :dedent: 4

`FunctionLineDistanceEuclidean.java <https://github.com/lessthanoptimal/ddogleg/blob/v0.15/examples/src/org/ddogleg/example/FunctionLineDistanceEuclidean.java>`_

This is where the function being optimized is defined. It implements FunctionNtoM, which defines a function that takes in N inputs and generates M outputs.
Each output is the residual error for function 'i'.

.. literalinclude:: ../../examples/src/org/ddogleg/example/FunctionLineDistanceEuclidean.java
   :language: java
   :linenos:
   :lines: 31-
   :tab-width: 4
   :dedent: 0

