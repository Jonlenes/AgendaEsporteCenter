
package com.jonlenes.app.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jonlenes.app.Modelo.Client;
import com.jonlenes.app.Modelo.ClientDao;
import com.jonlenes.app.R;
import com.jonlenes.app.TreatException;
import com.jonlenes.app.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.jansenfelipe.androidmask.MaskEditTextChangedListener;

public class CadastroClientActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private String absolutePathImage;
    private Bitmap bitmap;

    private ImageView ivClientImage;
    private EditText edtClientName;
    private EditText edtClientTelephone;

    private String sMaskTelephone;

    private Long idClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_client);

        ivClientImage = (ImageView) findViewById(R.id.ivClientImage);
        edtClientName = (EditText) findViewById(R.id.edtNameClient);
        edtClientTelephone = (EditText) findViewById(R.id.edtClientTelephone);
        Button btnSaveClient = (Button) findViewById(R.id.btnSaveClient);

        sMaskTelephone = "(##)#####-####";
        MaskEditTextChangedListener maskTelephone = new MaskEditTextChangedListener(sMaskTelephone, edtClientTelephone);
        edtClientTelephone.addTextChangedListener(maskTelephone);

        ivClientImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnSaveClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte [] bytesImage = null;

                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    bytesImage = stream.toByteArray();
                }

                if (validFields()) {

                    new InsertClientAsyncTask().execute(new Client(edtClientName.getText().toString(),
                            edtClientTelephone.getText().toString(), bytesImage));
                }
            }
        });

        idClient = getIntent().getLongExtra("idClient", -1);
        if (idClient != -1) {
            getSupportActionBar().setTitle("Alteração do cliente");
            new SearchClientAsyncTask().execute();
        } else
            getSupportActionBar().setTitle("Cadastro de cliente");

    }

    private boolean validFields() {

        if (edtClientName.getText().toString().isEmpty()) {
            edtClientName.setError("O nome não pode ser vazio.");
            return false;
        }

        if (edtClientTelephone.getText().toString().isEmpty() ||
                edtClientTelephone.getText().toString().length() < 8) {
            edtClientTelephone.setError("Telefone inválido.");
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {

                bitmap = BitmapFactory.decodeFile(absolutePathImage, new BitmapFactory.Options());
                rotateAndCompressImage(true);

            } else if (requestCode == REQUEST_IMAGE_GALLERY) {

                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    rotateAndCompressImage(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            ivClientImage.setImageBitmap(bitmap);
        }
    }


    private void rotateAndCompressImage(boolean takeCameraPhoto) {
        //int newWidth = 400;
        //int newHeight = 400;
        Matrix matrix = new Matrix();
        //float scaleWidth = ((float) newWidth) / bitmap.getWidth();;
        //float scaleHeight = ((float) newHeight) / bitmap.getHeight();

        matrix.postScale(0.3f, 0.3f);
        if (takeCameraPhoto)
            matrix.postRotate(90);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

    }

    private void selectImage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroClientActivity.this);

        builder.setTitle("Selecionar foto");
        builder.setItems(R.array.dialog_options_photo_itens, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: //Tirar foto
                        takePictureIntent();
                        break;

                    case 1: //Buscar da galeria
                        galleryTakePictureIntent();
                }
            }
        });
        builder.show();
    }

    private void takePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(CadastroClientActivity.this, "Impossivel carregar a fato.", Toast.LENGTH_LONG).show();
                return;
            }

            if (photoFile != null) {
                absolutePathImage = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void galleryTakePictureIntent()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Selecionar uma imagem"), REQUEST_IMAGE_GALLERY);
    }


    private class InsertClientAsyncTask extends AsyncTask<Client, Void, Void> {
        private final ProgressDialog progressDialog;
        private Exception exception;

        public InsertClientAsyncTask() {
            progressDialog = new ProgressDialog(CadastroClientActivity.this);
            progressDialog.setMessage("Salvando os dados...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(Client... params) {
            try {

                if (idClient == -1)
                    new ClientDao().insert(params[0]);
                else {
                    params[0].setId(idClient);
                    new ClientDao().update(params[0]);
                }

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
                TreatException.treat(CadastroClientActivity.this, exception);
            else
                CadastroClientActivity.this.finish();

        }
    }

    private class SearchClientAsyncTask extends AsyncTask<Void, Void, Client> {
        private ProgressDialog progressDialog;
        private Exception exception;

        public SearchClientAsyncTask() {
            progressDialog = new ProgressDialog(CadastroClientActivity.this);
            progressDialog.setMessage("Buscando cliente...");
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }


        @Override
        protected Client doInBackground(Void... params) {
            try {

                return new ClientDao().getClient(idClient);

            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Client client) {
            super.onPostExecute(client);

            progressDialog.dismiss();
            if (exception != null)
                TreatException.treat(CadastroClientActivity.this, exception);
            else {
                bitmap = Util.getBitmap(CadastroClientActivity.this, client.getImage());

                edtClientName.setText(client.getName());
                edtClientTelephone.setText(client.getTelephone());
                ivClientImage.setImageBitmap(bitmap);

                if (client.getImage() == null)
                    bitmap = null;
            }

        }
    }

}

