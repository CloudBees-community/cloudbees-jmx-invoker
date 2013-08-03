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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

/**
 * String utils
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class Strings2 {
    public static final String LINE_BREAK = System.getProperty("line.separator");

    private Strings2() {

    }

    /**
     * Return <code>true</code> if the given <code>str</code> is <code>null</code> or empty.
     */
    public static boolean isEmpty(@Nullable String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Return <code>true</code> if the given <code>str</code> is not <code>null</code> and not empty.
     */
    public static boolean isNotEmpty(@Nullable String str) {
        return !isEmpty(str);
    }

    @Nullable
    public static String join(@Nullable Object[] objects, @Nullable String delimiter) {
        if (objects == null)
            return null;

        return join(Arrays.asList(objects), delimiter);
    }

    @Nullable
    public static String join(@Nullable Iterable<Object> objects, @Nullable String delimiter) {
        if (objects == null)
            return null;
        if (delimiter == null)
            delimiter = "";

        String result = "";
        for (Object o : objects) {
            result += toString(o) + delimiter;
        }

        if (!result.isEmpty()) {
            result = result.substring(0, result.length() - delimiter.length());
        }
        return result;
    }

    @Nullable
    public static String indent(@Nullable String str, @Nullable String indentation) {
        if (indentation == null)
            indentation = "";
        if (str == null)
            return indentation;

        BufferedReader reader = new BufferedReader(new StringReader(str));
        String result = "";
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                result += indentation + line + LINE_BREAK;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // remove last LINE_BREAK
        result = result.isEmpty() ? indentation : result.substring(0, result.length() - LINE_BREAK.length());

        return result;
    }

    @Nullable
    public static String toString(@Nullable Object o, @Nullable String nullDefault) {
        return (o != null) ? toString(o) : nullDefault;
    }

    @Nonnull
    public static String toString(@Nullable Object o) {
        if (o == null)
            return "#null#";
        String str = "";

        if (o.getClass().isArray()) {
            str += "[";
            for (int i = 0; i < Array.getLength(o); i++) {
                str += toString(Array.get(o, i));
                if (i < Array.getLength(o) - 1)
                    str += ", ";
            }
        } else {
            str += o.toString();
        }
        return str;
    }
}
