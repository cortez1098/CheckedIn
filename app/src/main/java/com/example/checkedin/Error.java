package com.example.checkedin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Error extends AppCompatActivity {

    private String idUsuario = null;
    private Context cnt = this;                     // Obtiene el contexto actual
    private TextView textView = null;
    private Activity act = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        
		SharedPreferences preferencias = getSharedPreferences("preferencias", MODE_PRIVATE);
        idUsuario =  preferencias.getString("user", null);						// Obtiene el usuario almacenado en la memoria mediante SharedPreferences
        act = this;																				// Obtiene la actividad actual
        textView = findViewById(R.id.editTextError);
        textView.setOnFocusChangeListener(this::onFocusChange);									// Cambia el Listener en focus del TextView
    }

    public void onFocusChange(View v, boolean hasFocus) {										
        if (!hasFocus) {
            util.hideKeyboard(act);
        }
    }

    public void insertError(View view){
        util.hideKeyboard(act);
        String[] params = new String[]{idUsuario, textView.getText().toString()};
        new threadError().execute(params);							// Ejecuta la clase 'asincrona' threadError en un Thread diferente al principal
    }

    private class threadError extends AsyncTask<String, String, String> {				// AsyncTask (nuevo therad) para llamar al Web Service
        @Override
        protected void onPreExecute() {

        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {					// Acciones a realizar en segundo plano, en un thread diferente al principal
            if(params[1].equals("")){
                toast("ERROR, no deje el campo vacío!.");
                return null;
            }
            String retStr = WebServices.wsError(params[0], params[1]);
            if(retStr.equals("true")){
                toast("Se ha reportado el error correctamente.");
                finish();									//Cierra la 'ventana' (actividad) actual y regresa a la 'ventana' (actividad) anterior
            }
            else{
                toast( "ERROR, No se pudo reportar el error!.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public void toast(String msj){
        runOnUiThread(new Runnable(){                                               // Corre un método en el Thread de la UI
            public void run(){
                Toast.makeText(cnt, msj, Toast.LENGTH_LONG).show();                 // Crea un Toast (mensaje en pantalla momentaneo) en el Thread de la UI
            }
        });
    }
}
