package com.jonlenes.app.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.jonlenes.app.AgendaActivity;
import com.jonlenes.app.Modelo.Local;
import com.jonlenes.app.Modelo.ScheduledTime;
import com.jonlenes.app.Modelo.ScheduledTimeBo;
import com.jonlenes.app.R;
import com.jonlenes.app.TreatException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyReservasActivity extends AppCompatActivity {

    private ExpandableListView elvMyReserves;
    private ExpandableListView.OnChildClickListener childClickListenerTimes = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            Intent intent = new Intent(MyReservasActivity.this, ViewReserveActivity.class);
            intent.putExtra("idReserve", ((ScheduledTime) elvMyReserves.getExpandableListAdapter().getChild(groupPosition, childPosition)).getId());
            startActivity(intent);
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Minhas reservas");

        elvMyReserves = (ExpandableListView) findViewById(R.id.elvMyReserves);
        elvMyReserves.setOnChildClickListener(childClickListenerTimes);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new SearchTimesReservedAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_my_data: {
                Intent intent = new Intent(MyReservasActivity.this, CadastroUserActivity.class);
                intent.putExtra("blnUpdateMe", true);
                startActivity(intent);
                break;
            }

            case R.id.action_agenda: {
                startActivity(new Intent(MyReservasActivity.this, AgendaActivity.class));
                break;
            }

            case R.id.action_clints: {
                startActivity(new Intent(MyReservasActivity.this, ClientsActivity.class));
                break;
            }

            case R.id.action_sair: {
                Intent intent = new Intent(MyReservasActivity.this, LoginActivity.class);
                intent.putExtra("blnClearPreference", true);
                startActivity(intent);
                finish();
                break;
            }

            case R.id.action_refresh: {
                new SearchTimesReservedAsyncTask().execute();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private class SearchTimesReservedAsyncTask extends AsyncTask<Void, Void, Map<Local, List<ScheduledTime>>> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public SearchTimesReservedAsyncTask() {
            progressDialog = new ProgressDialog(MyReservasActivity.this);
            progressDialog.setMessage("Buscando suas reservas...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            MyReservasActivity.this.findViewById(R.id.tvEmpty).setVisibility(View.INVISIBLE);
        }

        @Override
        protected Map<Local, List<ScheduledTime>> doInBackground(Void... params) {
            try {

                return new ScheduledTimeBo().getScheduledTimeByUser();

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Map<Local, List<ScheduledTime>> map) {
            super.onPostExecute(map);

            progressDialog.dismiss();

            if (exception == null) {
                if (map.isEmpty())
                    MyReservasActivity.this.findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
                else {
                    ExpandableListAdapter adapter = new ExpandableListAdapter(MyReservasActivity.this, new ArrayList<Local>(map.keySet()), map);
                    elvMyReserves.setAdapter(adapter);
                }
            } else
                TreatException.treat(MyReservasActivity.this, exception);
        }
    }
}
