package ios.com.enviodedatos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Utils {

    public static boolean flagLocationChanged = false;

    public static boolean camara = false;

    public static final int delay = 800;    // Tiempo de SnackBar
    public static boolean startActGab = true;  // Variable para saber si se lanza el Activity Theos despues de WSActas
    public static boolean cuentas = false;
    public static String credito = "";      // PRIMARY KEY de referencia
    public static String cliente  = "";     // Nombre del cliente
    public static String calendario = "";   // Variable para saber donde poner la fecha y hora de calendario
    public static String regresar = "";     // Variable para saber a donde regresar despues de encueta (Pendientes/Agendadas)
    public static String id = "";           // id de la foto que se tomo
    public static String agencia = "";      // agencia

    public static String strQuery = "";
    public static String strLike = "";

    //Configuracion de la camara
    public static ProgressDialog progressDialog;
    public static boolean flagOk = false;
    public static int tipoFlash = 1;

    public static String strCuentas = "";
    public static String fragment = "";

    public static int hora1 = 7;
    public static int minutos1 = 0;

    public static int itemRes = 0;                          // Indice de Resultados
    public static String strFechaPago  = "Fecha de pago";    // Fecha de pago
    public static String strFechaPago2 = "Fecha de pago";    // Fecha de pago
    public static String strFechaPago3 = "Fecha de pago";    // Fecha de pago
    public static String strFechaPago4 = "Fecha de pago";    // Fecha de pago

    public static int anio = 0;
    public static int mes = 0;
    public static int dia = 0;

    public static int itemEstado = 0;   // Indice de Estado
    public static int itemEstrategia = 0;   // Indice de Estado
    public static int itemCiudad = 0;   // Indice de Ciudad
    public static int itemColonia = 0;  // Indice de Colonia

    public static int totalE = 0;       // Contador de Estados
    public static int totalM = 0;       // Contador de Municipios
    public static int totalC = 0;       // Contador de Colonias

    public static String producto = "";

    //public static int countDatos = 0;           // Variable para saber si se mando a llamar al servicio el tamaño tamaño de datos
    public static int countDatosFail = 0;       // Variabe para contar los datos que no se enviaron
    public static String strFailDatos = "";     // String que recopila los errores al enviar los datos

    //public static int countFotos = 0;           // Variable para saber si se mando a llamar al servicio el tamaño tamaño de imagenes
    public static int countFotosFail = 0;       // Variabe para contar las imagenes que no se enviaro
    public static String strFailFotos = "";     // String que recopila los errores al enviar las imagenes

    //public static int countPromesas = 0;           // Variable para saber si se mando a llamar al servicio el tamaño tamaño de promesas
    public static int countPromesasFail = 0;       // Variabe para contar las promesas que no se enviaro
    public static String strFailPromesas = "";     // String que recopila los errores al enviar las promesas

    public static boolean flagSendAuditoriPackage  = true;
    public static String nameAplicationFake = "";

    public static int countAdicional = 0;       // Variable para saber si se mando a llamar al servicio el tamaño tamaño de adicionales

    public static String  addressfromMarker = "";


    @SuppressLint("CommitPrefEdits")
    public static void setPreferenceBooleanValue(String key, boolean value) {
        //sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public static void initProgressDialog(Context ctx, String message) {
        progressDialog = null;
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
    }


    // Decodifica una imagen transformada en base 64.
    public static Bitmap decodeFromBase64(String encodedImage) {
        try {
            byte[] decodedString = ios.com.enviodedatos.Base64.decode(encodedImage.getBytes());
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }



    public static void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public static void hideKeyboard(@NonNull Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }







}
