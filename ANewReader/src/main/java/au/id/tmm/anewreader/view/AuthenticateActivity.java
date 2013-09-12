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

package au.id.tmm.anewreader.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import au.id.tmm.anewreader.R;
import au.id.tmm.anewreader.model.Account;
import au.id.tmm.anewreader.model.PreviousAccountInfoFile;
import au.id.tmm.anewreader.model.ReaderService;

/**
 * Activity for allowing the user to authenticate to a ReaderService.
 */
public class AuthenticateActivity extends Activity implements View.OnClickListener {

    protected static final String RETURNED_ACCOUNT_CODE = "RETURNED_ACCOUNT_CODE";
    protected static final String PREVIOUS_ACCOUNT_INFO_FILE_CODE = "PREVIOUS_ACCOUNT_INFO";

    private static final String THE_OLD_READER_BASE_URL = "https://theoldreader.com";
    private static final String THE_OLD_READER_TITLE = "The Old Reader";

    private static final String INOREADER_BASE_URL = "https://www.inoreader.com/";
    private static final String INOREADER_TITLE = "Inoreader";

    private static final ReaderService[] predefinedReaderServices = {
            new ReaderService(THE_OLD_READER_BASE_URL, THE_OLD_READER_TITLE),
            new ReaderService(INOREADER_BASE_URL, INOREADER_TITLE)
    };

    private Spinner predefinedReaderServicesSpinner;

    private PreviousAccountInfoFile previousAccountInfoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_authenticate_activity);

        ReaderServiceAdapter readerServiceAdapter = new ReaderServiceAdapter(this, android.R.layout.simple_list_item_1);

        predefinedReaderServicesSpinner = (Spinner) this.findViewById(R.id.predefined_reader_service_spinner);
        predefinedReaderServicesSpinner.setAdapter(readerServiceAdapter);

        readerServiceAdapter.addAll(predefinedReaderServices);
        readerServiceAdapter.notifyDataSetChanged();

        this.findViewById(R.id.button_login).setOnClickListener(this);

        previousAccountInfoFile = (PreviousAccountInfoFile)
                ActivityEscrow.getInstance().getAndDeallocate(PREVIOUS_ACCOUNT_INFO_FILE_CODE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:

                ReaderService readerService = (ReaderService) this.predefinedReaderServicesSpinner.getSelectedItem();
                String username = ((TextView) this.findViewById(R.id.authenticate_username_editText)).getText().toString();
                String password = ((TextView) this.findViewById(R.id.authenticate_password_editText)).getText().toString();

                new AttemptAuthenticateTask(readerService, username, password).execute();
        }
    }

    /**
     * Task for attempting to authenticate to a ReaderService.
     */
    private class AttemptAuthenticateTask extends AsyncTask<Void, Void, Account> {

        private ReaderService readerService;
        private String username;
        private String password;

        private Throwable failCause;

        public AttemptAuthenticateTask(ReaderService readerService, String username, String password) {
            this.readerService = readerService;
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.authentication_progress_layout).setVisibility(View.VISIBLE);
        }

        @Override
        protected Account doInBackground(Void... voids) {
            try {
                Account account = new Account(username, readerService);
                account.authenticate(password);
                return account;
            } catch (Throwable e) {
                this.failCause = e;
                this.cancel(false);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Account account) {
            findViewById(R.id.authentication_progress_layout).setVisibility(View.GONE);
            onSuccessfulAuthenticate(account);
        }

        @Override
        protected void onCancelled() {
            findViewById(R.id.authentication_progress_layout).setVisibility(View.GONE);
            onFailedAuthenticate(failCause);
        }
    }


    private void onSuccessfulAuthenticate(Account authenticatedAccount) {

        new AsyncTask<Account, Void, Void>() {
            @Override
            protected Void doInBackground(Account... accounts) {
                try {
                    previousAccountInfoFile.storeAccount(accounts[0]);
                } catch (IOException e) {
                    // We have failed to write to a local file, which shouldn't happen unless
                    // something more serious is wrong.
                    throw new RuntimeException(e);
                }
                return null;
            }
        }.execute(authenticatedAccount);

        Intent returnedIntent = new Intent();
        ActivityEscrow.getInstance().put(RETURNED_ACCOUNT_CODE, authenticatedAccount);
        setResult(Activity.RESULT_OK, returnedIntent);
        this.finish();

    }

    private void onFailedAuthenticate(Throwable cause) {
        Toast.makeText(this,
                this.getString(R.string.authentication_failed) + ": " + cause.getLocalizedMessage(),
                Toast.LENGTH_SHORT).show();
    }


    private class ReaderServiceAdapter extends ArrayAdapter<ReaderService> {

        public ReaderServiceAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ReaderService currentReaderService = super.getItem(position);

            LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);

            View readerServiceSpinnerView = inflater.inflate(R.layout.adapter_view_readerservice_spinner, parent, false);

            TextView readerServiceTitleTextView = (TextView) readerServiceSpinnerView.findViewById(R.id.readerservice_spinner_title);
            readerServiceTitleTextView.setText(currentReaderService.getTitle());

            TextView readerServiceUrlTextView = (TextView) readerServiceSpinnerView.findViewById(R.id.readerservice_spinner_base_url);
            readerServiceUrlTextView.setText(currentReaderService.getBaseUrl());

            return readerServiceSpinnerView;

        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return this.getView(position, convertView, parent);
        }
    }
}
