package com.superluli.infra.commons;

import java.util.List;

public abstract class AbstractRestResourceList<T extends AbstractRestResource> {

	protected String href;
	protected String prev;
	protected String next;
	protected int limit;
	protected List<T> elements;

	protected AbstractRestResourceList(){
		
	}
	
	protected AbstractRestResourceList(String href, String prev, String next, int limit, List<T> elements) {
		
		super();
		this.href = href;
		this.prev = prev;
		this.next = next;
		this.limit = limit;
		this.elements = elements;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @param href
	 *            the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the prev
	 */
	public String getPrev() {
		return prev;
	}

	/**
	 * @param prev
	 *            the prev to set
	 */
	public void setPrev(String prev) {
		this.prev = prev;
	}

	/**
	 * @return the next
	 */
	public String getNext() {
		return next;
	}

	/**
	 * @param next
	 *            the next to set
	 */
	public void setNext(String next) {
		this.next = next;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @param limit
	 *            the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * @return the elements
	 */
	public List<T> getElements() {
		return elements;
	}

	/**
	 * @param elements
	 *            the elements to set
	 */
	public void setElements(List<T> elements) {
		this.elements = elements;
	}
}
