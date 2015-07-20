package com.hwtxframework.web.datatable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by panye on 2014/9/13.
 */
public class JqGridResponse<T> implements Serializable {

    private int total;
    private int page;
    private int records;

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public int getRecords() {
        return records;
    }

    public void setRecords(int records) {
        this.records = records;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    private List<T> rows;
}