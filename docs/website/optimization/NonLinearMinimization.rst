Non-Linear Minimization
########################

The following code demonstrates non-linear minimization by minimizing a classic test problem, Rosenbrock.
See the `optimization manual <Manual.html>`__ for an overview of all optimization techniques available in DDogleg.
Unconstrained minimization solves problems which can be described by a function of the form:

  .. math::

    \min\limits_{\boldsymbol{x} \in \Re^N} f(\boldsymbol{x})

where :math:`f(\boldsymbol{x})` is a function with a scalar output but takes in m variables. The actual formula being
minimized is:

  .. math::
    f(\boldsymbol{x}) = 100(x_2 - x_1^2)^2 + (1-x_1)^2

We will use Quasi-Newton BFGS to solve this problem. There are other options available. See the manual referenced
above for a list.

:gitexample:`ExampleUnconstrainedMinimization.java`

Here it is shown how to set up and perform the optimization. DDogleg is a bit unusual in that it allows you to invoke
each step of the optimization manually. The advantage of that is that it is very easy to abort your optimization early
if you run out of time. For sake of brevity, we use UtilProcess.optimize() to handle the iterations.

.. literalinclude:: ../../../examples/src/org/ddogleg/example/ExampleUnconstrainedMinimization.java
   :language: java
   :linenos:
   :start-after: public class ExampleUnconstrainedMinimization
   :tab-width: 4
   :dedent: 4

When you run this example you should see something like the following:

.. literalinclude:: ../example_output/nonlinearminimization.txt

