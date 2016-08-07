package com.jonlenes.app.View;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jonlenes.app.Modelo.Client;
import com.jonlenes.app.Modelo.ClientBo;
import com.jonlenes.app.Modelo.ClientDao;
import com.jonlenes.app.R;
import com.jonlenes.app.TreatException;
import com.jonlenes.app.Util;

import java.util.List;

public class ClientsActivity extends AppCompatActivity {

    private final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
            new DialogFragment() {

                private View view;

                @NonNull
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    view = inflater.inflate(R.layout.dialog_view_client, null);

                    ImageView imageView = (ImageView) view.findViewById(R.id.ivClientImage);

                    byte[] bytes = ((Client) parent.getItemAtPosition(position)).getImage();
                    imageView.setImageBitmap(Util.getBitmap(ClientsActivity.this, bytes));
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setView(view);

                    return builder.create();
                }

            }.show(ClientsActivity.this.getSupportFragmentManager(), "dialog");
        }
    };
    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

            AlertDialog.Builder builder = new AlertDialog.Builder(ClientsActivity.this);
            builder.setTitle("Opções");
            builder.setItems(R.array.dialog_options_clients, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0: //Editar
                            updateClient((Client) parent.getItemAtPosition(position));
                            break;

                        case 1: //Deletar
                            deleteClient((Client) parent.getItemAtPosition(position));
                            break;
                    }
                }
            });
            builder.show();

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clints);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ClientsActivity.this, CadastroClientActivity.class));
            }
        });

        ListView listView = (ListView) findViewById(R.id.lvClients);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);

        new SearchClientsAsyncTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new SearchClientsAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.clients_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_clients: {
                new SearchClientsAsyncTask().execute();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteClient(final Client client) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClientsActivity.this);
        builder.setTitle("Confirmação");
        builder.setMessage("Deseja realmente excluir?");
        builder.setNegativeButton("Não", null);
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteClientsAsyncTask().execute(client.getId());
            }
        });
        builder.create().show();
    }

    private void updateClient(Client client) {
        Intent intent = new Intent(ClientsActivity.this, CadastroClientActivity.class);
        intent.putExtra("idClient", client.getId());
        startActivity(intent);
    }


    private class SearchClientsAsyncTask extends AsyncTask<Void, Void, List<Client> > {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public SearchClientsAsyncTask() {
            progressDialog = new ProgressDialog(ClientsActivity.this);
            progressDialog.setMessage("Buscando clientes...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ClientsActivity.this.findViewById(R.id.tvEmpty).setVisibility(View.INVISIBLE);
            progressDialog.show();
        }


        @Override
        protected List<Client> doInBackground(Void... params) {
            try {

                return new ClientDao().getAll();

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Client> list) {
            super.onPostExecute(list);

            progressDialog.dismiss();

            if (exception == null) {
                if (list.isEmpty())
                    ClientsActivity.this.findViewById(R.id.tvEmpty).setVisibility(View.INVISIBLE);
                else
                    ((ListView) ClientsActivity.this.findViewById(R.id.lvClients)).setAdapter(new AdapterListClient(list));
            } else
                TreatException.treat(ClientsActivity.this, exception);
        }
    }

    private class DeleteClientsAsyncTask extends AsyncTask<Long, Void, Void > {
        private ProgressDialog progressDialog;
        private Exception exception;

        public DeleteClientsAsyncTask() {
            progressDialog = new ProgressDialog(ClientsActivity.this);
            progressDialog.setMessage("Deletando...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(Long... params) {
            try {

                new ClientBo().delete(params[0]);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();

            if (exception == null)
                new SearchClientsAsyncTask().execute();
            else
                TreatException.treat(ClientsActivity.this, exception);
        }
    }


    class AdapterListClient extends BaseAdapter {
        private final List<Client> list;

        public AdapterListClient(List<Client> list) {
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
                convertView = ClientsActivity.this.getLayoutInflater().inflate(R.layout.row_listview_clients, null);
                viewHolder = new ViewHolder();

                viewHolder.civImageClientRow = (ImageView) convertView.findViewById(R.id.civImageClientRow);
                viewHolder.tvNomeClientRow = (TextView) convertView.findViewById(R.id.tvNomeClientRow);
                viewHolder.tvTelephoneClientRow = (TextView) convertView.findViewById(R.id.tvTelephoneClientRow);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Client client = list.get(position);

            viewHolder.civImageClientRow.setImageBitmap(Util.getBitmap(ClientsActivity.this, client.getImage()));
            viewHolder.tvNomeClientRow.setText(client.getName());
            viewHolder.tvTelephoneClientRow.setText(client.getTelephone());

            return convertView;
        }

        private class ViewHolder {
            ImageView civImageClientRow;
            TextView tvNomeClientRow;
            TextView tvTelephoneClientRow;
        }
    }


}
