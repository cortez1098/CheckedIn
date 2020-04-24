package com.example.checkedin;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.ArrayList;

public class WebServices {

    //final static String ip = "192.168.43.143";          // readonly variable para la IP del servidor del Web Service
    //final static String ip = "192.168.1.10";
    final static String ip = "192.168.0.11";

    private static String NAMESPACE = "http://tempuri.org/";            // Namespace del WebService
    private static String URL = "http://" + ip + ":2020/CheckInWS.asmx";    //URL del WebService


    // Método general para todos los WebService que devuelven String. Recibe un SoapObject con todos los parametros, un METHOD_Name con el nombre del Método
    // y un SOAP_ACTION con el soap action del método.
    public static SoapPrimitive WebSericeGral(SoapObject Request, String METHOD_NAME, String SOAP_ACTION){

        // Inicializa el envelope
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(Request);          // Agrega la petición al envelope

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Proxy.NO_PROXY, URL, 6000);
        androidHttpTransport.debug = true;
        try
        {
            androidHttpTransport.call(SOAP_ACTION, envelope);       // Llama al WS
            return (SoapPrimitive) envelope.getResponse();       // Toma el resultado del WS y lo 'castea' en un objeto del tipo SoapPrimitive
            //return r.toString();        //Convierte el resultado en String
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // Verifica que el dispositivo se encuentre dentro del poligono de puntos, devuelve "true" si así es.
    public static String wsPoligono(String id, String Longi, String Lat){
        String METHOD_NAME = "estaDentro";                          // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/estaDentro";       // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        //se agregan los parametros del método a llamar
        Request.addProperty("user", id);
        Request.addProperty("Longi", Longi);
        Request.addProperty("Lat", Lat);

        SoapPrimitive r = WebSericeGral(Request, METHOD_NAME, SOAP_ACTION);
        if(r != null){
            return r.toString();
        }
        else{
            return null;
        }
    }

    // Incresa una nueva tupla en la tabla "Registro" cuando el usuario llega a su estación de trabajo. Devuelve "true" si se pudo ingresar correctamente.
    public static String wsLlegada(String h, String u, String c){
        String METHOD_NAME = "Llegada";                                 // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/Llegada";              // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        Request.addProperty("h", h);
        Request.addProperty("user", u);
        Request.addProperty("comment", c);

        SoapPrimitive r = WebSericeGral(Request, METHOD_NAME, SOAP_ACTION);
        if(r != null){
            return r.toString();
        }
        else{
            return null;
        }
    }

    // Ingresa una nueva tupla en la tabla "Dispositivo" para 'vincular' un dispositivo con un empleado. Devuelve "true" si se realizó correctamente.
    public static String wsIMEI(String u, String imei){
        String METHOD_NAME = "IMEI";                                       // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/IMEI";                    // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        Request.addProperty("user", u);
        Request.addProperty("IMEI", imei);

        SoapPrimitive r = WebSericeGral(Request, METHOD_NAME, SOAP_ACTION);
        if(r != null){
            return r.toString();
        }
        else{
            return null;
        }
    }

    // Verifica que los datos ingresados coincidan con aquellos en la tabla "Usuario"
    public  static String wsLogin(String u, String p){
        String METHOD_NAME = "Login";                                   // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/Login";                // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        Request.addProperty("user", u);
        Request.addProperty("pass", p);

        SoapPrimitive r = WebSericeGral(Request, METHOD_NAME, SOAP_ACTION);
        if(r != null){
            return r.toString();
        }
        else{
            return null;
        }
    }

    // Incresa una nueva tupla en la tabla "Registro" cuando el usuario registra una salida de su estación de trabajo. Devuelve "true" si se pudo ingresar correctamente.
    public static String wsSalida (int id, int h, int m, int s, String u, String f){
        String METHOD_NAME = "Salida";                              // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/Salida";           // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        Request.addProperty("idReg", id);
        Request.addProperty("h", h);
        Request.addProperty("m", m);
        Request.addProperty("s", s);
        Request.addProperty("user", u);
        Request.addProperty("f", f);

        SoapPrimitive r = WebSericeGral(Request, METHOD_NAME, SOAP_ACTION);
        if(r != null){
            return r.toString();
        }
        else{
            return null;
        }
    }

    // Ingresa una nueva tupla en la tabla "Error". Devuelve "true" si se realizó correctamente
    public static String wsError (String u, String d){
        String METHOD_NAME = "Error";                               // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/Error";            // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        Request.addProperty("user", u);
        Request.addProperty("desc", d);

        SoapPrimitive r = WebSericeGral(Request, METHOD_NAME, SOAP_ACTION);
        if(r != null){
            return r.toString();
        }
        else{
            return null;
        }
    }

    // Compara la hora a la que llegó el usuario con la hora almacenada en la BD, luego devuelve el ID del comentario correspondiente a qué tan tarde (o temprano) llegó.
    public static int wsHora(String id, String dia, String h, String m, String s){
        String METHOD_NAME = "hora";                                // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/hora";             // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        Request.addProperty("user", id);
        Request.addProperty("dia", dia);
        Request.addProperty("h", h);
        Request.addProperty("m", m);
        Request.addProperty("s", s);

        SoapPrimitive r = WebSericeGral(Request, METHOD_NAME, SOAP_ACTION);
        if(r != null){
            return Integer.parseInt(r.toString());
        }
        else{
            return 0;
        }
    }

    // Busca y devuelve el ID del registro de llegada
    public  static int wsIdRegistro(String u, String h, int comment, String f){
        String METHOD_NAME = "idRegistro";                                          // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/idRegistro";                       // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        Request.addProperty("user", u);
        Request.addProperty("h", h);
        Request.addProperty("comment", comment);
        Request.addProperty("f", f);

        SoapPrimitive r = WebSericeGral(Request, METHOD_NAME, SOAP_ACTION);
        if(r != null){
            return Integer.parseInt(r.toString());
        }
        else{
            return 0;
        }
    }

    // Busca y devuelve una lista de Registros dependiendo de los parametros
    // TODO: Arreglar "arrayType" en la salida
    public static ArrayList<String> wsHistorial(String u, String m, String a){
        String METHOD_NAME = "buscar";                                          // Nombre del método a llamar del Web Service
        String SOAP_ACTION = "http://tempuri.org/buscar";                       // SOAP_Action del método del Web Service

        SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);

        Request.addProperty("user", u);
        Request.addProperty("mes", m);
        Request.addProperty("anio", a);
        Request.addProperty("punt", "");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(Request);          // Agrega la petición al envelope

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Proxy.NO_PROXY, URL, 6000);
        androidHttpTransport.debug = true;
        try
        {
            androidHttpTransport.call(SOAP_ACTION, envelope);       // Llama al WS
            //return r.toString();        //Convierte el resultado en String
            SoapObject result = (SoapObject) envelope.bodyIn;               // Toma el resultado del WS y lo 'castea' en un objeto del tipo SoapObject
            int count = result.getPropertyCount();
            ArrayList<String> lista = new ArrayList<String>();
            for (int i = 0; i < count; i++)
            {
                lista.add(result.getPropertyAsString(i));
            }
            return lista;

        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
