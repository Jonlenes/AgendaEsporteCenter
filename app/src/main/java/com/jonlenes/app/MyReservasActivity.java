package com.jonlenes.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jonlenes.app.Modelo.ScheduledTime;
import com.jonlenes.app.Modelo.ScheduledTimeBo;
import com.jonlenes.app.View.CadastroUserActivity;
import com.jonlenes.app.View.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.List;

public class MyReservasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Minhas reservas");

        ((ListView) findViewById(R.id.listViewTimesReserved)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyReservasActivity.this, ViewReserveActivity.class);
                intent.putExtra("idReserve", ((ScheduledTime) parent.getItemAtPosition(position)).getId());
                startActivity(intent);
            }
        });
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
        switch (item.getItemId())
        {
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
        }

        return super.onOptionsItemSelected(item);
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_USER) {
            if (ClientDao.getIdUserActive() != -1) {
                SharedPreferences.Editor editor = MyReservasActivity.this.getPreferences(0).edit();
                editor.putLong("IdUserActivity", ClientDao.getIdUserActive());
                editor.apply();
            } else {
                finish();
            }
        }
    }*/


    private class SearchTimesReservedAsyncTask extends AsyncTask<Void, Void, List<ScheduledTime>> {
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
        }

        @Override
        protected List<ScheduledTime> doInBackground(Void... params) {
            try {

                return new ScheduledTimeBo().getScheduledTimeByUser();

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<ScheduledTime> list) {
            super.onPostExecute(list);

            progressDialog.dismiss();

            if (exception == null)
                ((ListView) findViewById(R.id.listViewTimesReserved)).setAdapter(new AdapterListTimesReserved(list));
            else
                TreatException.treat(MyReservasActivity.this, exception);
        }
    }

    private class AdapterListTimesReserved extends BaseAdapter {
        private List<ScheduledTime> list;

        public AdapterListTimesReserved(List<ScheduledTime> list) {
            super();
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = MyReservasActivity.this.getLayoutInflater().inflate(R.layout.row_listview_times_reserved, null);
                viewHolder = new ViewHolder();

                viewHolder.tvLocalRowList = (TextView) convertView.findViewById(R.id.tvLocalRowList);;
                viewHolder.tvDateRowList = (TextView) convertView.findViewById(R.id.tvDateRowList);;
                viewHolder.tvStartTimeRowList = (TextView) convertView.findViewById(R.id.tvStartTimeRowList);;
                viewHolder.tvEndTimeRowList = (TextView) convertView.findViewById(R.id.tvEndTimeRowList);;
                viewHolder.tvDurationRowList = (TextView) convertView.findViewById(R.id.tvDurationRowList);;

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ScheduledTime scheduledTime = list.get(position);

            viewHolder.tvLocalRowList.setText(scheduledTime.getLocal().getDescription());
            viewHolder.tvDateRowList.setText(new SimpleDateFormat("dd/MM/yyyy").format(scheduledTime.getDateDay()));
            viewHolder.tvStartTimeRowList.setText(scheduledTime.getStartTime().toString());
            viewHolder.tvEndTimeRowList.setText(scheduledTime.getEndTime().toString());
            viewHolder.tvDurationRowList.setText(String.valueOf(scheduledTime.getDuration()) + "min");

            return convertView;
        }

        private class ViewHolder {
            TextView tvLocalRowList;
            TextView tvDateRowList;
            TextView tvStartTimeRowList;
            TextView tvEndTimeRowList;
            TextView tvDurationRowList;
        }
    }
}
