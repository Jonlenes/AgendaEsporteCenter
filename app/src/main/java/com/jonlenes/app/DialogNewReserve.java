package com.jonlenes.app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jonlenes.app.Modelo.AgendaBo;
import com.jonlenes.app.Modelo.Client;
import com.jonlenes.app.Modelo.ClientDao;
import com.jonlenes.app.Modelo.Local;
import com.jonlenes.app.Modelo.LocalDao;
import com.jonlenes.app.Modelo.Reserve;
import com.jonlenes.app.Modelo.ReserveDao;
import com.jonlenes.app.View.ViewReserveActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by asus on 27/07/2016.
 */
public class DialogNewReserve extends DialogFragment {

    private FragmentActivity activity;
    private AlertDialog dialog;
    private View view;

    private EditText edtDateReserve;
    private EditText edtHoursReserva;
    private EditText edtDurationReserve;
    private Spinner spnLocalReserve;
    private Spinner spnClientReserve;
    private CheckBox chkRecorrenciaMensal;

    private boolean isRecorrenciaMensal;
    private Long idReserve;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_new_reserve, null);

        edtDateReserve = (EditText) view.findViewById(R.id.edtDateReserve);
        edtHoursReserva = (EditText) view.findViewById(R.id.edtHoursReserva);
        edtDurationReserve = (EditText) view.findViewById(R.id.edtDurationReserve);
        spnLocalReserve = (Spinner) view.findViewById(R.id.spnLocalReserve);
        spnClientReserve = (Spinner) view.findViewById(R.id.spnClient);
        chkRecorrenciaMensal = (CheckBox) view.findViewById(R.id.chkRecorrenciaMensal);

        edtHoursReserva.setOnClickListener(clickTimeReserve);
        edtDateReserve.setOnClickListener(clickDateReserve);
        edtDurationReserve.setOnClickListener(clickDurationReserve);

        builder.setView(view)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancelar", null);

        Bundle bundle = getArguments();
        idReserve = (bundle != null ? bundle.getLong("idReserve", -1) : -1L);
        if (idReserve != -1) {
            builder.setTitle("Atualizando reserva");
        } else
            builder.setTitle("Nova reserva");

        dialog = builder.create();
        dialog.show();

        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (invalidFields())
                        return;

                    Reserve Reserve = new Reserve();

                    Reserve.setId(idReserve);
                    Reserve.setDateDay(Util.parseDate(edtDateReserve.getText().toString()));
                    Reserve.setStartTime(Util.parseTime(edtHoursReserva.getText().toString()));
                    Reserve.setDuration(Long.parseLong((edtDurationReserve).getText().toString()));
                    Reserve.setEndTime(Util.sumTime(Reserve.getStartTime(), Reserve.getDuration()));
                    Reserve.setLocal((Local) spnLocalReserve.getSelectedItem());
                    Reserve.setClient((Client) spnClientReserve.getSelectedItem());

                    isRecorrenciaMensal = chkRecorrenciaMensal.isChecked();

                    new InsertReserveAsyncTask().execute(Reserve);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        new SearchClientsAsyncTask().execute();
        new SearchLocalsAsyncTask().execute();

        if (idReserve != -1)
            new SearchReserveAsyncTask().execute();

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (idReserve != -1) {
            ((ViewReserveActivity) getActivity()).refreshActivity();
        } else {
            ((AgendaActivity) getActivity()).refreshActivity();
        }

    }
    private final View.OnClickListener clickDateReserve = new View.OnClickListener() {
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

            new DatePickerFragment().show(activity.getSupportFragmentManager(), "dialog");
        }
    };


    View.OnClickListener clickTimeReserve = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            class TimePickerFragment extends DialogFragment
                    implements TimePickerDialog.OnTimeSetListener {

                @NonNull
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {

                    final Calendar c = Calendar.getInstance();
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int minute = c.get(Calendar.MINUTE);

                    return new TimePickerDialog(getActivity(), this, hour, minute / 5, true);
                }

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if (minute % 5 != 0)
                        minute = ((minute / 5) + 1) * 5;

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(0, 0, 0, hourOfDay, minute);

                    edtHoursReserva.setText(new SimpleDateFormat("HH:mm").format(calendar.getTime()));
                }
            }
            new TimePickerFragment().show(activity.getSupportFragmentManager(), "dialog");
        }
    };

    View.OnClickListener clickDurationReserve = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            class DialogNumberPicker extends DialogFragment {
                NumberPicker numberPicker;

                @NonNull
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    super.onCreateDialog(savedInstanceState);

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    view = inflater.inflate(R.layout.dialog_number_picker, null);

                    numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
                    setValuesInterval(numberPicker);

                    if (!edtDurationReserve.getText().toString().isEmpty())
                        numberPicker.setValue((Integer.parseInt(edtDurationReserve.getText().toString()) / 5) - 1);


                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setView(view);
                    builder.setNegativeButton("Cancelar", null);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            edtDurationReserve.setText(String.valueOf((numberPicker.getValue() + 1) * 5));
                        }
                    });

                    return builder.create();
                }
            }
            new DialogNumberPicker().show(DialogNewReserve.this.getChildFragmentManager(), "dialog");
        }
    };

    private boolean invalidFields() {
        if (edtDateReserve.getText().toString().isEmpty()) {
            edtDateReserve.setError("A data deve ser preenchida.");
            return true;
        }

        if (edtHoursReserva.getText().toString().isEmpty()) {
            edtHoursReserva.setError("A hora deve ser preenchida.");
            return true;
        }

        if (edtDurationReserve.getText().toString().isEmpty()) {
            edtDurationReserve.setError("O tempo de duração deve ser preenchido.");
            return true;
        }

        return false;
    }

    private int getIndexItemSpinner(Spinner spinner, Long id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item instanceof Client) {
                if (((Client) item).getId().equals(id))
                    return i;
            } else if (item instanceof Local) {
                if (((Local) item).getId().equals(id))
                    return i;
            }
        }
        return 0;
    }

    private void setValuesInterval(NumberPicker numberPicker) {
        int NUMBER_OF_VALUES = 960/5;
        int PICKER_RANGE = 5;

        String[] displayedValues  = new String[NUMBER_OF_VALUES];

        for(int i = 0; i < NUMBER_OF_VALUES; i++)
            displayedValues[i] = String.valueOf(PICKER_RANGE * (i + 1));

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(displayedValues.length - 1);
        numberPicker.setDisplayedValues(displayedValues);
    }

    private class SearchClientsAsyncTask extends AsyncTask<Void, Void, List<Client>> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public SearchClientsAsyncTask() {
            progressDialog = new ProgressDialog(activity);
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
                ArrayAdapter<Client> adapterClients = new ArrayAdapter<>(activity,
                        android.R.layout.simple_spinner_item, list);
                adapterClients.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnClientReserve.setAdapter(adapterClients);
            } else {
                TreatException.treat(activity, exception);
            }

        }
    }

    private class SearchLocalsAsyncTask extends AsyncTask<Void, Void, List<Local>> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public SearchLocalsAsyncTask() {
            progressDialog = new ProgressDialog(activity);
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

                ArrayAdapter<Local> adapterLocal = new ArrayAdapter<>(activity,
                        android.R.layout.simple_spinner_item, locals);
                adapterLocal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnLocalReserve.setAdapter(adapterLocal);

            } else {
                TreatException.treat(activity, exception);
            }
        }
    }

    private class SearchReserveAsyncTask extends AsyncTask<Void, Void, Reserve> {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public SearchReserveAsyncTask() {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Buscando reserva...");
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

                return new ReserveDao().getReserve(idReserve);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Reserve Reserve) {
            super.onPostExecute(Reserve);

            if (exception == null) {

                int i = 0;
                while (i < 5 && (spnClientReserve.getAdapter().isEmpty() || spnLocalReserve.getAdapter().isEmpty())) {
                    try {
                        wait(1000);
                        ++i;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (i == 5) {
                    Toast.makeText(activity, "Não foi possível buscar a reserva.", Toast.LENGTH_LONG).show();
                    dismiss();
                } else {

                    edtDateReserve.setText(Util.formatDate(Reserve.getDateDay()));
                    edtHoursReserva.setText(Util.formatTime(Reserve.getStartTime()));
                    edtDurationReserve.setText(String.valueOf(Reserve.getDuration()));
                    spnClientReserve.setSelection(getIndexItemSpinner(spnClientReserve, Reserve.getClient().getId()));
                    spnLocalReserve.setSelection(getIndexItemSpinner(spnLocalReserve, Reserve.getLocal().getId()));

                }


            } else {
                TreatException.treat(activity, exception);
            }

            progressDialog.dismiss();

        }
    }

    private class InsertReserveAsyncTask extends AsyncTask<Reserve, Void, Void> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public InsertReserveAsyncTask() {
            progressDialog = new ProgressDialog(activity);
            if (idReserve != -1)
                progressDialog.setMessage("Atualizando...");
            else
                progressDialog.setMessage("Reservando...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Reserve... params) {
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
                TreatException.treat(activity, exception);
            else
                dialog.dismiss();
        }
    }
}
