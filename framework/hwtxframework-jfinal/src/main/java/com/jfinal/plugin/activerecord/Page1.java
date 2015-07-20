/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord;

import java.io.Serializable;
import java.util.List;

/**
 * Page is the result of Model.paginate(......) or Db.paginate(......)
 */
public class Page1<T> implements Serializable {

    private static final long serialVersionUID = -5395997221963176643L;

    private List<T> list; // list result of this page
    private int totalRow; // total row

    /**
     * Constructor.
     *
     * @param list
     *            the list of paginate result
     * @param totalRow
     *            the total row of paginate
     */
    public Page1(List<T> list, int totalRow) {
        this.list = list;
        this.totalRow = totalRow;
    }

    /**
     * Return list of this page.
     */
    public List<T> getList() {
        return list;
    }

    /**
     * Return total row.
     */
    public int getTotalRow() {
        return totalRow;
    }
}