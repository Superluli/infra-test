package com.superluli.infra.commons;

import java.util.List;

public class IndexBasedRestResourceList<T extends AbstractRestResource> extends AbstractRestResourceList<T> {

	protected int start;

	public IndexBasedRestResourceList() {
		super();
	}

	public IndexBasedRestResourceList(String href, String prev, String next, int start, int limit, List<T> elements) {

		super(href, prev, next, limit, elements);
		this.start = start;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(int start) {
		this.start = start;
	}
}
