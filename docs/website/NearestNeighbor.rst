Nearest Neighbor
#######################

A nearest neighbor searches for all the neighbors of a point inside of a set which minimizes a distance metric.  In DDogleg the only distance metric available is Euclidean distance squares, which is the same as minimizing Euclidean distance, but faster.

`ExampleNearestNeighbor.java <https://github.com/lessthanoptimal/ddogleg/blob/v0.10/examples/src/org/ddogleg/example/ExampleNearestNeighbor.java>`_

.. literalinclude:: examples/ExampleNearestNeighbor.java
   :language: java
   :linenos:
   :tab-width: 4
   :dedent: 2
