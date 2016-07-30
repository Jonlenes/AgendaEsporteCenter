package com.jonlenes.app.View;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jonlenes.app.Modelo.User;
import com.jonlenes.app.Modelo.UserBo;
import com.jonlenes.app.R;
import com.jonlenes.app.TreatException;

public class CadastroUserActivity extends AppCompatActivity {

    private EditText edtNameUser;
    private EditText edtPasswordUser;
    private EditText edtPasswordRepeatUser;

    private boolean isNewUser = true;
    private Long id = -1l;
    private String passwordActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_user);

        edtNameUser = (EditText) findViewById(R.id.edtNameUser);
        edtPasswordUser = (EditText) findViewById(R.id.edtPasswordUser);
        edtPasswordRepeatUser = (EditText) findViewById(R.id.edtRepeatPasswordUser);
        Button btnSaveUser = (Button) findViewById(R.id.btnSaveUser);

        btnSaveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validFields()) {

                    if (!isNewUser) {
                        final EditText editTextPassword = new EditText(CadastroUserActivity.this);
                        editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

                        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroUserActivity.this);
                        builder.setTitle("Confirme senha atual");
                        builder.setView(editTextPassword);
                        builder.setPositiveButton("Ok", null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (editTextPassword.getText().toString().equals(passwordActual)) {
                                    new InsertUserAsyncTask().execute(new User(edtNameUser.getText().toString(), edtPasswordUser.getText().toString()));
                                } else {
                                    Toast.makeText(CadastroUserActivity.this, "Senha não confere.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        builder.create().show();

                    } else {

                        new InsertUserAsyncTask().execute(new User(edtNameUser.getText().toString(), edtPasswordUser.getText().toString()));

                    }
                }
            }
        });

        if (getIntent().getBooleanExtra("blnUpdateMe", false)) {
            isNewUser = false;
            new SearchUserAsyncTask().execute();
        }

        if (isNewUser) {
            getSupportActionBar().setTitle("Cadastro do usuário");
        } else {
            getSupportActionBar().setTitle("Meus dados");
        }

    }

    private boolean validFields() {

        if (edtNameUser.getText().toString().isEmpty()) {
            edtNameUser.setError("O nome de usuário não pode ser vazio.");
            return false;
        }

        if (isNewUser && edtPasswordUser.getText().toString().isEmpty()) {
            edtPasswordUser.setError("A senha não pode ser vazia.");
            return false;
        }

        if (isNewUser && edtPasswordRepeatUser.getText().toString().isEmpty()) {
            edtPasswordRepeatUser.setError("A senha não pode ser vazia.");
            return false;
        }

        if ((!edtPasswordUser.getText().toString().isEmpty() || !edtPasswordRepeatUser.getText().toString().isEmpty()) &&
                !edtPasswordUser.getText().toString().equals(edtPasswordRepeatUser.getText().toString())) {
            Toast.makeText(this, "As senhas não correspondem.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!edtPasswordUser.getText().toString().isEmpty() && edtPasswordUser.getText().toString().length() < 4) {
            Toast.makeText(CadastroUserActivity.this, "A senha deve ter no mínimo 4 caracteres.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private class SearchUserAsyncTask extends AsyncTask<Void, Void, User> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public SearchUserAsyncTask() {
            progressDialog = new ProgressDialog(CadastroUserActivity.this);
            progressDialog.setMessage("Buscando seus dados...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected User doInBackground(Void... params) {
           try {

                return new UserBo().getUserActive();

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            progressDialog.dismiss();

            if (exception != null) {
                TreatException.treat(CadastroUserActivity.this, exception);
            }
            else {
                id = user.getId();
                passwordActual = user.getPassword();
                edtNameUser.setText(user.getName());
            }
        }
    }

    private class InsertUserAsyncTask extends AsyncTask<User, Void, String> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public InsertUserAsyncTask() {
            progressDialog = new ProgressDialog(CadastroUserActivity.this);
            if (isNewUser)
                progressDialog.setMessage("Inserindo novo usuário...");
            else
                progressDialog.setMessage("Atualizando seus dados...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected String doInBackground(User... params) {
            try {
                if (isNewUser)
                    new UserBo().insert(params[0]);
                else {

                    if (params[0].getPassword().isEmpty())
                        params[0].setPassword(passwordActual);

                    params[0].setId(id);
                    new UserBo().update(params[0]);
                }

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();

            if (exception != null)
                TreatException.treat(CadastroUserActivity.this, exception);
            else {
                startActivity(new Intent(CadastroUserActivity.this, MyReservasActivity.class));
                CadastroUserActivity.this.finish();
            }
        }
    }

}
