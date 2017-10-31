/*
 * Copyright (c) Orchestral Developments Ltd and the Orion Health group of companies (2001 - 2016).
 *
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 */
package bug.predict;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ResultPrinter {

	public static void collateWeightsAndPrint(final int maxResults) {
		final Map<String, Double> fullFileNamesWithWeight = new HashMap<String, Double>();

		for (final CommitInfo commitInfo : BugHotSpot.fixInfoList) {
			final Map<String, Double> fileNamesWithWeight = commitInfo.getFileNamesWithWeight();
			final Iterator it = fileNamesWithWeight.entrySet().iterator();
			while (it.hasNext()) {
				final Map.Entry pair = (Map.Entry) it.next();

				final String key = (String) pair.getKey();
				final Double value = (Double) pair.getValue();

				final Double keyInFinalMap = fullFileNamesWithWeight.get(key);

				if (keyInFinalMap != null) {
					final Double newWeight = keyInFinalMap + value;
					fullFileNamesWithWeight.put(key, newWeight);
				} else {
					fullFileNamesWithWeight.put(key, value);
				}
				it.remove(); // avoids a ConcurrentModificationException
			}
		}
		final boolean DESC = false;
		final Map<String, Double> sortedMap = sortByComparator(fullFileNamesWithWeight, DESC);
		printMap(sortedMap, maxResults);
	}

	private static Map<String, Double> sortByComparator(final Map<String, Double> unsortMap, final boolean order) {

		final List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(final Entry<String, Double> o1,
					final Entry<String, Double> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		final Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (final Entry<String, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static void printMap(final Map<String, Double> map, final int totalEntries) {
		int count = 0;
		System.out.println("\n----------------------------------------");
		System.out.println(String.format("Top '%s' bug hotspots:", totalEntries));
		System.out.println("----------------------------------------");

		for (final Entry<String, Double> entry : map.entrySet()) {
			count++;
			System.out.println(count + ". " + entry.getKey() + " (Weight : " + entry.getValue() + ")");
			if (count > totalEntries) {
				break;
			}
		}
		System.out.println("\n----------------------------------------");
		System.out.println(String.format("Top '%s' occurance count:", totalEntries));
		System.out.println("----------------------------------------");

		final boolean DESC = false;

		final Map<String, Double> sortedCountMap = sortByComparator(BugHotSpot.fileNamesWithOccuranceCount, DESC);
		count = 0;
		for (final Entry<String, Double> entry : sortedCountMap.entrySet()) {
			count++;
			System.out.println(count + ". " + entry.getKey() + " (Value : " + entry.getValue() + ")");
			if (count > totalEntries) {
				break;
			}
		}
	}
}

