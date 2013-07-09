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
package com.cloudbees.util.nio;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.Charset;

/**
 * File Utils.
 *
 * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
 */
public class Files2 {

    /**
     * Read the given file and return it as a String
     */
    @Nonnull
    public static String readFile(@Nonnull File file, @Nonnull String charset) throws IOException {
        return readFile(file, Charset.forName(charset));
    }

    /**
     * Read the given file and return it as a String
     */
    @Nonnull
    public static String readFile(@Nonnull File file, @Nonnull Charset charset) throws IOException {
        if (file == null)
            throw new NullPointerException("Given 'file' can not be null");
        if (!file.exists())
            throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
        if (file.isDirectory())
            throw new IllegalArgumentException("File " + file.getAbsolutePath() + " can NOT be a directory");

        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(in, bos);
        return new String(bos.toByteArray(), charset);

    }

    /**
     * Copy the given inputstream to the given outputstream.
     */
    public static void copy(@Nonnull InputStream in, @Nonnull OutputStream out) throws IOException {
        byte[] buffer = new byte[512];
        int length;
        while ((length = in.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
        }
    }
}
