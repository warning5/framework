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

package com.jfinal.log;

import org.apache.log4j.Level;

/**
 * Log4jLogger.
 */
public class Log4jLogger extends Logger {

    private static final String callerFQCN = Log4jLogger.class.getName();
    private org.apache.log4j.Logger log;

    Log4jLogger(Class<?> clazz) {
        log = org.apache.log4j.Logger.getLogger(clazz);
    }

    Log4jLogger(String name) {
        log = org.apache.log4j.Logger.getLogger(name);
    }

    public void info(String message) {
        log.log(callerFQCN, Level.INFO, message, null);
    }

    public void info(String message, Throwable t) {
        log.log(callerFQCN, Level.INFO, message, t);
    }

    public void debug(String message) {
        log.log(callerFQCN, Level.DEBUG, message, null);
    }

    public void debug(String message, Throwable t) {
        log.log(callerFQCN, Level.DEBUG, message, t);
    }

    public void warn(String message) {
        log.log(callerFQCN, Level.WARN, message, null);
    }

    public void warn(String message, Throwable t) {
        log.log(callerFQCN, Level.WARN, message, t);
    }

    public void error(String message) {
        log.log(callerFQCN, Level.ERROR, message, null);
    }

    public void error(String message, Throwable t) {
        log.log(callerFQCN, Level.ERROR, message, t);
    }

    public void fatal(String message) {
        log.log(callerFQCN, Level.FATAL, message, null);
    }

    public void fatal(String message, Throwable t) {
        log.log(callerFQCN, Level.FATAL, message, t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return log.isEnabledFor(Level.WARN);
    }

    public boolean isErrorEnabled() {
        return log.isEnabledFor(Level.ERROR);
    }

    public boolean isFatalEnabled() {
        return log.isEnabledFor(Level.FATAL);
    }
}

