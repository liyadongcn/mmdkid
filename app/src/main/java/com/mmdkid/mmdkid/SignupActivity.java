package com.mmdkid.mmdkid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mmdkid.mmdkid.models.Token;
import com.mmdkid.mmdkid.models.User;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
public class SignupActivity extends AppCompatActivity  implements RESTAPIConnection.OnConnectionListener{

    private static final String TAG = "SignupActivity";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordRepeatView;
    private EditText mUsernameView;
    private View mProgressView;
    private View mLoginFormView;

    // Status
    private boolean mIsRegistering = false;
    private boolean mTokenValid = false;
    private boolean mIsGettingToken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set up the sign up form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        mPasswordRepeatView = (EditText) findViewById(R.id.password_repeat);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        mLoginFormView = findViewById(R.id.signup_form);
        mProgressView = findViewById(R.id.signup_progress);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {
        if (mIsRegistering ) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPasswordRepeatView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password_repeat = mPasswordRepeatView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password_repeat) && !isPasswordValid(password_repeat)) {
            mPasswordRepeatView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordRepeatView;
            cancel = true;
        }
        // Check the password and the repeat password equals.
        if(!password.equals(password_repeat)){
            mPasswordRepeatView.setError(getString(R.string.error_not_equal_password));
            focusView = mPasswordRepeatView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user sign up attempt.
            showProgress(true);

            RESTAPIConnection connection = new RESTAPIConnection(this);
            User.signup(connection,username,email,password,password_repeat,User.ROLE_PARENT);

        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 3;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public void onErrorRespose(Class c, String error) {

        if(c == User.class){
            mIsRegistering = false;
            Log.d(TAG,"Register a new user error " + error);
        }

        if(c == Token.class){
            mIsGettingToken =false;
            Log.d(TAG,"Get the user token error." + error);
        }
        showProgress(false);
    }

    @Override
    public void onResponse(Class c, ArrayList responseDataList) {
        App app = (App)getApplicationContext();
        if(c == User.class){
            mIsRegistering = false;
            if(responseDataList.isEmpty()){
                Log.d(TAG,"Register a new user with no user info.");
            }else{
                User user = (User) responseDataList.get(0);
                Log.d(TAG,"Register a new user success.");
                Log.d(TAG,"User avatar is " + user.mAvatar);
                app.setCurrentUser(user);
                Log.d(TAG,"Try to get the user token.");
                attemptGetToken(mUsernameView.getText().toString(),mPasswordView.getText().toString());
            }
        }
        if(c==Token.class){
            mIsGettingToken =false;
            Token token = (Token) responseDataList.get(0);
            Log.d(TAG,"The access token is :" + token.mAccessToken);
            mTokenValid = true;
            token.saveToLocal(this);
        }
        if(!mIsGettingToken && !mIsRegistering && mTokenValid){
            app.setIsGuest(false);
            showProgress(false);
            finish();
        }

    }

    private void attemptGetToken(String identity, String password) {
        if(mIsGettingToken) return;
        mIsGettingToken = true;
        RESTAPIConnection connection = new RESTAPIConnection(this);
        Token.find(connection).where("username",identity).where("password",password).all();
    }
}

