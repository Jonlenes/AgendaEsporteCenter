package com.jonlenes.app.View;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jonlenes.app.DialogNewReserve;
import com.jonlenes.app.Modelo.AgendaBo;
import com.jonlenes.app.Modelo.Reserve;
import com.jonlenes.app.Modelo.ReserveDao;
import com.jonlenes.app.R;
import com.jonlenes.app.TreatException;
import com.jonlenes.app.Util;

public class ViewReserveActivity extends AppCompatActivity {

    private Long idReserve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reserve);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Detalhes da reserva");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ViewReserveActivity.this);
                builder
                        .setTitle("Opções")
                        .setItems(R.array.dialog_options_reserve, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        updateReserve();
                                        break;

                                    case 1:
                                        deleteReserve();
                                        break;
                                }
                            }
                        })
                        .create().show();

            }
        });


        idReserve = getIntent().getLongExtra("idReserve", -1);
        if (idReserve == -1) {
            Toast.makeText(this, "Não foi possível buscar essa reserva.", Toast.LENGTH_LONG).show();
            finish();
        }

        new SearchReserveAsyncTask().execute();
    }

    private void deleteReserve() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewReserveActivity.this);
        builder.setTitle("Confirmação");
        builder.setMessage("Deseja realmente excluir?");
        builder.setNegativeButton("Não", null);
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteReserveAsyncTask().execute();
            }
        });
        builder.create().show();
    }

    private void updateReserve() {
        DialogNewReserve dialogNewReserve = new DialogNewReserve();
        Bundle bundle = new Bundle();
        bundle.putLong("idReserve", idReserve);
        dialogNewReserve.setArguments(bundle);
        //dialogNewReserve.setTargetFragment(this, UPDATE_FINALISED);
/*        dialogNewReserve.alertDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                new SearchReserveAsyncTask().execute();
            }
        });*/
        dialogNewReserve.show(getSupportFragmentManager(), "dialog");
    }

    public void refreshActivity() {
        new SearchReserveAsyncTask().execute();
    }


    private class SearchReserveAsyncTask extends AsyncTask<Void, Void, Reserve> {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public SearchReserveAsyncTask() {
            progressDialog = new ProgressDialog(ViewReserveActivity.this);
            progressDialog.setMessage("Buscando...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected Reserve doInBackground(Void... params) {
            try {

                return new AgendaBo().getReserva(idReserve);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Reserve Reserve) {
            super.onPostExecute(Reserve);

            progressDialog.dismiss();
            if (exception != null)
                TreatException.treat(ViewReserveActivity.this, exception);
            else {

                ((ImageView) ViewReserveActivity.this.findViewById(R.id.ivClientReserve)).setImageBitmap(
                        Util.getBitmap(ViewReserveActivity.this, Reserve.getClient().getImage()));
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvClientNameReserve)).setText(Reserve.getClient().getName());
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvClientTelephoneReserve)).setText(Reserve.getClient().getTelephone());
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvLocalReserve)).setText(Reserve.getLocal().getDescription());
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvDateReserve)).setText(Util.formatDate(Reserve.getDateDay()));
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvStartTimeReserve)).setText(Util.formatTime(Reserve.getStartTime()));
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvEndTimeReserve)).setText(Util.formatTime(Reserve.getEndTime()));
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvDurationReserve)).setText(Reserve.getDuration().toString() + "min");

            }

        }
    }

    private class DeleteReserveAsyncTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public DeleteReserveAsyncTask() {
            progressDialog = new ProgressDialog(ViewReserveActivity.this);
            progressDialog.setMessage("Deletando...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(Void... params) {
            try {

                new ReserveDao().delete(idReserve);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();
            if (exception != null)
                TreatException.treat(ViewReserveActivity.this, exception);
            else
                finish();
        }
    }

}
