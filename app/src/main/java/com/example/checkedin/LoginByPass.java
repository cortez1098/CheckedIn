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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginByPass extends AppCompatActivity {

    public Button btn = null;
    public EditText txt_user = null;
    public EditText txt_pass = null;
    public Activity act = this;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    private Context cnt = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_by_pass);

        // Verifica si se va a utilizar el IMEI a través de las preferencias y pude los permisos necesarios
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        Boolean imei = preferences.getBoolean("IMEI", true);

        if(imei){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        }

        btn = findViewById(R.id.btnIniciarSes);
        txt_user = findViewById(R.id.txt_User);
        txt_pass = findViewById(R.id.txt_Pass);
        txt_user.setOnFocusChangeListener(this::onFocusChange);         // Para ocultar el teclado cuando se pierde el focus del TextBox
        txt_pass.setOnFocusChangeListener(this::onFocusChange);         // Para ocultar el teclado cuando se pierde el focus del TextBox
    }

    // Esconde el teclado
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            util.hideKeyboard(act);
        }
    }

    // Esconde el teclado al hacer click en el botón y llama al método Login()
    public void compLogin(View view) {
        util.hideKeyboard(act);
        Login();
    }

    // Encripta la información ingresada y ejecuta un nuevo Thread para el WS
    public void Login() {
        btn.setEnabled(false);

        String userMD5 = util.md5(txt_user.getText().toString().getBytes());                // Encripta el nombre de usuario por medio del algoritmo MD5 de 16 bytes
        String passMD5 = util.md5(txt_pass.getText().toString().getBytes());                // Encripta la contraseña por medio del algoritmo MD5 de 16 bytes

        String[] params = new String[]{userMD5, passMD5};
        new threadLogin().execute(params);                          // Ejecuta la clase Asincrona (AsyncTask) para acceder al WS

        btn.setEnabled(true);
    }

    // Invocado cuando el Login fue ejecutado con exito.
    private void Success(String u){
        // Indica que ya se realizó el primer Login
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);           //Primera vez que se loguea
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstLogin", false);
        editor.apply();

        // Almacena el usuario (encriptado) para posterior uso (en la clase Menu)
        SharedPreferences preferencias = getSharedPreferences("preferencias", MODE_PRIVATE);           //Almacena el usuario, en Hash MD5
        SharedPreferences.Editor edithor = preferencias.edit();
        edithor.putString("user", u);
        edithor.apply();

        // Obtiene y encripta el IMEI del telefono
        String tmSerial = util.getIMEI(this, this);
        String serialMD5 = util.md5(tmSerial.getBytes());

        // Verifica si ya se ingresó el IMEI en la BD. Sino se ha hecho, lo ingresa. De lo contrario abre la 'ventana' (actividad) de Menu
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        Boolean imei = preferences.getBoolean("IMEI", true);
        if(imei){
            String[] params = new String[]{u, serialMD5};
            new threadIMEI().execute(params);
        }
        else{
            Intent intent = new Intent(this, Menu.class);
            startActivity(intent);
        }
    }

    // Clase Asincrona (AsyncTask) que crea un nuevo Thread para ejecutar el WS del IMEI
    private class threadIMEI extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {

        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            // Llama al WS para ingresar el IMEI en la BD
            String retStr = WebServices.wsIMEI(params[0], params[1]);
            if(retStr.equals("true")){
                // Almacena que ya se ingresó el IMEI en la BD, para no volver a pedirlo
                SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);           //Primera vez que se loguea
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("IMEI", false);
                editor.apply();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(cnt, "Se ha vinculado su dispositivo a su cuenta", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(cnt, "ERROR, No se pudo ingresar el IMEI.", Toast.LENGTH_SHORT).show();
                        txt_user.setText("");
                        txt_pass.setText("");
                    }
                });
            }
            // Abre la 'ventana' (actividad) de Menú
            Intent intent = new Intent(cnt, Menu.class);
            startActivity(intent);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //retStr = result.toString() + msj;
        }
    }

    // Clase Asincrona (AsyncTask) que crea un nuevo Thread para ejecutar el WS del Login
    private class threadLogin extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {

        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            // Llama al WS de Login y si devuelve "true", invoca el método de Success. De lo contrario reinicia los TextView's
            String retStr = WebServices.wsLogin(params[0], params[1]);
            if(retStr.equals("true")){
                Success(params[0]);
            }
            else{
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(cnt, "ERROR, los datos no coinciden.", Toast.LENGTH_SHORT).show();
                        txt_user.setText("");
                        txt_pass.setText("");
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //retStr = result.toString() + msj;
        }
    }


}
