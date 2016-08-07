package com.jonlenes.app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jonlenes.app.Modelo.AgendaBo;
import com.jonlenes.app.Modelo.Local;
import com.jonlenes.app.Modelo.LocalDao;
import com.jonlenes.app.Modelo.Reserve;
import com.jonlenes.app.View.ExpandableListAdapter;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AgendaActivity extends AppCompatActivity {

    private boolean[] checkedLocals;
    private ExpandableListView listViewTimes;

    //Filter
    private List<Local> locals;
    private Date dateFilterStart;
    private Date dateFilterEnd;
    private boolean isReserved;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_agenda);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Agenda");

        FloatingActionButton fabNewReserve = (FloatingActionButton) findViewById(R.id.fab);
        listViewTimes = (ExpandableListView) findViewById(R.id.expandableListView);
        findViewById(R.id.btnFiltrar).setOnClickListener(clickFiltrar);

        if (fabNewReserve != null) {
            fabNewReserve.setOnClickListener(clickNewReserve);
        }

        listViewTimes.setOnChildClickListener(childClickListenerTimes);
        /*if (listViewTimes != null) {
            listViewTimes.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY > oldScrollY)
                        fabNewReserve.hide();
                    else
                        fabNewReserve.show();
                }
            });
        }*/

        //Filtros
        isReserved = true;
        dateFilterStart = new Date(new java.util.Date().getTime());
        dateFilterEnd = new Date(new java.util.Date().getTime());
        new SearchLocalsAsyncTask().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();

        new SearchLocalsAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.agenda_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_agenda: {
                refreshActivity();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshActivity() {
        new SearchLocalsAsyncTask().execute();
    }

    private final View.OnClickListener clickNewReserve = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogNewReserve dialogNewReserve = new DialogNewReserve();
            dialogNewReserve.show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
        }
    };
    private ExpandableListView.OnChildClickListener childClickListenerTimes = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            Reserve Reserve = (Reserve) listViewTimes.getExpandableListAdapter().getChild(groupPosition, childPosition);

            if (isReserved) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AgendaActivity.this);
                builder.setTitle("Reserva");
                builder.setMessage("Agendado por: " + Reserve.getUser().getName() + "\n\n" +
                        "Para: " + Reserve.getClient().getName());
                builder.setPositiveButton("Ok", null);
                builder.create().show();
            }
            return false;
        }
    };
    private final View.OnClickListener clickFiltrar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new DialogFragment() {

                private AlertDialog dialog;
                private View view;

                private RadioGroup rgTipo;
                private EditText edtDateStart;
                private EditText edtDateEnd;
                private RadioGroup rgPeriodo;


                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    view = inflater.inflate(R.layout.dialog_filter_agenda, null);

                    rgTipo = ((RadioGroup) view.findViewById(R.id.rgTipo));
                    edtDateStart = (EditText) view.findViewById(R.id.edtDateStart);
                    edtDateEnd = (EditText) view.findViewById(R.id.edtDateEnd);
                    rgPeriodo = (RadioGroup) view.findViewById(R.id.rgPeriodo);

                    rgTipo.setOnCheckedChangeListener(checkedChangeListenerTipo);
                    rgPeriodo.setOnCheckedChangeListener(checkedChangeListenerPeriodos);
                    edtDateStart.setOnClickListener(clickDateStart);
                    edtDateEnd.setOnClickListener(clickDateEnd);
                    view.findViewById(R.id.btnSelectLocal).setOnClickListener(clickSelectLocais);

                    preecheDialog();

                    builder.setView(view)
                            .setTitle("Parâmetros")
                            .setPositiveButton("Ok", null)
                            .setNegativeButton("Cancelar", null);

                    dialog = builder.create();
                    dialog.show();

                    dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (rgTipo.getCheckedRadioButtonId() == R.id.rbLivre &&
                                    (edtDateStart.getText().toString().isEmpty() || edtDateEnd.getText().toString().isEmpty())) {
                                Toast.makeText(AgendaActivity.this, "Para buscar os horários livres as datas devem esta preenchidas.", Toast.LENGTH_LONG).show();
                            } else {
                                new SearchTimeAsyncTask().execute();
                                dismiss();
                            }
                        }
                    });

                    return dialog;
                }

                private void preecheDialog() {
                    edtDateStart.setText(Util.formatDate(dateFilterStart));
                    edtDateEnd.setText(Util.formatDate(dateFilterEnd));
                    rgTipo.check(isReserved ? R.id.rbReserved : R.id.rbLivre);
                }

                private RadioGroup.OnCheckedChangeListener checkedChangeListenerTipo = new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        isReserved = (checkedId == R.id.rbReserved);
                    }
                };
                private final RadioGroup.OnCheckedChangeListener checkedChangeListenerPeriodos = new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        switch (checkedId) {
                            case R.id.rbSelecrToday: {
                                dateFilterStart = new Date(new java.util.Date().getTime());
                                dateFilterEnd = new Date(new java.util.Date().getTime());
                                break;
                            }

                            case R.id.rbSelectWeek: {
                                dateFilterStart = new Date(new java.util.Date().getTime());
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(dateFilterStart);
                                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                                dateFilterEnd = new Date(calendar.getTime().getTime());
                                break;
                            }

                            case R.id.rbSelectMonth: {
                                dateFilterStart = new Date(new java.util.Date().getTime());
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(dateFilterStart);
                                calendar.add(Calendar.MONTH, 1);
                                dateFilterEnd = new Date(calendar.getTime().getTime());
                                break;
                            }
                        }

                        preecheDialog();
                    }
                };
                private View.OnClickListener clickSelectLocais = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DialogFragment() {
                            final boolean[] checkedItensOriginal = new boolean[checkedLocals.length];

                            @NonNull
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                super.onCreate(savedInstanceState);

                                CharSequence[] sequences = new CharSequence[locals.size()];
                                for (int i = 0; i < locals.size(); ++i) {
                                    sequences[i] = locals.get(i).getDescription();
                                    checkedItensOriginal[i] = checkedLocals[i];
                                }

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                builder.setTitle("Selecione os locais")
                                        .setMultiChoiceItems(sequences, checkedLocals,
                                                new DialogInterface.OnMultiChoiceClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which,
                                                                        boolean isChecked) {

                                                    }
                                                })
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                System.arraycopy(checkedItensOriginal, 0, checkedLocals, 0, checkedItensOriginal.length);
                                                dialog.dismiss();
                                            }
                                        });

                                return builder.create();
                            }
                        }.show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
                    }
                };
                private View.OnClickListener clickDateStart = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        class DatePickerFragment extends DialogFragment
                                implements DatePickerDialog.OnDateSetListener {

                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {

                                final Calendar c = Calendar.getInstance();
                                if (edtDateStart.getText().toString().isEmpty())
                                    c.setTime(new java.util.Date());
                                else {
                                    try {
                                        c.setTime(Util.parseDate(edtDateStart.getText().toString()));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                int year = c.get(Calendar.YEAR);
                                int month = c.get(Calendar.MONTH);
                                int day = c.get(Calendar.DAY_OF_MONTH);

                                return new DatePickerDialog(getActivity(), this, year, month, day);
                            }

                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month, day);
                                dateFilterStart = new Date(calendar.getTime().getTime());

                                edtDateStart.setText(Util.formatDate(dateFilterStart));
                                rgPeriodo.check(-1);

                            }
                        }

                        new DatePickerFragment().show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
                    }
                };
                private final View.OnClickListener clickDateEnd = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        class DatePickerFragment extends DialogFragment
                                implements DatePickerDialog.OnDateSetListener {

                            @NonNull
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {

                                final Calendar c = Calendar.getInstance();
                                if (edtDateEnd.getText().toString().isEmpty())
                                    c.setTime(new java.util.Date());
                                else {
                                    try {
                                        c.setTime(Util.parseDate(edtDateEnd.getText().toString()));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                int year = c.get(Calendar.YEAR);
                                int month = c.get(Calendar.MONTH);
                                int day = c.get(Calendar.DAY_OF_MONTH);

                                return new DatePickerDialog(getActivity(), this, year, month, day);
                            }

                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month, day);
                                dateFilterEnd = new Date(calendar.getTime().getTime());

                                edtDateEnd.setText(Util.formatDate(dateFilterEnd));
                                rgPeriodo.check(-1);
                            }
                        }

                        new DatePickerFragment().show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
                    }
                };

            }.show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
        }

    };


    private class SearchTimeAsyncTask extends AsyncTask<Void, Void, Map<Local, List<Reserve>>> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public SearchTimeAsyncTask() {
            progressDialog = new ProgressDialog(AgendaActivity.this);
            progressDialog.setMessage("Buscando agenda...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AgendaActivity.this.findViewById(R.id.tvEmpty).setVisibility(View.INVISIBLE);
            progressDialog.show();
        }

        @Override
        protected Map<Local, List<Reserve>> doInBackground(Void... params) {
            try {
                List<Local> locals = new ArrayList<>();
                for (int i = 0; i < checkedLocals.length; ++i)
                    if (checkedLocals[i])
                        locals.add(AgendaActivity.this.locals.get(i));

                return new AgendaBo().getHorarios(dateFilterStart, dateFilterEnd, locals, isReserved);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Map<Local, List<Reserve>> map) {
            super.onPostExecute(map);

            progressDialog.dismiss();

            if (exception == null) {

                if (map.isEmpty()) {
                    AgendaActivity.this.findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
                } else {

                    ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
                    ExpandableListAdapter adapter = new ExpandableListAdapter(AgendaActivity.this, new ArrayList<>(map.keySet()), map);

                    expandableListView.setAdapter(adapter);
                }
            } else {
                TreatException.treat(AgendaActivity.this, exception);
            }
        }
    }

    private class SearchLocalsAsyncTask extends AsyncTask<Void, Void, List<Local>> {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public SearchLocalsAsyncTask() {
            progressDialog = new ProgressDialog(AgendaActivity.this);
            progressDialog.setMessage("Buscando...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected List<Local> doInBackground(Void... params) {
            try {

                return new LocalDao().getAll();

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Local> locals) {
            super.onPostExecute(locals);

            progressDialog.dismiss();

            if (exception == null) {

                AgendaActivity.this.locals = new ArrayList<>(locals);
                AgendaActivity.this.checkedLocals = new boolean[locals.size()];

                for (int i = 0; i < checkedLocals.length; ++i)
                    checkedLocals[i] = true;

                new SearchTimeAsyncTask().execute();

            } else {
                TreatException.treat(AgendaActivity.this, exception);
            }
        }
    }
}