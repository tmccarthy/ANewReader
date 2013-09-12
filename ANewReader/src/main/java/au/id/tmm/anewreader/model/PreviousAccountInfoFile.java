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

package au.id.tmm.anewreader.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import au.id.tmm.anewreader.model.net.ReaderServiceAuthenticationHelper;
import au.id.tmm.anewreader.utility.IOUtils;

/**
 * Class representing the file used to store information about the previous account used. This is
 * used to maintain login sessions across multiple uses of the app.
 */
public class PreviousAccountInfoFile {

    private static final int INITIAL_FILE_VERSION = 1;
    private static final int CURRENT_FILE_VERSION = 1;

    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final String VERSION_KEY = "version";
    private static final String READER_BASE_URL_KEY = "readerBaseUrl";
    private static final String READER_TITLE_KEY = "reader";
    private static final String TOKEN_KEY = "token";
    private static final String USERNAME_KEY = "username";

    private File infoFile;

    public static PreviousAccountInfoFile getInfoFile(File parentDirectory, String filename) {
        return new PreviousAccountInfoFile(parentDirectory, filename);
    }

    private PreviousAccountInfoFile(File parentDirectory, String filename) {

        this.infoFile = new File(parentDirectory, filename);
    }

    /**
     * Retrieves the previous account from the file. If there was an error when reading or parsing
     * the file, returns null.
     */
    public Account getPreviousAccount() {

        try {

            boolean doRetry;

            do {
                doRetry = false;

                try {

                    JSONObject previousAccountInfo = new JSONObject(IOUtils.readFileAsString(infoFile, DEFAULT_ENCODING));

                    String readerBaseUrl = previousAccountInfo.getString(READER_BASE_URL_KEY);
                    String readerTitle = previousAccountInfo.getString(READER_TITLE_KEY);
                    String token = previousAccountInfo.getString(TOKEN_KEY);
                    String username = previousAccountInfo.getString(USERNAME_KEY);

                    ReaderService readerService = new ReaderService(readerBaseUrl, readerTitle);
                    ReaderServiceAuthenticationHelper authHelper = ReaderServiceAuthenticationHelper.generate(readerService, token);

                    return new Account(username, readerService, authHelper);
                } catch (JSONException e) {

                    // Something has gone wrong with the format of the previously stored account.

                    // Here we would attempt to retrieve the version of the file according to the process
                    // for previous versions, until we either correctly retrieve the version or we try
                    // everything.

                    for (int i = INITIAL_FILE_VERSION; i <= CURRENT_FILE_VERSION; i++) {
                        try {
                            int previousVersion = this.retrieveVersion(i);
                            this.upgrade(previousVersion, CURRENT_FILE_VERSION);
                        } catch (UnableToRetrieveVersionException retrieveVersionException) {
                            doRetry = true;
                            break;
                        } catch (UpgradeNotSuccessfulException e1) {
                            return null;
                        }
                    }
                }
            } while (doRetry);

        } catch (IOException e) {
            return null;
        }
        // This should never be reached, but to keep the compiler happy...
        return null;
    }

    /**
     * Write the given account to the file.
     */
    public void storeAccount(Account account) throws IOException {
        try {
            JSONObject accountInfo = new JSONObject();

            accountInfo.put(READER_BASE_URL_KEY, account.getReaderService().getBaseUrl());
            accountInfo.put(READER_TITLE_KEY, account.getReaderService().getTitle());
            accountInfo.put(TOKEN_KEY, account.getAuthHelper().getToken());
            accountInfo.put(USERNAME_KEY, account.getUsername());

            IOUtils.writeStringToFile(this.infoFile, DEFAULT_ENCODING, accountInfo.toString());

        } catch (JSONException e) {
            // Something has gone wrong with the construction of the json object. This is generally
            // going to be caused by application logic gone awry, so we throw a runtime exception.
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to retrieve the version number of the file.
     */
    private int retrieveVersion(int version) throws UnableToRetrieveVersionException, IOException {
        try {
            switch (version) {
                case 1:
                    JSONObject previousAccountInfo = new JSONObject(IOUtils.readFileAsString(infoFile, DEFAULT_ENCODING));
                    return previousAccountInfo.getInt(VERSION_KEY);
                default:
                    throw new UnableToRetrieveVersionException();
            }
        } catch (JSONException e) {
            throw new UnableToRetrieveVersionException(e);
        }
    }

    /**
     * Used to upgrade an account info file from one version to another.
     */
    private void upgrade(int previousVersion, int newVersion) throws UpgradeNotSuccessfulException {
        // Add upgrade procedure as the version of this file changes.
        throw new UpgradeNotSuccessfulException();
    }


    private class UpgradeNotSuccessfulException extends Exception {
        private UpgradeNotSuccessfulException() {
        }

        private UpgradeNotSuccessfulException(String detailMessage) {
            super(detailMessage);
        }

        private UpgradeNotSuccessfulException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        private UpgradeNotSuccessfulException(Throwable throwable) {
            super(throwable);
        }
    }

    private class UnableToRetrieveVersionException extends Exception {
        private UnableToRetrieveVersionException() {
        }

        private UnableToRetrieveVersionException(String detailMessage) {
            super(detailMessage);
        }

        private UnableToRetrieveVersionException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        private UnableToRetrieveVersionException(Throwable throwable) {
            super(throwable);
        }
    }

}
