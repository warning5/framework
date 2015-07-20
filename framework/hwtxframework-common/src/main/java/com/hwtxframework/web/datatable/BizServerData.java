package com.hwtxframework.web.datatable;

import java.io.Serializable;

public class BizServerData implements Serializable{

	private static final long serialVersionUID = 1L;

	private String name;
	private String position;
	private String office;
	private String extn;
	private String start_date;
	private String salary;
	private String DT_RowClass;


	public String getDT_RowClass() {
		return DT_RowClass;
	}

	public void setDT_RowClass(String dT_RowClass) {
		DT_RowClass = dT_RowClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getOffice() {
		return office;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	public String getExtn() {
		return extn;
	}

	public void setExtn(String extn) {
		this.extn = extn;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

}