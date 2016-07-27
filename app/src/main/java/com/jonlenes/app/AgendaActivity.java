package com.jonlenes.app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;


import com.jonlenes.app.Modelo.AgendaBo;
import com.jonlenes.app.Modelo.Client;
import com.jonlenes.app.Modelo.ClientDao;
import com.jonlenes.app.Modelo.Local;
import com.jonlenes.app.Modelo.LocalDao;
import com.jonlenes.app.Modelo.ScheduledTime;

import java.util.ArrayList;
import java.util.Map;

public class AgendaActivity extends AppCompatActivity {

    private List<Local> locals;
    private boolean[] checkedLocals;
    private Date dateFilter;
    private boolean isReserved;
    private ExpandableListView listViewTimes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_agenda);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Agenda");

        FloatingActionButton fabNewReserve = (FloatingActionButton) findViewById(R.id.fab);
        Button btnSelecLocals = (Button) findViewById(R.id.btnSelectLocal);
        EditText edtDateFilter = (EditText) findViewById(R.id.edtDateFilter);
        RadioGroup rgTipo = ((RadioGroup) findViewById(R.id.rgTipo));
        listViewTimes = (ExpandableListView) findViewById(R.id.expandableListView);

        isReserved = true;

        if (btnSelecLocals != null) {
            btnSelecLocals.setOnClickListener(clickSelectLocals);
        }
        if (fabNewReserve != null) {
            fabNewReserve.setOnClickListener(clickNewReserve);
        }
        if (edtDateFilter != null) {
            edtDateFilter.setOnClickListener(clickDate);
            edtDateFilter.setText(new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
            try {
                dateFilter = Util.parseDate(edtDateFilter.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (rgTipo != null) {
            rgTipo.setOnCheckedChangeListener(checkedChangeListenerTipo);
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


        new SearchLocalsAsyncTask().execute();

    }

    private View.OnClickListener clickSelectLocals = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            new DialogFragment() {
                boolean []checkedItensOriginal = new boolean[checkedLocals.length];

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
                                    new SearchTimeAsyncTask().execute();
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    for (int i = 0; i < checkedItensOriginal.length; ++i)
                                        checkedLocals[i] = checkedItensOriginal[i];
                                    dialog.dismiss();
                                }
                            });

                    return builder.create();
                }
            }.show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
        }
    };

    private View.OnClickListener clickNewReserve = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new DialogFragment() {

                private AlertDialog dialog;
                private View view;

                private EditText edtDateReserve;
                private EditText edtHoursReserva;
                private EditText edtDurationReserve;
                private Spinner spnLocalReserve;
                private Spinner spnClientReserve;

                private boolean isRecorrenciaMensal;

                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    view = inflater.inflate(R.layout.dialog_new_reserve, null);

                    edtDateReserve = (EditText) view.findViewById(R.id.edtDateReserve);
                    edtHoursReserva = (EditText) view.findViewById(R.id.edtHoursReserva);
                    edtDurationReserve = (EditText) view.findViewById(R.id.edtDurationReserve);
                    spnLocalReserve = (Spinner) view.findViewById(R.id.spnLocalReserve);
                    spnClientReserve = (Spinner) view.findViewById(R.id.spnClient);

                    edtHoursReserva.setOnClickListener(clickTimeReserve);
                    edtDateReserve.setOnClickListener(clickDateReserve);

                    ArrayAdapter<Local> adapterLocal = new ArrayAdapter<>(AgendaActivity.this,
                            android.R.layout.simple_spinner_item, locals);
                    adapterLocal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnLocalReserve.setAdapter(adapterLocal);

                    new SearchClientsAsyncTask().execute();

                    builder.setView(view)
                            .setTitle("Nova reserva")
                            .setPositiveButton("OK", null)
                            .setNegativeButton("Cancelar", null);

                    dialog = builder.create();
                    dialog.show();

                    dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {

                                ScheduledTime scheduledTime = new ScheduledTime();

                                scheduledTime.setDateDay(Util.parseDate(edtDateReserve.getText().toString()));
                                scheduledTime.setStartTime(Util.parseTime(edtHoursReserva.getText().toString()));
                                scheduledTime.setDuration(Long.parseLong((edtDurationReserve).getText().toString()));
                                scheduledTime.setEndTime(Util.sumTime(scheduledTime.getStartTime(), scheduledTime.getDuration()));
                                scheduledTime.setLocal((Local) spnLocalReserve.getSelectedItem());
                                scheduledTime.setClient((Client) spnClientReserve.getSelectedItem());

                                isRecorrenciaMensal = ((CheckBox) view.findViewById(R.id.chkRecorrenciaMensal)).isChecked();

                                new InsertScheduledTimeAsyncTask().execute(scheduledTime);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    return dialog;

                }

                View.OnClickListener clickTimeReserve = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        class TimePickerFragment extends DialogFragment
                                implements TimePickerDialog.OnTimeSetListener {

                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {

                                final Calendar c = Calendar.getInstance();
                                int hour = c.get(Calendar.HOUR_OF_DAY);
                                int minute = c.get(Calendar.MINUTE);

                                return new TimePickerDialog(getActivity(), this, hour, minute, true);
                            }

                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(0, 0, 0, hourOfDay, minute);

                                edtHoursReserva.setText(new SimpleDateFormat("HH:mm").format(calendar.getTime()));
                            }
                        }
                        new TimePickerFragment().show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
                    }
                };

                private View.OnClickListener clickDateReserve = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        class DatePickerFragment extends DialogFragment
                                implements DatePickerDialog.OnDateSetListener {

                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                final Calendar c = Calendar.getInstance();
                                int year = c.get(Calendar.YEAR);
                                int month = c.get(Calendar.MONTH);
                                int day = c.get(Calendar.DAY_OF_MONTH);

                                return new DatePickerDialog(getActivity(), this, year, month, day);
                            }

                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month, day);

                                edtDateReserve.setText(new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime()));
                            }
                        }

                        new DatePickerFragment().show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
                    }
                };

                @Override
                public void onDismiss(DialogInterface dialog) {
                    super.onDismiss(dialog);

                    new SearchTimeAsyncTask().execute();
                }

                class SearchClientsAsyncTask extends AsyncTask<Void, Void, List<Client>> {
                    private ProgressDialog progressDialog;
                    private Exception exception;

                    public SearchClientsAsyncTask() {
                        progressDialog = new ProgressDialog(AgendaActivity.this);
                        progressDialog.setMessage("Buscando clientes...");
                        progressDialog.setCancelable(false);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.show();
                    }

                    @Override
                    protected List<Client> doInBackground(Void... params) {
                        try {

                            return new ClientDao().getAllWithoutImage();

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
                            ArrayAdapter<Client> adapterClients = new ArrayAdapter<>(AgendaActivity.this,
                                    android.R.layout.simple_spinner_item, list);
                            adapterClients.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spnClientReserve.setAdapter(adapterClients);
                        } else {
                            TreatException.treat(AgendaActivity.this, exception);
                        }

                    }
                }

                class InsertScheduledTimeAsyncTask extends AsyncTask<ScheduledTime, Void, Void> {
                    private ProgressDialog progressDialog;
                    private Exception exception;

                    public InsertScheduledTimeAsyncTask() {
                        progressDialog = new ProgressDialog(AgendaActivity.this);
                        progressDialog.setMessage("Reservando...");
                        progressDialog.setCancelable(false);
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog.show();
                    }

                    @Override
                    protected Void doInBackground(ScheduledTime... params) {
                        try {

                            new AgendaBo().marcarHorario(params[0], isRecorrenciaMensal);

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
                            TreatException.treat(AgendaActivity.this, exception);
                        else
                            dialog.dismiss();
                    }
                }

            }.show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
        }
    };

    private View.OnClickListener clickDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            class DatePickerFragment extends DialogFragment
                    implements DatePickerDialog.OnDateSetListener {

                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    // Use the current date as the default date in the picker
                    final Calendar c = Calendar.getInstance();
                    c.setTime(dateFilter);
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH);

                    // Create a new instance of DatePickerDialog and return it
                    return new DatePickerDialog(getActivity(), this, year, month, day);
                }

                public void onDateSet(DatePicker view, int year, int month, int day) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day);
                    dateFilter = new Date(calendar.getTime().getTime());

                    ((EditText) getActivity().findViewById(R.id.edtDateFilter)).setText(new SimpleDateFormat("dd/MM/yyyy").format(dateFilter));

                    new SearchTimeAsyncTask().execute();
                }
            }

            new DatePickerFragment().show(AgendaActivity.this.getSupportFragmentManager(), "dialog");
        }
    };

    private RadioGroup.OnCheckedChangeListener checkedChangeListenerTipo = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            isReserved = (checkedId == R.id.rbReserved);
            new SearchTimeAsyncTask().execute();
        }
    };

    private ExpandableListView.OnChildClickListener childClickListenerTimes = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

            ScheduledTime scheduledTime = (ScheduledTime) listViewTimes.getExpandableListAdapter().getChild(groupPosition, childPosition);

            if (isReserved) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AgendaActivity.this);
                builder.setTitle("Reserva");
                builder.setMessage("Agendado por: " + scheduledTime.getUser().getName() + "\n\n" +
                        "Para: " + scheduledTime.getClient().getName());
                builder.setPositiveButton("Ok", null);
                builder.create().show();
            }
            return false;
        }
    };

    private class SearchTimeAsyncTask extends AsyncTask<Void, Void, Map<Local, List<ScheduledTime>>> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public SearchTimeAsyncTask() {
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
        protected Map<Local, List<ScheduledTime>> doInBackground(Void... params) {
            try {
                List<Local> locals = new ArrayList<>();
                for(int i = 0; i < checkedLocals.length; ++i)
                    if (checkedLocals[i])
                        locals.add(AgendaActivity.this.locals.get(i));

                return new AgendaBo().getHorarios(dateFilter, locals, isReserved);

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

                ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
                ExpandableListAdapter adapter = new ExpandableListAdapter(AgendaActivity.this, new ArrayList<Local>(map.keySet()), map);

                expandableListView.setAdapter(adapter);

            } else {
                TreatException.treat(AgendaActivity.this, exception);
            }
        }
    }

    private class SearchLocalsAsyncTask extends AsyncTask<Void, Void, List<Local>> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public SearchLocalsAsyncTask() {
            progressDialog = new ProgressDialog(AgendaActivity.this);
            progressDialog.setMessage("Buscando locais...");
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
                AgendaActivity.this.checkedLocals = new boolean[locals.size()]; //Erro aqui

                for (int i = 0; i < checkedLocals.length; ++i)
                    checkedLocals[i] = true;

                new SearchTimeAsyncTask().execute();

            } else {
                TreatException.treat(AgendaActivity.this, exception);
            }
        }
    }

}