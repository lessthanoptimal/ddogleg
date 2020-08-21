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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Abeles
 */
public class TestGraphDataManager
{
	@Test
	void reset()
	{
		GraphDataManager alg = new GraphDataManager();

		Node n = alg.createNode();
		n.data = 1;
		n.edges.grow();

		Edge e = alg.createEdge();
		e.data = 1;
		e.dest = n;

		alg.reset();

		assertEquals(0,alg.usedEdges.size());
		assertEquals(1,alg.unusedEdges.size());
		assertEquals(0,alg.usedNodes.size());
		assertEquals(1,alg.unusedNodes.size());

		assertNotNull(((Edge) alg.unusedEdges.getFirst()).data);
		assertNotNull(((Edge) alg.unusedEdges.getFirst()).dest);
		assertNotNull(((Node) alg.unusedNodes.getFirst()).data);
		assertTrue(((Node)alg.unusedNodes.getFirst()).edges.size != 0 );
	}

	@Test
	void resetHard()
	{
		GraphDataManager alg = new GraphDataManager();

		Node n = alg.createNode();
		n.data = 1;
		n.edges.grow();

		Edge e = alg.createEdge();
		e.data = 1;
		e.dest = n;

		alg.resetHard();

		assertEquals(0,alg.usedEdges.size());
		assertEquals(1,alg.unusedEdges.size());
		assertEquals(0,alg.usedNodes.size());
		assertEquals(1,alg.unusedNodes.size());

		assertNull(((Edge) alg.unusedEdges.getFirst()).data);
		assertNull(((Edge) alg.unusedEdges.getFirst()).dest);
		assertNull(((Node) alg.unusedNodes.getFirst()).data);
		assertEquals(((Node) alg.unusedNodes.getFirst()).edges.size, 0);
	}

	@Test
	void createEdge()
	{
		GraphDataManager<?,?> alg = new GraphDataManager<>();

		Edge e = alg.createEdge();
		assertEquals(1,alg.usedEdges.size());
		assertEquals(0,alg.unusedEdges.size());

		alg.reset();

		assertSame(e, alg.createEdge());
		assertEquals(1,alg.usedEdges.size());
		assertEquals(0,alg.unusedEdges.size());
	}

	@Test
	void recycleEdge()
	{
		GraphDataManager alg = new GraphDataManager();

		Edge e = alg.createEdge();
		assertEquals(1,alg.usedEdges.size());
		assertEquals(0,alg.unusedEdges.size());

		alg.recycleEdge(e);

		assertEquals(0,alg.usedEdges.size());
		assertEquals(1,alg.unusedEdges.size());
	}

	@Test
	void createNode()
	{
		GraphDataManager alg = new GraphDataManager();

		Node e = alg.createNode();
		assertEquals(1,alg.usedNodes.size());
		assertEquals(0,alg.unusedNodes.size());

		alg.recycleNode(e);

		assertEquals(0,alg.usedNodes.size());
		assertEquals(1, alg.unusedNodes.size());
	}

	@Test
	void recycleNode()
	{
		GraphDataManager alg = new GraphDataManager();

		Node n = alg.createNode();
		assertEquals(1,alg.usedNodes.size());
		assertEquals(0,alg.unusedNodes.size());

		alg.reset();

		assertSame(n, alg.createNode());
		assertEquals(1,alg.usedNodes.size());
		assertEquals(0, alg.unusedNodes.size());
	}
}
