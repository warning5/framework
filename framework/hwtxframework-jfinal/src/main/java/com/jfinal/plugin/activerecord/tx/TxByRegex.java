/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.plugin.activerecord.tx;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * TxByRegex.
 * For controller interception, the regular expression matching the controller key,
 * otherwise matching the method name of the method
 */
public class TxByRegex implements Interceptor {

    private Pattern pattern;

    public TxByRegex(String regex) {
        this(regex, true);
    }

    public TxByRegex(String regex, boolean caseSensitive) {
        if (StrKit.isBlank(regex))
            throw new IllegalArgumentException("regex can not be blank.");

        pattern = caseSensitive ? Pattern.compile(regex) : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public void intercept(final Invocation inv) {
        Config config = Tx.getConfigWithTxConfig(inv);
        if (config == null)
            config = DbKit.getConfig();

        String target = inv.isActionInvocation() ? inv.getActionKey() : inv.getMethodName();
        if (pattern.matcher(target).matches()) {
            DbPro.use(config.getName()).tx(new IAtom() {
                public boolean run() throws SQLException {
                    inv.invoke();
                    return true;
                }
            });
        } else {
            inv.invoke();
        }
    }
}


