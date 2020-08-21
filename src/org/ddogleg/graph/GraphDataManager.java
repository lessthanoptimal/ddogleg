/*
 * Copyright (c) 2012-2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of DDogleg (http://ddogleg.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ddogleg.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles creating and recycling data in a graph.
 *
 * @author Peter Abeles
 */
public class GraphDataManager<N,E>
{
   // edges which are being actively used
   protected List<Edge<N,E>> usedEdges = new ArrayList<>();
   // edge data which is not being used
   protected ArrayDeque<Edge<N,E>> unusedEdges = new ArrayDeque<>();

   // nodes which are being actively used
   protected List<Node<N,E>> usedNodes = new ArrayList<>();
   // node data which is not being used
   protected ArrayDeque<Node<N,E>> unusedNodes = new ArrayDeque<>();

   /**
    * Takes all the used nodes and makes them unused.
    */
   public void reset()
   {
      unusedEdges.addAll(usedEdges);
      unusedNodes.addAll(usedNodes);

      usedEdges.clear();
      usedNodes.clear();
   }

   /**
    * Takes all the used nodes and makes them unused.  Also dereferences any objects saved in 'data'.
    */
   public void resetHard() {
      for( int i = 0; i < usedEdges.size(); i++ ) {
         Edge<N,E> e = usedEdges.get(i);
         e.data = null;
         e.dest = null;
      }

      for( int i = 0; i < usedNodes.size(); i++ ) {
         Node<N,E> n = usedNodes.get(i);
         n.data = null;
         n.edges.reset();
      }

      unusedEdges.addAll(usedEdges);
      unusedNodes.addAll(usedNodes);

      usedEdges.clear();
      usedNodes.clear();
   }

   public Edge<N,E> createEdge() {
      Edge<N,E> e;
      if( unusedEdges.isEmpty() ) {
         e = new Edge<>();
      } else {
         e = unusedEdges.pop();
      }
      usedEdges.add(e);
      return e;
   }

   public void recycleEdge( Edge<N,E> e ) {
      if( !usedEdges.remove(e) ) {
         throw new IllegalArgumentException("The edge is not in the used list!");
      }
      unusedEdges.add(e);
   }

   public Node<N,E> createNode() {
      Node<N,E> n;
      if( unusedNodes.isEmpty() ) {
         n = new Node<>();
      } else {
         n = unusedNodes.pop();
      }
      usedNodes.add(n);
      return n;
   }

   public void recycleNode( Node<N,E> n ) {
      if( !usedNodes.remove(n) ) {
         throw new IllegalArgumentException("The edge is not in the used list!");
      }
      unusedNodes.add(n);
   }

}
