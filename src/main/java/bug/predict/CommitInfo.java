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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CommitInfo {
	private float weight;
	private String commitId;
	private String author;
	private Date date;
	private String commitMessage;
	private final Map<String, Double> fileNamesWithWeight = new HashMap<>();
	private final Map<String, String> filesPaths = new HashMap<>();

	public float getWeight() {
		return this.weight;
	}

	public void setWeight(final float weight) {
		this.weight = weight;
	}


	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(final String author) {
		this.author = author;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public String getCommitMessage() {
		return this.commitMessage;
	}

	public void setCommitMessage(final String commitMessage) {
		this.commitMessage = commitMessage;
	}

	public String getCommitId() {
		return this.commitId;
	}

	public void setCommitId(final String commitId) {
		this.commitId = commitId;
	}

	public Map<String, String> getFilesPaths() {
		return this.filesPaths;
	}

	public Map<String, Double> getFileNamesWithWeight() {
		return this.fileNamesWithWeight;
	}
}
