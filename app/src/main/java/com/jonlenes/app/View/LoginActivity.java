package com.jonlenes.app.View;

import android.app.ProgressDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jonlenes.app.Modelo.UserBo;
import com.jonlenes.app.MyReservasActivity;
import com.jonlenes.app.R;
import com.jonlenes.app.TreatException;


public class LoginActivity extends AppCompatActivity {

    final private Integer REQUEST_NEW_USER_CADASTRADO = 1;

    private EditText edtNameUser;
    private EditText edtPasswordUser;
    private CheckBox chkConnectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtNameUser = (EditText) findViewById(R.id.edtNameUser);
        edtPasswordUser = (EditText) findViewById(R.id.edtPasswordUser);
        chkConnectedUser = (CheckBox) findViewById(R.id.chkConnectedUser);

        Button btnLoginUser = (Button) findViewById(R.id.btnLoginUser);
        Button btnRegisterUser = (Button) findViewById(R.id.btnRegisterUser);

        btnLoginUser.setOnClickListener(clickLogin);
        btnRegisterUser.setOnClickListener(clickRegister);

        if (getIntent().getBooleanExtra("blnClearPreference", false)) {
            SharedPreferences.Editor editor = LoginActivity.this.getPreferences(0).edit();
            editor.remove("IdUserActivity");
            editor.apply();
            UserBo.setIdUserActive(-1l);
        } else {
            Long idUserActivity = LoginActivity.this.getPreferences(0).getLong("IdUserActivity", -1);

            if (idUserActivity != -1) {
                UserBo.setIdUserActive(idUserActivity);
                startActivity(new Intent(LoginActivity.this, MyReservasActivity.class));
                finish();
            }
        }
    }

    private OnClickListener clickLogin = new OnClickListener() {
        @Override
        public void onClick(View v) {
            new LoginAsyncTask().execute(edtNameUser.getText().toString(), edtPasswordUser.getText().toString());
        }
    };

    private OnClickListener clickRegister = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(LoginActivity.this, CadastroUserActivity.class), REQUEST_NEW_USER_CADASTRADO);
        }
    };


    public class LoginAsyncTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog progressDialog;
        private Exception exception;

        public LoginAsyncTask() {
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("Entrando...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(String... params) {
            try {

                new UserBo().login(params[0], params[1]);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();

            if (exception != null) {

                TreatException.treat(LoginActivity.this, exception);

            } else {

                if (chkConnectedUser.isChecked()) {
                    SharedPreferences.Editor editor = LoginActivity.this.getPreferences(0).edit();
                    editor.putLong("IdUserActivity", UserBo.getIdUserActive());
                    editor.apply();
                }

                startActivity(new Intent(LoginActivity.this, MyReservasActivity.class));
                finish();
            }

        }
    }
}

