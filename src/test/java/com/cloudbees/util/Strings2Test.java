/*
 * Copyright 2010-2013, CloudBees Inc.
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
package com.cloudbees.util;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

/**
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class Strings2Test {
    @Test
    public void testJoin() throws Exception {
        String[] in = {"this", "is", "a", "sentence"};
        String actual = Strings2.join(in, " ");
        String expected = "this is a sentence";
        assertThat(actual, is(expected));
    }

    @Test
    public void indent_multi_line_string() throws Exception {
        String indentation = "   ";
        String in = "line1\n" +
                "line2\n" +
                "line3";
        String actual = Strings2.indent(in, indentation);

        String expected = indentation + "line1\n" +
                indentation + "line2\n" +
                indentation + "line3";
        assertThat(actual, is(expected));
    }

    @Test
    public void indent_mono_line_string() throws Exception {
        String indentation = "   ";
        String in = "line1";
        String actual = Strings2.indent(in, indentation);

        String expected = indentation + "line1";
        assertThat(actual, is(expected));
    }

    @Test
    public void indent_blank_string() throws Exception {
        String indentation = "   ";
        String in = "";
        String actual = Strings2.indent(in, indentation);

        String expected = indentation + "";
        assertThat(actual, is(expected));
    }
}
