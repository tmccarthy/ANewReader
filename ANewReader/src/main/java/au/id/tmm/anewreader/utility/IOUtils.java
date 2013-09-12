/*******************************************************************************
 * This file is part of A New Reader
 * Copyright (C) 2013 Timothy McCarthy
 *
 * A New Reader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * A New Reader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with A New Reader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package au.id.tmm.anewreader.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * Utility class with static utility methods for I/O.
 */
public class IOUtils {

    private IOUtils() {
    }

    /**
     * Read the contents of a file into a string using the given encoding.
     * @throws IOException
     */
    public static String readFileAsString(File file, String encoding) throws IOException {
        InputStream inputStream = new FileInputStream(file);

        return IOUtils.stringFromInputSteam(inputStream, encoding);
    }

    /**
     * Write the given string to the given file using the given encoding.
     * @throws IOException
     */
    public static void writeStringToFile(File file, String encoding, String string) throws IOException {

        OutputStream outputStream = new FileOutputStream(file);

        try {
            outputStream.write(string.getBytes(encoding));
        } finally {
            outputStream.close();
        }

    }

    /**
     * Takes a string from an InputStream. The given inputStream should be buffered (when
     * appropriate) before being passed to this method.
     */
    public static String stringFromInputSteam(InputStream inputStream, String encoding) throws IOException {
        try {

            Scanner s = new Scanner(inputStream, encoding).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";

        } finally {
            inputStream.close();
        }
    }

}
