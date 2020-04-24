package com.example.checkedin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Historial extends AppCompatActivity {

    private Spinner cb_Mes;
    private Spinner cb_Anio;
    private ListView lv;
    private Context cnt = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        cb_Mes = findViewById(R.id.cb1);
        cb_Anio = findViewById(R.id.cb2);
        lv = findViewById(R.id.listview);


    }

    public void btnBuscar(View view){
        String anio = cb_Anio.getSelectedItem().toString();
        String mes = cb_Mes.getSelectedItem().toString().substring(0, 2);

        SharedPreferences preferencias = getSharedPreferences("preferencias", MODE_PRIVATE);
        String idUsuario =  preferencias.getString("user", null);

        String[] params = new String[] { idUsuario, mes, anio, "" };
        new threadBuscar().execute(params);
    }


    // Clase Asincrona (AsynkTask) que ejecuta el WS en un Thread distinto al main
    private class threadBuscar extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {

        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            ArrayList<String> al = (ArrayList<String>) WebServices.wsHistorial(params[0], params[1], params[2]);            // Ejecuta el WS
            if(al != null){
                runOnUiThread(new Runnable() {
                    public void run() {
                        ArrayAdapter adapter = new ArrayAdapter(cnt, android.R.layout.simple_list_item_1, al);
                        lv.setAdapter(adapter);                                                             // Ingresa el ArrayList al ListView
                    }
                });
            }
            else{
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(cnt, "ERROR. No se pudo obtener la información", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }
}
