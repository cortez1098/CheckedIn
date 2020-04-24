package com.example.checkedin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import android.app.Activity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.app.ActivityCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.LOCATION_SERVICE;

public class util {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    private static Date date;
    private static Calendar calendar;

    // Esconde el teclado
    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Obtiene la Hora, los Minutos o los Segundos, dependiendo del parametro.
    public static int getHMS(int cal){
        date = new Date();
        calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        return calendar.get(cal);
    }

    // Método para obtener el IMEI del telfono. Primero pregunta si se cuenta con los permisos necesarios, de no ser el caso pide los permisos.
    public static String getIMEI(Context cnt, Activity act)
    {
        String IMEI = "";
        try {
            //Si no tiene permiso de acceder al dispositivo, lo solicita
            if (ActivityCompat.checkSelfPermission(cnt, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
            //Si sí cuenta con los permisos necesarios, obtiene IMEI
            else {
                TelephonyManager telephonyManager = (TelephonyManager) cnt.getSystemService(cnt.TELEPHONY_SERVICE);
                IMEI = telephonyManager.getDeviceId();
            }
        }
        catch(Exception e)
        {
            util.MostrarMensaje("Error", "Error al obtener IMEI. Mensaje del Sistema: " + e.getMessage(), cnt);
        }
        return IMEI;
    }

    // Función que muestra un mensaje al usuario
    public static void MostrarMensaje(String sTitulo, String sMsj, Context cnt)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(cnt);
        builder.setTitle(sTitulo);
        builder.setMessage(sMsj);
        builder.setPositiveButton("Aceptar",null);
        builder.create();
        builder.show();
    }

    // Clase que funciona para "enpaquetar" las coordenadas.
    public static class retCoord{
        //public String Coord = null;
        public String Error = null;
        public Double Latitud, Longitud = null;
        public retCoord(String E, Double Lat, Double Lon){
            Error = E;
            Latitud = Lat;
            Longitud = Lon;
        }
    }

    // Obtener las coordenadas GPS del dispositivo y las manda en un objeto de la clase retCoord
    public static retCoord getCoordGPS(Activity act)
    {
        String coordGPS = "";
        String Er = null;
        retCoord obj = new retCoord(null, 0.0, 0.0);
        try
        {
            //Verifica si tiene los permisos para acceder al GPS, sino los solicita
            if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED    )
            {
                //Requiere permisos para Android 6.0
                ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 225);
                obj.Error = "Permisos";
            }
            //Si si cuenta con los permisos necesarios, soliciza actualización de posicion GPS
            else
            {
                LocationManager locationManager = (LocationManager) act.getSystemService(LOCATION_SERVICE);
                String locationProvider = LocationManager.PASSIVE_PROVIDER;
                Location lastlocation = locationManager.getLastKnownLocation(locationProvider);
                obj.Latitud = lastlocation.getLatitude ();
                obj.Longitud = lastlocation.getLongitude ();
            }
        }
        catch (Exception e)
        {
            obj.Error = "Error " + e.getMessage();
        }
        return obj;
    }

    // Verifica si el dispositivo cuenta con lector de huella digital
    public boolean isHardwareSupported(Context context) {
        FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(context);
        return fingerprintManager.isHardwareDetected();
    }

    // Verifica que el usuario haya registrado alguna huella digital en el dispositivo
    public boolean isFingerprintAvailable(Context context) {
        FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(context);
        return fingerprintManager.hasEnrolledFingerprints();
    }

    // Verifica que el usuario haya otorgado permisos del lector de huella a la aplicación
    public boolean isPermissionGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) ==
                PackageManager.PERMISSION_GRANTED;
    }

    // Verifica que el dispositivo tenga al menos Android P
    public boolean isBiometricPromptEnabled() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P);
    }

    // Encripta a algoritmo MD5
    public static byte[] encryptMD5(byte[] data) throws Exception
    {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);
        return md5.digest();
    }

    // Encripta a algoritmo MD5
    public static String md5(byte[] source){
        BigInteger md5Data = null;
        try{
            md5Data = new BigInteger(1, encryptMD5(source));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        String md5Str = md5Data.toString(16);
        if(md5Str.length()<32){
            md5Str = 0 + md5Str;
        }
        return md5Str;
    }
}
