package com.hwtxframework.web.datatable;

import java.io.Serializable;
import java.util.List;

public class ServerResponseData<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sEcho;
	private int iTotalRecords;
	private int iTotalDisplayRecords;
	private List<T> aaData;

	public List<T> getAaData() {
		return aaData;
	}

	public void setAaData(List<T> aaData) {
		this.aaData = aaData;
	}

	public String getSEcho() {
		return sEcho;
	}

	public void setSEcho(String sEcho) {
		this.sEcho = sEcho;
	}

	public int getITotalRecords() {
		return iTotalRecords;
	}

	public void setITotalRecords(int iTotalRecords) {
		this.iTotalRecords = iTotalRecords;
	}

	public int getITotalDisplayRecords() {
		return iTotalDisplayRecords;
	}

	public void setITotalDisplayRecords(int iTotalDisplayRecords) {
		this.iTotalDisplayRecords = iTotalDisplayRecords;
	}

}