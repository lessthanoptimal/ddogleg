BigDogArray
#######################

BigDogArrays are designed to store extremely large arrays. This is accomplished by internally using an array-of-arrays
as a net results they are more complex to work with than DogArrays. In fact, it would be trivial to modify them to
exceed the 32-bit limit of Java arrays, but this capability isn't enabled for backwards compatibility. At least
not yet.

:gitexample:`ExampleBigDogArray.java`

.. literalinclude:: ../../examples/src/org/ddogleg/example/ExampleBigDogArray.java
   :language: java
   :linenos:
   :start-after: public class
   :tab-width: 4
   :dedent: 4

