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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Lists;

public class BugHotSpot {

	public static final List<CommitInfo> fixInfoList = new ArrayList<>();
	public static Map<String, Double> fileNamesWithOccuranceCount = new LinkedHashMap<>();

	public static void main(final String[] args) throws IOException, GitAPIException {
		final Repository repo = new FileRepository("D:\\Referrals-Git\\.git");
		// final Repository repo = new FileRepository("D:\\HealthcarePathway\\Forms\\.git");
		// final Repository repo = new FileRepository("D:\\HealthcarePathway\\Pathways\\.git");
		// final Repository repo = new FileRepository("D:\\HealthcarePathway\\Designer\\.git");
		// final Repository repo = new FileRepository("D:\\Task\\.git");
		// final Repository repo = new FileRepository("D:\\BPM\\.git");

		final Git git = new Git(repo);

		final Iterable<RevCommit> commits = git.log().call();

		Date earliestCommitDate = new Date();
		final List<RevCommit> commitsList = Lists.newArrayList(commits.iterator());
		int bugCommits = 0;
		for (final RevCommit commit : commitsList) {
			if (validateCommit(commit.getFullMessage())) {
				final Date currentCommitDate = new Date(commit.getCommitTime() * 1000L);
				if (currentCommitDate.before(earliestCommitDate)) {
					earliestCommitDate = currentCommitDate;
				}
				bugCommits++;
			}
		}
		System.out.println("--------------------------------------------------------------------");
		System.out.println("Total commits found: " + commitsList.size());
		System.out.println("Total bug commits identified: " + bugCommits);
		System.out.println("Earliest bug commit date found: " + earliestCommitDate);
		System.out.println("--------------------------------------------------------------------");
		System.out.println("Normalising bug commits...");
		for (final RevCommit commit : commitsList) {
			if (validateCommit(commit.getFullMessage())) {
				final CommitInfo fixInfo = new CommitInfo();
				Parser.getFilesInCommit(repo, commit, fixInfo, earliestCommitDate);
				fixInfoList.add(fixInfo);
			}
		}
		System.out.println("\n\nCollating all bug commits and printing...");
		ResultPrinter.collateWeightsAndPrint(20);
		git.close();
	}

	private static boolean validateCommit(final String commitMessage) {
		return commitMessage.contains("Merge pull request") && commitMessage.contains("from bugfix/");
	}
}