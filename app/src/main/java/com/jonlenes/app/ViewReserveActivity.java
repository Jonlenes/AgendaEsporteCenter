package com.jonlenes.app;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jonlenes.app.Modelo.AgendaBo;
import com.jonlenes.app.Modelo.Client;
import com.jonlenes.app.Modelo.ClientDao;
import com.jonlenes.app.Modelo.ScheduledTime;

public class ViewReserveActivity extends AppCompatActivity {

    private Long idReserve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reserve);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        idReserve = getIntent().getLongExtra("idReserve", -1);
        if (idReserve == -1) {
            Toast.makeText(this, "Não foi possível buscar essa reserva.", Toast.LENGTH_LONG).show();
            finish();
        }

        new SearchReserveAsyncTask().execute();

    }


    private class SearchReserveAsyncTask extends AsyncTask<Void, Void, ScheduledTime> {
        private ProgressDialog progressDialog;
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
        protected ScheduledTime doInBackground(Void... params) {
            try {

                return new AgendaBo().getReserva(idReserve);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(ScheduledTime scheduledTime) {
            super.onPostExecute(scheduledTime);

            progressDialog.dismiss();
            if (exception != null)
                TreatException.treat(ViewReserveActivity.this, exception);
            else {

                ((ImageView) ViewReserveActivity.this.findViewById(R.id.ivClientReserve)).setImageBitmap(
                        Util.getBitmap(ViewReserveActivity.this, scheduledTime.getClient().getImage()));
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvClientNameReserve)).setText(scheduledTime.getClient().getName());
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvClientTelephoneReserve)).setText(scheduledTime.getClient().getTelephone());
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvLocalReserve)).setText(scheduledTime.getLocal().getDescription());
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvDateReserve)).setText(scheduledTime.getDateDay().toString());
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvStartTimeReserve)).setText(Util.formatTime(scheduledTime.getStartTime()));
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvEndTimeReserve)).setText(Util.formatTime(scheduledTime.getEndTime()));
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvDurationReserve)).setText(scheduledTime.getDuration().toString() + "min");
                ((TextView) ViewReserveActivity.this.findViewById(R.id.tvNameUserReserve)).setText(scheduledTime.getUser().getName());


            }

        }
    }

}
