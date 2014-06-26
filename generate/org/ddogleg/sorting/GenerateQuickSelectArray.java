/*
 * Copyright (c) 2012-2013, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.sorting;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * @author Peter Abeles
 */
public class GenerateQuickSelectArray {
	String className = "QuickSelectArray";

	PrintStream out;
	
	String type;

	public GenerateQuickSelectArray() throws FileNotFoundException {
		out = new PrintStream(new FileOutputStream(className + ".java"));
	}

	public void generate()  throws FileNotFoundException {
		printPreamble();

		printFunctions("float");
		printFunctions("double");
		printFunctions("long");
		printFunctions("int");
		printFunctions("short");
		printFunctions("byte");

		out.print("\n" +
				"}\n");
	}

	private void printPreamble() {
		out.print("package pja.sorting;\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * QuickSelect searches for the k-th largest item in the list.  While doing this search\n" +
				" * it will sort the list partially.  all the items below k will have a value less than it\n" +
				" * and all the items more than k will have a value greater than it.  However the values\n" +
				" * above and below can be unsorted.  QuickSelect is faster than QuickSort of you don't\n" +
				" * need a fully sorted list.\n" +
				" * </p>\n" +
				" * <p>\n" +
				" * An implementation of the quick select algorithm from Numerical Recipes Third Edition\n" +
				" * that is specified for arrays of doubles.  See page 433.\n" +
				" * </p>\n" +
				" *\n" +
				" *\n" +
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	private void printFunctions(String type ) {
		out.print("   /**\n" +
				"\t * Sorts the array such that the values in the array up to and including\n" +
				"\t * 'k' are sorted the least to greatest.  This implies that the array\n" +
				"\t * itself is modified. For convinience the 'k' element is returned.\n" +
				"\t *\n" +
				"\t * @param data The unsorted list. Is modified.\n" +
				"\t * @param k The element of the sorted list that is to be found\n" +
				"\t * @param maxIndex Only element up to this value are considered\n" +
				"\t * @return the 'k'th largest element\n" +
				"\t */\n" +
				"\tpublic static "+type+" select( "+type+" []data , int k , int maxIndex ) {\n" +
				"\n" +
				"\t\tint i,j,mid;\n" +
				"\t\tint n = maxIndex;\n" +
				"\t\t"+type+" a;\n" +
				"\t\tint l = 0;\n" +
				"\t\tint ir = n-1;\n" +
				"\n" +
				"\t\t"+type+" temp;\n" +
				"\n" +
				"\t\tfor(;;) {\n" +
				"\t\t\tif( ir <= l+1 ) {\n" +
				"\t\t\t\tif( ir == l+1 && data[ir] < data[l] ) {\n" +
				"\t\t\t\t\ttemp = data[l];\n" +
				"\t\t\t\t\tdata[l] = data[ir];\n" +
				"\t\t\t\t\tdata[ir] = temp;\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\treturn data[k];\n" +
				"\t\t\t} else {\n" +
				"\t\t\t\tmid = (l+ir) >> 1;\n" +
				"\n" +
				"\t\t\t\tint lp1 = l+1;\n" +
				"\t\t\t\ttemp = data[mid];\n" +
				"\t\t\t\tdata[mid] = data[lp1];\n" +
				"\t\t\t\tdata[lp1] = temp;\n" +
				"\n" +
				"\t\t\t\tif( data[l] > data[ir] ) {\n" +
				"\t\t\t\t\ttemp = data[l];\n" +
				"\t\t\t\t\tdata[l] = data[ir];\n" +
				"\t\t\t\t\tdata[ir] = temp;\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\tif( data[lp1] > data[ir] ) {\n" +
				"\t\t\t\t\ttemp = data[lp1];\n" +
				"\t\t\t\t\tdata[lp1] = data[ir];\n" +
				"\t\t\t\t\tdata[ir] = temp;\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\tif( data[l] > data[lp1] ) {\n" +
				"\t\t\t\t\ttemp = data[lp1];\n" +
				"\t\t\t\t\tdata[lp1] = data[l];\n" +
				"\t\t\t\t\tdata[l] = temp;\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\ti=lp1;\n" +
				"\t\t\t\tj=ir;\n" +
				"\t\t\t\ta=data[lp1];\n" +
				"\n" +
				"\t\t\t\tfor(;;) {\n" +
				"\t\t\t\t\tdo i++; while(data[i]<a);\n" +
				"\t\t\t\t\tdo j--; while (data[j]>a);\n" +
				"\t\t\t\t\tif( j < i) break;\n" +
				"\t\t\t\t\ttemp = data[i];\n" +
				"\t\t\t\t\tdata[i] = data[j];\n" +
				"\t\t\t\t\tdata[j] = temp;\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\tdata[lp1] = data[j];\n" +
				"\t\t\t\tdata[j] = a;\n" +
				"\t\t\t\tif( j >= k ) ir=j-1;\n" +
				"\t\t\t\tif( j <= k ) l=i;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
		
		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Returns the original index of the 'k' largest element in the list.\n" +
				"\t * </p>\n" +
				"\t * \n" +
				"\t * <p>\n" +
				"\t * Note: There is additional overhead since the values of indexes needs to be set\n" +
				"\t * </p>\n" +
				"\t * \n" +
				"\t * @param indexes Temporary storage and is overwritten\n" +
				"\t */\n" +
				"\tpublic static int selectIndex( "+type+" []data , int k , int maxIndex ,  int []indexes) {\n" +
				"\n" +
				"\t\tfor( int i = 0; i < indexes.length; i++ ) {\n" +
				"\t\t\tindexes[i] = i;\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tint i,j,mid;\n" +
				"\t\tint n = maxIndex;\n" +
				"\t\t"+type+" a;\n" +
				"\t\tint indexA;\n" +
				"\t\tint l = 0;\n" +
				"\t\tint ir = n-1;\n" +
				"\n" +
				"\t\tfor(;;) {\n" +
				"\t\t\tif( ir <= l+1 ) {\n" +
				"\t\t\t\tif( ir == l+1 && data[ir] < data[l] ) {\n" +
				"\t\t\t\t\tswap(data,indexes,l,ir);\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\treturn indexes[k];\n" +
				"\t\t\t} else {\n" +
				"\t\t\t\tmid = (l+ir) >> 1;\n" +
				"\n" +
				"\t\t\t\tint lp1 = l+1;\n" +
				"\t\t\t\tswap(data,indexes,mid,lp1);\n" +
				"\n" +
				"\t\t\t\tif( data[l] > data[ir] ) {\n" +
				"\t\t\t\t\tswap(data,indexes,l,ir);\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\tif( data[lp1] > data[ir] ) {\n" +
				"\t\t\t\t\tswap(data,indexes,lp1,ir);\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\tif( data[l] > data[lp1] ) {\n" +
				"\t\t\t\t\tswap(data,indexes,lp1,l);\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\ti=lp1;\n" +
				"\t\t\t\tj=ir;\n" +
				"\t\t\t\ta=data[lp1];\n" +
				"\t\t\t\tindexA=indexes[lp1];\n" +
				"\n" +
				"\t\t\t\tfor(;;) {\n" +
				"\t\t\t\t\tdo i++; while(data[i]<a);\n" +
				"\t\t\t\t\tdo j--; while (data[j]>a);\n" +
				"\t\t\t\t\tif( j < i) break;\n" +
				"\t\t\t\t\tswap(data,indexes,i,j);\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\tdata[lp1] = data[j];\n" +
				"\t\t\t\tdata[j] = a;\n" +
				"\t\t\t\tindexes[lp1] = indexes[j];\n" +
				"\t\t\t\tindexes[j] = indexA;\n" +
				"\t\t\t\tif( j >= k ) ir=j-1;\n" +
				"\t\t\t\tif( j <= k ) l=i;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\n" +
				"\tprivate static void swap( "+type+"[] data , int []indexes, int a , int b )\n" +
				"\t{\n" +
				"\t\t"+type+" tempD = data[a];\n" +
				"\t\tint tempI = indexes[a];\n" +
				"\n" +
				"\t\tdata[a] = data[b];\n" +
				"\t\tindexes[a] = indexes[b];\n" +
				"\n" +
				"\t\tdata[b] = tempD;\n" +
				"\t\tindexes[b] = tempI;\n" +
				"\t}\n\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GenerateQuickSelectArray app = new GenerateQuickSelectArray();
		app.generate();
	}
}
