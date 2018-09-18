Nearest Neighbor
#######################

A nearest neighbor searches for all the neighbors of a point inside of a set which minimizes a distance metric.  In DDogleg the only distance metric available is Euclidean distance squares, which is the same as minimizing Euclidean distance, but faster.

:gitexample:`ExampleNearestNeighbor.java`

.. literalinclude:: ../../examples/src/org/ddogleg/example/ExampleNearestNeighbor.java
   :language: java
   :linenos:
   :start-after: public class
   :tab-width: 4
   :dedent: 4
