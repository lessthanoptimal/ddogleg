/*
 * Copyright (c) 2012-2015, Peter Abeles. All Rights Reserved.
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

package org.ddogleg.stats;

import org.ddogleg.sorting.QuickSelect;
import org.ddogleg.struct.GrowQueue_F64;

/**
 * @author Peter Abeles
 */
public class UtilStatisticsQueue {

	public static double mean( GrowQueue_F64 list ) {
		double total = 0;
		for (int i = 0; i < list.size(); i++) {
			total += list.data[i];
		}

		return total / list.size();
	}

	public static double variance( GrowQueue_F64 list , double mean ) {
		double total = 0;
		for (int i = 0; i < list.size(); i++) {
			double d = list.data[i] - mean;
			total += d*d;
		}

		return total / (list.size()-1);
	}

	public static double stdev( GrowQueue_F64 list , double mean ) {
		return Math.sqrt(variance(list,mean));
	}

	public static double fraction( GrowQueue_F64 list , double fraction ) {
		int k = (int)((list.size-1)*fraction+0.5);
		return QuickSelect.select(list.data,k,list.size);
	}
}
