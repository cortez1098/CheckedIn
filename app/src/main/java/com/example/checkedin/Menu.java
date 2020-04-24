package com.example.checkedin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Menu extends AppCompatActivity {

    private Button btnCheck = null;
    private Context cnt = this;
    private Activity act = this;

    private String idUsuario = null;
    private SharedPreferences prefLS = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        btnCheck = findViewById(R.id.btnChecarLlegada);

        SharedPreferences preferencias = getSharedPreferences("preferencias", MODE_PRIVATE);
        idUsuario =  preferencias.getString("user", null);

        prefLS = getSharedPreferences("prefLS", MODE_PRIVATE);
        cambiarBoton();

    }

    // Cambia el texto y Listener del botón después de haber realizado un registro de llegada o de salida
    public void cambiarBoton(){
        if(prefLS.getBoolean("Llegada", true)){
            btnCheck.setText("CHECAR LLEGADA");
            btnCheck.setOnClickListener(this::CheckLlegada);
        }
        else{
            btnCheck.setText("CHECAR SALIDA");
            btnCheck.setOnClickListener(this::CheckSalida);
        }
    }

    //Listener para registrar la salida. Desabilita el botón y ejecuta un nuevo Thread para el WS
    public void CheckSalida(View view){
        btnCheck.setEnabled(false);
        Thread thread = new Thread(this::hilo);
        thread.start();
    }

    // //Listener para registrar la llegada. Desabilita el botón y ejecuta un nuevo Thread (AsynkTask) para el WS
    public void CheckLlegada(View view){
        String[] params = null;
        btnCheck.setEnabled(false);
        new threadLlegada().execute(params);
    }

    // Thread que ejecuta el WS fuera del thread principal
    public void hilo(){
        // Obtiene la ID del registro desde las preferencias
        SharedPreferences prefID = getSharedPreferences("prefId", MODE_PRIVATE);
        int id = prefID.getInt("ID", 0);

        // Obtiene la fecha del registro desde las preferencias
        SharedPreferences prefFecha = getSharedPreferences("prefFecha", MODE_PRIVATE);
        String f = prefFecha.getString("Fecha", null);

        int horas = util.getHMS(Calendar.HOUR_OF_DAY);      //Obtiene la Hora
        int minutos = util.getHMS(Calendar.MINUTE);      //Obtiene los minutos
        int segundos = util.getHMS(Calendar.SECOND);      //Obtiene los segundos

        String retSalida = WebServices.wsSalida(id, horas, minutos, segundos, idUsuario, f);            //Llama al WS
        if(retSalida.equals("true")){
            runOnUiThread(new Runnable() {
                public void run() {
                    util.MostrarMensaje("Hecho", "Salida registrada correctamente!!", cnt);

                    //Guarda la preferencia de que se ha realizado el registro de salida
                    SharedPreferences.Editor editor = prefLS.edit();
                    editor.putBoolean("Llegada", true);
                    editor.apply();

                    cambiarBoton();
                }
            });
        }
        else{
            util.MostrarMensaje("ERROR", retSalida, cnt);
        }

        runOnUiThread(new Runnable() {
            public void run() {
                btnCheck.setEnabled(true);
            }
        });
    }

    // Inicia la 'ventana' (actividad) de Error
    public void repError(View view){
        Intent intent = new Intent(this, Error.class);
        startActivity(intent);
    }

    // TODO: terminar el historial
    // Muestra un mensaje de "proximamente"
    public void historial(View view){
        Intent intent = new Intent(this, Historial.class);
        startActivity(intent);
        //Toast.makeText(this, "C O M I N G  S O O N", Toast.LENGTH_LONG).show();
    }

    // Se ejecuta cuando se realizó un registro (salida o llegada) correctamente
    private void Success(int id) {
        runOnUiThread(new Runnable() {
            public void run() {
                util.MostrarMensaje("Hecho", "Llegada registrada correctamente!!", cnt);
            }
        });

        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        SharedPreferences prefFecha = getSharedPreferences("prefFecha", MODE_PRIVATE);
        SharedPreferences.Editor eFecha = prefFecha.edit();
        eFecha.putString("Fecha", fDate);
        eFecha.apply();

        SharedPreferences.Editor editor = prefLS.edit();
        editor.putBoolean("Llegada", false);
        editor.apply();

        cambiarBoton();
    }


    // Clase Asincrona (AsynkTask) que ejecuta el WS en un Thread distinto al main
    private class threadLlegada extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {

        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            util.retCoord objCoord = util.getCoordGPS(act);         // Obtiene las coordenadas del dispositivo
            String resultCoord = "error";

            if(objCoord.Error == null){             //Si se pudieron obtener coordenadas del GPS
                String Longi = objCoord.Longitud.toString();
                String Lat = objCoord.Latitud.toString();
                resultCoord = WebServices.wsPoligono(idUsuario, Longi, Lat);
            }
            else{                                   //NO se pudieron obtener coordenadas del GPS
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(cnt, objCoord.Error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            if(resultCoord.equals("true")){             //Si está dentro del poligono
                int horas = util.getHMS(Calendar.HOUR_OF_DAY);      //Obtiene la Hora
                int minutos = util.getHMS(Calendar.MINUTE);      //Obtiene los minutos
                int segundos = util.getHMS(Calendar.SECOND);      //Obtiene los segundos
                int resultHora = WebServices.wsHora(idUsuario, "Lunes", Integer.toString(horas), Integer.toString(minutos), Integer.toString(segundos));        //Llama al WebService que compara las horas
                if(resultHora != 0){            //Verifica que el WebService devolviera algo
                    String h = Integer.toString(horas) + ":" + Integer.toString(minutos) + ":"  + Integer.toString(segundos);       //Crea la cadena de hora
                    String resultLlegada = WebServices.wsLlegada(h, idUsuario, Integer.toString(resultHora));           //Llama al WebService para ingresar una tupla de registro de llegada
                    if(resultLlegada.equals("true")){           //Si everything sale bien
                        Date cDate = new Date();
                        String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);            // Obtiene la fecha en formato YYYY-MM-DD
                        int idRegistro = WebServices.wsIdRegistro(idUsuario, h, resultHora, fDate);         // Llama al WS de idRegistro

                        SharedPreferences prefId = getSharedPreferences("prefId", MODE_PRIVATE);        //Guarda la ID del registro en las preferencias
                        SharedPreferences.Editor eID = prefId.edit();
                        eID.putInt("ID", idRegistro);
                        eID.apply();

                        Success(idRegistro);                // Llama el método Success
                    }
                    else{                                       //si sale algún error (i.e. no se encontró el usuario, server dead, etc)
                        runOnUiThread(new Runnable() {
                            public void run() {
                                util.MostrarMensaje("ERROR", "No se pudo realizar el registro", cnt);
                            }
                        });
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            btnCheck.setEnabled(true);
                        }
                    });
                }
                else{                           //Si el WS no devuelve lo esperado
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(cnt, "ERROR. El empleado no se encuentra dentro del área de trabajo", Toast.LENGTH_LONG).show();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        public void run() {
                            btnCheck.setEnabled(true);
                        }
                    });
                }
            }
            else if(resultCoord.equals("Permisos")){
                runOnUiThread(new Runnable() {
                    public void run() {
                        util.MostrarMensaje("Permisos!", "No se cuenta con permisos de GPS, vuelva a intentarlo después de otorgar los permisos.", cnt);
                        btnCheck.setEnabled(true);
                    }
                });
            }
            else{                               //NO está dentro del poligono
                runOnUiThread(new Runnable() {
                    public void run() {
                        util.MostrarMensaje("ERROR", "El empleado no se encuentra dentro del área de trabajo", cnt);
                        btnCheck.setEnabled(true);
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
