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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class Parser {

	/**
	 * Returns all bugfix branches
	 */
	public static List<Ref> getAllBugfixBranches(final Repository repository) throws JGitInternalException, GitAPIException {
		final List<Ref> bugFixBranch = new ArrayList<Ref>();
		final Git git = new Git(repository);
		final List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call();
		for (final Ref branch : branches) {
			if (branch.getName().contains("bugfix")) {
				// System.out.println("Bug Branch Found: " + branch.getName());
				bugFixBranch.add(branch);
			}
		}
		System.out.println("\n\nTotal bug branches found :" + branches.size());
		git.close();
		return bugFixBranch;
	}

	public static void getFilesInCommit(final Repository repository, final RevCommit commit, final CommitInfo fixInfo,
			final Date earliestCommitDate) {
		final RevWalk rw = new RevWalk(repository);
		final DiffFormatter df = new DiffFormatter(new ByteArrayOutputStream());
		df.setRepository(repository);
		RevCommit parent;
		try {
			parent = rw.parseCommit(commit.getParent(0).getId());
			final List<DiffEntry> entries = df.scan(parent.getTree(), commit.getTree());

			for (final DiffEntry entry : entries) {
				final String filePath = entry.getNewPath();
				if (validFilePath(filePath)) {
					final String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
					if (fileName.equals("null") || fileName.equals("MANIFEST.MF")) {
						continue;
					}
					// System.out.println("\n" + fileName);
					// System.out.println(filePath);

					// Increment occurance count
					if (BugHotSpot.fileNamesWithOccuranceCount.containsKey(filePath)) {
						final double existingCount = BugHotSpot.fileNamesWithOccuranceCount.get(filePath);
						BugHotSpot.fileNamesWithOccuranceCount.put(filePath, existingCount + 1);
					} else {
						BugHotSpot.fileNamesWithOccuranceCount.put(filePath, 1D);
					}
					// System.out.println(commit.getFullMessage());

					fixInfo.setCommitId(commit.getId().getName());
					fixInfo.setAuthor(commit.getAuthorIdent().getName());
					final Date commitDate = new Date(commit.getCommitTime() * 1000L);
					fixInfo.setDate(commitDate);
					fixInfo.setCommitMessage(commit.getFullMessage());
					fixInfo.getFilesPaths().put(fileName, filePath);

					final double getWeight = getWeight(getNormalisedDate(earliestCommitDate, commitDate));
					fixInfo.getFileNamesWithWeight().put(filePath, getWeight);
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			rw.close();
			df.close();
		}
	}

	private static double getNormalisedDate(final Date earliestCommitDate, final Date commitDate) {
		final Date currentDate = new Date(System.currentTimeMillis());
		final double minDate = earliestCommitDate.getTime();
		final double maxDate = currentDate.getTime();
		final double dateOfCommit = commitDate.getTime();
		final double normalisedValue = (dateOfCommit - minDate) / (maxDate - minDate);
		// System.out.println("normalised date" + normalisedValue);
		System.out.print(".");
		return normalisedValue;
	}

	// Source google makes use of this algorithm
	private static double getWeight(final double normalizedCommitDate) {
		final double weight = 1 / (1 + Math.exp((-12 * normalizedCommitDate) + 12));
		return weight;
	}

	private static boolean validFilePath(final String filePath) {
		if (!filePath.contains("Test.java") && filePath.contains("/")) {
			return true;
		}
		return false;
	}
}
