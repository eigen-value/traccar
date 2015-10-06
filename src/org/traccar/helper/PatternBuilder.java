/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.helper;

import java.util.regex.Pattern;

public class PatternBuilder {

    private final StringBuilder pattern = new StringBuilder();

    private final boolean collapse = true;

    public PatternBuilder xpr(String s) {
        pattern.append(s);
        return this;
    }

    public PatternBuilder txt(String s) {
        pattern.append(s.replaceAll("([\\\\\\.\\[\\{\\(\\*\\+\\?\\^\\$\\|])", "\\\\$1"));
        return this;
    }

    public PatternBuilder num(String s) {
        if (collapse) {
            s = s.replace("dddd", "d{4}").replace("ddd", "d{3}").replace("dd", "d{2}");
            s = s.replace("xxxx", "x{4}").replace("xxx", "x{3}").replace("xx", "x{2}");
        }
        pattern.append(s.replace("d", "\\d").replace("x", "\\p{XDigit}").replaceAll("([\\.\\|])", "\\\\$1"));
        return this;
    }

    public PatternBuilder any() {
        pattern.append(".*");
        return this;
    }

    public PatternBuilder not(String s) {
        return xpr("[^").txt(s).xpr("]+");
    }

    public PatternBuilder nxt(String s) {
        return not(s).txt(s);
    }

    public Pattern compile() {
        return Pattern.compile(pattern.toString(), Pattern.DOTALL);
    }

    @Override
    public String toString() {
        return pattern.toString();
    }

}