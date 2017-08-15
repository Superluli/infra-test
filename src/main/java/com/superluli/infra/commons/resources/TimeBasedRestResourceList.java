package com.superluli.infra.commons.resources;

import java.util.List;

public class TimeBasedRestResourceList<T extends AbstractRestResource> extends AbstractRestResourceList<T> {

	protected long start;

	public TimeBasedRestResourceList() {
		super();
	}

	public TimeBasedRestResourceList(String href, String prev, String next, long start, int limit, List<T> elements) {

		super(href, prev, next, limit, elements);
		this.start = start;
	}

	/**
	 * @return the start
	 */
	public long getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(long start) {
		this.start = start;
	}
}
