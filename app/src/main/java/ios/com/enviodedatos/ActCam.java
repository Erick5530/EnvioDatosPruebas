package ios.com.enviodedatos;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ActCam extends AppCompatActivity {

    /* DECLARACION DE OBJETOS */

    private ClsAcel acelerometro;

    private SurfaceView camera_preview;     // Preview de la camara
    private ImageView button_capture;       // Tomar la fotografia
    private ImageView btnNo;                // Cancelar la foto / volver a tomar
    private ImageView btnFlash;             // Configurar flash
    private ImageView rotateLeft;           // Rotar la imagen a la izquierda
    private ImageView rotateRight;           // Rotar la imagen a la izquierda

    private SurfaceHolder previewHolder = null;
    public Camera camera;
    private Camera.PictureCallback mPicture;

    private ScaleGestureDetector SGD;
    private Camera.Parameters parameters;
    private File pictureFile;

    private Bitmap myBitmap;                // Bitmap de imagen original

    private File dir;

    private CropImageView cropImageView;

    /* DECLARACIOON DE VARIABLES GLOBALES */

    int currentZoomLevel = 0;
    private int maxZoomLevel;
    private boolean flagFlash = true;

    public ActCam() {
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.progressDialog != null)
            Utils.progressDialog.dismiss();

        /* ESTABLECER LA ACTIVIDAD EN PANTALLA COMPLETA */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.act_take_picture);

        /* PREPARAR EL SENSOR ACELEROMETRO */
        acelerometro = new ClsAcel(this);

        /* DECLARACION INSTANCIAS */
        camera_preview = findViewById(R.id.surface);
        button_capture = findViewById(R.id.button_capture);
        btnNo = findViewById(R.id.buttonNo);
        btnFlash = findViewById(R.id.buttonFlash);
        rotateLeft = findViewById(R.id.buttonRotate90pos);
        rotateRight = findViewById(R.id.buttonRotateM90pos);
        cropImageView = findViewById(R.id.cropImageView);

        cropImageView.setAutoZoomEnabled(true);

        btnNo.setVisibility(View.GONE);
        rotateLeft.setVisibility(View.GONE);
        rotateRight.setVisibility(View.GONE);

        /* DECLARACIÓN DE EVENTOS */
        camera_PictureCallback();
        rotateLeft_setOnClickListener();
        rotateRiht_setOnClickListener();
        btnNo_setOnClickListener();
        btnFlash_setOnClickListener();
        button_capture_setOnClickListener();

        /* COMPROBAMOS SI EL DISPOSITIVO TIENE FLASH */
        if (!suportFlash()) {
            flagFlash = false;
        }

        if (flagFlash)
            btnFlash.setVisibility(View.VISIBLE);
        else
            btnFlash.setVisibility(View.GONE);

        previewHolder = camera_preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        previewHolder.setFixedSize(getWindow().getWindowManager()
                .getDefaultDisplay().getWidth(), getWindow().getWindowManager()
                .getDefaultDisplay().getHeight());

        SGD = new ScaleGestureDetector(this, new ScaleListener());
    }

    private void rotateLeft_setOnClickListener() {

        rotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(-90);

                System.out.println("Rotando a la izquierda");

                PhotoViewAttacher pAttacher;
                pAttacher = new PhotoViewAttacher((ImageView)cropImageView.getRootView());
                pAttacher.update();

            }
        });
    }

    private void rotateRiht_setOnClickListener() {

        rotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.rotateImage(90);
                System.out.println("Rotando a la derecha");
            }
        });
    }

    private void btnNo_setOnClickListener() {

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                (findViewById(R.id.buttonNo)).setEnabled(false);
                cropImageView.setImageBitmap(null);
                cropImageView.clearImage();

                (findViewById(R.id.buttonRotate90pos)).setVisibility(View.GONE);
                (findViewById(R.id.buttonRotateM90pos)).setVisibility(View.GONE);
                if (flagFlash) {
                    (findViewById(R.id.buttonFlash)).setVisibility(View.VISIBLE);
                } else {
                    (findViewById(R.id.buttonFlash)).setVisibility(View.GONE);
                }
                Animation modal_in = AnimationUtils.loadAnimation(ActCam.this, R.anim.modal_in);
                modal_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ((ImageView) findViewById(R.id.button_capture)).setImageResource(R.drawable.icon_circle);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                modal_in.reset();
                (findViewById(R.id.button_capture)).startAnimation(modal_in);

                Animation modal_out = AnimationUtils.loadAnimation(ActCam.this, R.anim.modal_out);
                modal_out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                modal_out.reset();
                (findViewById(R.id.buttonFlash)).startAnimation(modal_out);

                ((ImageView) findViewById(R.id.button_capture)).setImageResource(R.drawable.icon_circle);
                (findViewById(R.id.buttonNo)).setVisibility(View.GONE);
                (findViewById(R.id.button_capture)).setVisibility(View.VISIBLE);
                (findViewById(R.id.button_capture)).setEnabled(true);

                camera_preview.setVisibility(View.VISIBLE);
                cropImageView.setVisibility(View.INVISIBLE);
                try {
                    if (camera == null)
                        Toast.makeText(ActCam.this, "No es posible tomar la fotografía, la cámara no está disponible.", Toast.LENGTH_LONG).show();

                } catch (Exception ignored) {
                }

                if (myBitmap != null && !myBitmap.isRecycled()) {
                    myBitmap.recycle();
                    myBitmap = null;
                }

                Utils.flagOk = false;
                Utils.freeMemory();
            }
        });
    }

    private void btnFlash_setOnClickListener() {

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                * 0 = Apagado
                * 1 = Automatico
                * 2 = Encendido
                * */
                Utils.tipoFlash++;
                if (Utils.tipoFlash == 3) {
                    Utils.tipoFlash = 0;
                }
                switch (Utils.tipoFlash) {
                    case 0:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                        ((ImageView) findViewById(R.id.buttonFlash)).setImageResource(R.drawable.ic_flash_off_white_24dp);
                        break;
                    case 1:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        camera.setParameters(parameters);
                        ((ImageView) findViewById(R.id.buttonFlash)).setImageResource(R.drawable.ic_flash_auto_white_24dp);
                        break;
                    case 2:
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        camera.setParameters(parameters);
                        ((ImageView) findViewById(R.id.buttonFlash)).setImageResource(R.drawable.ic_flash_on_white_24dp);
                        break;
                }
                System.out.println("Este es el tipo de flash : " + Utils.tipoFlash);
            }
        });
    }

    private void button_capture_setOnClickListener() {

        button_capture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                (findViewById(R.id.button_capture)).setEnabled(false);
                (findViewById(R.id.buttonNo)).setEnabled(true);
                try {
                    camera.setParameters(parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                (findViewById(R.id.buttonRotate90pos)).setEnabled(false);
                (findViewById(R.id.buttonRotateM90pos)).setEnabled(false);

                Animation modal_out = AnimationUtils.loadAnimation(ActCam.this, R.anim.modal_out);
                modal_out.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        (findViewById(R.id.buttonFlash)).setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                modal_out.reset();
                (findViewById(R.id.buttonFlash)).startAnimation(modal_out);

                Animation modal_in = AnimationUtils.loadAnimation(ActCam.this, R.anim.modal_in);
                modal_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                modal_in.reset();
                (findViewById(R.id.buttonNo)).startAnimation(modal_in);

                Animation modal_in_cap = AnimationUtils.loadAnimation(ActCam.this, R.anim.modal_in);
                modal_in_cap.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        ((ImageView) findViewById(R.id.button_capture)).setImageResource(R.drawable.icon_circle_ok);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                modal_in_cap.reset();
                (findViewById(R.id.button_capture)).startAnimation(modal_in_cap);

                if (Utils.flagOk) {

                    (findViewById(R.id.button_capture)).setEnabled(false);

                    DateFormat dateFormatFoto = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                    Date dateFoto = new Date();
                    String Fecha = dateFormatFoto.format(dateFoto);

                    String s;
                    String sR;

                    if (myBitmap != null) {

                        s = convertBitmapToStringRedi(cropImageView.getCroppedImage());
                        sR = convertBitmapToStringRedi(rediImagePorTamanio(cropImageView.getCroppedImage(), 50));

                        Utils.freeMemory();

                        String tipoFoto = getTipoFoto(ActGab.tmpDescImagen);

                        File pictureFileCompress = new File(dir, "Img.jpg");

                        try {

                            SQLiteDatabase db = openOrCreateDatabase(DB.DB_NAME, Context.MODE_PRIVATE, null);

                            if (existeFoto(Utils.credito, tipoFoto)) {
                                ContentValues valuesImages = new ContentValues();
                                valuesImages.put(DB.PR_MC_CREDITO, Utils.credito);
                                valuesImages.put(DB.FECHA_VISITA, Fecha);
                                valuesImages.put(DB.CAT_PA_ID, tipoFoto);
                                valuesImages.put(DB.IMAGEN, s);
                                valuesImages.put("NOMBRE_IMAGEN", pictureFileCompress.getName());
                                db.update(DB.TABLE_NAME_IMAGENES, valuesImages,
                                        DB.PR_MC_CREDITO + "='" + Utils.credito + "' AND " + DB.CAT_PA_ID + "= '" + tipoFoto + "'", null);
                                System.out.println("UPDATE FOTO");
                                Utils.saveLog(ActCam.this,"ActCam","UPDATE TB_IMG: " + Utils.credito + " Fecha:" + Fecha + " TIPO:" + tipoFoto);
                            } else {
                                ContentValues valuesImages = new ContentValues();
                                valuesImages.put(DB.PR_MC_CREDITO, Utils.credito);
                                valuesImages.put(DB.FECHA_VISITA, Fecha);
                                valuesImages.put(DB.CAT_PA_ID, tipoFoto);
                                valuesImages.put(DB.IMAGEN, s);
                                valuesImages.put("NOMBRE_IMAGEN", pictureFileCompress.getName());
                                db.insert(DB.TABLE_NAME_IMAGENES, null, valuesImages);
                                System.out.println("INSERT FOTO");
                                Utils.saveLog(ActCam.this,"ActCam","INSERT TB_IMG: " + Utils.credito + " Fecha:" + Fecha + " TIPO:" + tipoFoto);
                            }

                            if (existeFotoAux(Utils.credito, tipoFoto)) {
                                System.out.println("Entro en el update de aux  image");
                                ContentValues valuesImages = new ContentValues();
                                valuesImages.put(DB.PR_MC_CREDITO, Utils.credito);
                                valuesImages.put(DB.FECHA_VISITA, Fecha);
                                valuesImages.put(DB.CAT_PA_ID, tipoFoto);
                                valuesImages.put(DB.IMAGEN, sR);
                                valuesImages.put("NOMBRE_IMAGEN", pictureFileCompress.getName());
                                db.update(DB.TABLE_NAME_IMAGENES_AUX, valuesImages,
                                        DB.PR_MC_CREDITO + "='" + Utils.credito + "' AND " + DB.CAT_PA_ID + "= '" + tipoFoto + "'", null);
                                Utils.saveLog(ActCam.this,"ActCam","UPDATE TB_IMG_AUX: " + Utils.credito + " Fecha:" + Fecha + " TIPO:" + tipoFoto);
                            } else {
                                System.out.println("Entro en el insert de aux  image");
                                ContentValues valuesImages = new ContentValues();
                                valuesImages.put(DB.PR_MC_CREDITO, Utils.credito);
                                valuesImages.put(DB.FECHA_VISITA, Fecha);
                                valuesImages.put(DB.CAT_PA_ID, tipoFoto);
                                valuesImages.put(DB.IMAGEN, sR);
                                valuesImages.put("NOMBRE_IMAGEN", pictureFileCompress.getName());
                                db.insert(DB.TABLE_NAME_IMAGENES_AUX, null, valuesImages);
                                Utils.saveLog(ActCam.this,"ActCam","INSERT TB_IMG_AUX: " + Utils.credito + " Fecha:" + Fecha + " TIPO:" + tipoFoto);
                            }

                            if (tipoFoto.equals("7")) {
                                if (FrgWork.rdnSiLuz != null) {
                                    FrgWork.rdnSiLuz.setChecked(true);
                                }
                            }

                            if (tipoFoto.equals("8")) {
                                if (FrgWork.rdnSiCon != null) {
                                    FrgWork.rdnSiCon.setChecked(true);
                                }
                            }
                            db.close();
                            System.out.println("Imagen: agregada correctamente");

                            if (!ActGab.arrayListFotoTomada.contains(ActGab.tmpDescImagen)) {
                                System.out.println("Si aumento el contador");
                                ActGab.countFotos++;
                            }
                            ActGab.arrayListFotoTomada.add(ActGab.tmpDescImagen);
                            FrgWork.fotos.setText("Fotos: " + ActGab.countFotos);

                        } catch (Exception e) {
                            //TODO: Volver a insertar pero reducida la imagen
                            Utils.saveLog(ActCam.this,"ActCam",e.toString());
                            e.printStackTrace();
                        }

                        cropImageView.setImageBitmap(null);
                        cropImageView.clearImage();

                        if (myBitmap != null && !myBitmap.isRecycled()) {
                            myBitmap.recycle();
                            myBitmap = null;
                        }

                        Utils.flagOk = false;
                        Utils.progressDialog.dismiss();

                        finish();
                        if (camera != null)
                            camera.release();

                        ActGab.itemCam.setIcon(R.drawable.ic_camera_alt_white_24dp);
                    } else {
                        btnNo.performClick();
                    }

                } else if (!Utils.flagOk) {

                    button_capture.setEnabled(false);
                    try {
                        camera.takePicture(null, null, mPicture);
                    } catch (Exception e) {
                        System.out.println("EX controlada: " + e.toString());
                        Utils.saveLog(ActCam.this,"ActCam",e.toString());
                    }
                }
            }
        });


    }

    private void camera_PictureCallback() {

        mPicture = new Camera.PictureCallback() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {

                    dir = new File(Environment.getExternalStorageDirectory(), "MC");
                    if (!dir.exists())
                        dir.mkdirs();

                    pictureFile = new File(dir, "Img.jpg");

                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();

                } catch (Exception ignored) {
                } finally {

                    myBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());

                    double ejex = acelerometro.getEjex();
                    double ejey = acelerometro.getEjey();
                    double ejez = acelerometro.getEjez();

                    System.out.println("Ejex : " + ejex);
                    System.out.println("Ejey : " + ejey);
                    System.out.println("Ejez : " + ejez);

                    /***************** REDIMENSIONAR LA IMAGEN SEGUN SU TAMAÑO*********************/

                    double pesoImagen = pictureFile.length();

                    if (pesoImagen >= 500000 && pesoImagen <= 700000) {
                        myBitmap = rediImagePorTamanio(myBitmap, 70);
                        System.out.println("La imagen se redujo a su 70 %");
                    } else if (pesoImagen > 700000 && pesoImagen <= 1000000) {
                        myBitmap = rediImagePorTamanio(myBitmap, 50);
                        System.out.println("La imagen se redujo a su 50 %");
                    } else if (pesoImagen > 1000000 && pesoImagen <= 2000000) {
                        myBitmap = rediImagePorTamanio(myBitmap, 30);
                        System.out.println("La imagen se redujo a su 30 %");
                    } else if (pesoImagen < 500000) {
                        myBitmap = rediImagePorTamanio(myBitmap, 90);
                        System.out.println("La imagen se redujo a su 90 %");
                    } else if (pesoImagen > 2000000) {
                        myBitmap = rediImagePorTamanio(myBitmap, 20);
                        System.out.println("La imagen se redujo a su 20 %");
                    }

                    /**************************************************************************************/

                    if ((ejex >= 0 && ejex <= 10) && (ejey >= -4 && ejey <= 3.0) && (ejez >= 2 && ejez <= 11.0)) {
                        System.out.println("Horizontal");
                        //myBitmapRotated = RotateBitmap(resizeBitmap, 0);
                        // myBitmapRotated = myBitmap;
                    } else if ((ejex >= -10 && ejex <= -1) && (ejey >= -1 && ejey <= 2.3) && (ejez >= 1 && ejez <= 10.3)) {
                        System.out.println("horizontal inversa");
                        myBitmap = RotateBitmap(myBitmap, 180);
                    } else if ((ejex <= 1.6 && ejex >= -1.5) && (ejey >= 1.5 && ejey <= 10) && (ejez >= 0 && ejez <= 10.3)) {
                        System.out.println("vertical");
                        myBitmap = RotateBitmap(myBitmap, 90);
                    } else if ((ejex >= -0.4 && ejex <= 0.4) && (ejey >= -10 && ejey <= -0.9) && (ejez >= 0 && ejez <= 10.5)) {
                        System.out.println("vertical inversa");
                        myBitmap = RotateBitmap(myBitmap, 270);
                    } else {
                        //myBitmapRotated = RotateBitmap(resizeBitmap, 0);
                        //myBitmap = myBitmap;
                    }

                    /*Deteccion de OSCURIDAD */
                    (findViewById(R.id.buttonNo)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.buttonNo)).setEnabled(true);

                    (findViewById(R.id.buttonRotate90pos)).setEnabled(true);
                    (findViewById(R.id.buttonRotateM90pos)).setEnabled(true);
                    button_capture.setImageResource(R.drawable.icon_circle_ok);

                    cropImageView.setVisibility(View.VISIBLE);
                    cropImageView.setImageBitmap(myBitmap);
                    camera_preview.setVisibility(View.INVISIBLE);
                    if (camera != null) {
                        camera.stopPreview();
                    }

                    Utils.flagOk = true;
                    (findViewById(R.id.button_capture)).setEnabled(true);
                    (findViewById(R.id.buttonNo)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.buttonRotate90pos)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.buttonRotateM90pos)).setVisibility(View.VISIBLE);
                }
            }
        };
    }

    private String convertBitmapToStringRedi(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int newWidth = (width * 50) / 100;
        int newHeight = (height * 50) / 100;

        System.out.println(newWidth + "/" + width);
        System.out.println(newHeight + "/" + height);

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap a = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        a.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byte_arr = stream.toByteArray();
        return Base64.encodeToString(byte_arr, Base64.DEFAULT);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        SGD.onTouchEvent(ev);
        return true;
    }

    private static Bitmap RotateBitmap(Bitmap source, float angle) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        } catch (Exception e) {
        }

        return source;
    }

    /* COMPROBAR SI TIENE FLASH */
    private boolean suportFlash() {
        boolean flash;
        try {
            flash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        } catch (Exception e) {
            flash = false;
        }
        return flash;
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            double scaleFactor = detector.getScaleFactor();
            if (1.0f > scaleFactor) {
                if (currentZoomLevel > 0) {
                    currentZoomLevel--;
                    parameters.setZoom(currentZoomLevel);
                    camera.setParameters(parameters);
                }
            } else {
                if (currentZoomLevel < maxZoomLevel) {
                    currentZoomLevel++;
                    //mCamera.startSmoothZoom(currentZoomLevel);
                    parameters.setZoom(currentZoomLevel);
                    camera.setParameters(parameters);
                }
            }
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (camera != null)
            camera.release();
        camera = getCameraInstance();
        if (camera == null) {
            Toast.makeText(this, "No es posible tomar la fotografía, la cámara no está disponible.   on resume", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        if (camera == null) {
            Toast.makeText(this, "No es posible tomar la fotografía, la cámara no está disponible.   on resume", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @Override
    public void onBackPressed() {

        try {
            if (myBitmap != null && !myBitmap.isRecycled()) {
                myBitmap.recycle();
                myBitmap = null;
            }

            Utils.progressDialog.dismiss();
            Utils.flagOk = false;
            finish();

            if (camera != null) {
                camera.stopPreview();
                camera.release();
                System.out.println("Sie es diferente de null en el back");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        return (result);
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setParameters(parameters);
            } catch (Throwable ignored) {
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                if (camera != null) {
                    parameters = camera.getParameters();
                }
                if (parameters != null) {
                    if (parameters.getSupportedFocusModes()
                            .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    }
                    Camera.Size size = getBestPreviewSize(width, height,
                            parameters);
                    if (size != null) {
                        parameters.setPreviewSize(size.width, size.height);
                        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
                        Camera.Size size2 = sizes.get(0);
                        for (int i = 0; i < sizes.size(); i++) {
                            if (sizes.get(i).width > size2.width)
                                size2 = sizes.get(i);
                        }

                        parameters.setPictureSize(size2.width, size2.height);
                        camera.setParameters(parameters);
                        System.out.println("===================Se establecieron lod parametros correctamente");
                        camera.setDisplayOrientation(90);
                        camera.startPreview();

                        if (parameters.isZoomSupported()) {
                            maxZoomLevel = parameters.getMaxZoom();
                        }

                        if (flagFlash) {
                            switch (Utils.tipoFlash) {
                                case 0:
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                    camera.setParameters(parameters);
                                    ((ImageView) findViewById(R.id.buttonFlash)).setImageResource(R.drawable.ic_flash_off_white_24dp);
                                    break;
                                case 1:
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                                    camera.setParameters(parameters);
                                    ((ImageView) findViewById(R.id.buttonFlash)).setImageResource(R.drawable.ic_flash_auto_white_24dp);
                                    break;
                                case 2:
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                    camera.setParameters(parameters);
                                    ((ImageView) findViewById(R.id.buttonFlash)).setImageResource(R.drawable.ic_flash_on_white_24dp);
                                    break;
                            }
                        }
                    }
                }
            }catch(Exception e){
                System.out.println("Exception surfaceChanged: " + e.toString());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0);
        } catch (Exception ignored) {
        }
        return c;
    }

    private Bitmap rediImagePorTamanio(Bitmap image, int porcentaje) {
        Bitmap reply = null;
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            int newWidth = (width * porcentaje) / 100;
            int newHeight = (height * porcentaje) / 100;

            System.out.println(newWidth + "/" + width);
            System.out.println(newHeight + "/" + height);

            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            reply = Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);

        } catch (Exception e) {
            System.out.println("Error al intentar reducir el tamaño de la imagen");
        } finally {
            Utils.freeMemory();
        }
        return reply;
    }

    private String getTipoFoto(String descrpcion) {
        String reply = "";
        SQLiteDatabase db = openOrCreateDatabase(DB.DB_NAME, Context.MODE_PRIVATE, null);
        try {
            Cursor c = db.rawQuery("SELECT " + DB.CAT_TF_ID +
                    " FROM " + DB.TABLE_NAME_TIPO_FOTO +
                    " WHERE " + DB.CAT_TF_DESCRIPCION + " ='" + descrpcion + "'", null);
            if (c.moveToFirst()) {
                reply = c.getString(0);
                System.out.println("Id encontrado =======" + reply);
            }
            c.close();
        } catch (Exception e) {
            System.out.println("Error al abrir la base de datos en la camara");

        }
        db.close();
        db = null;
        return reply;
    }

    private boolean existeFoto(String credito, String tipo) {
        boolean reply = false;
        SQLiteDatabase db = openOrCreateDatabase(DB.DB_NAME, Context.MODE_PRIVATE, null);

        try {
            Cursor c = db.rawQuery("SELECT " + DB.PR_MC_CREDITO +
                    " FROM " + DB.TABLE_NAME_IMAGENES +
                    " WHERE " + DB.PR_MC_CREDITO + "='" + credito + "' AND " + DB.CAT_PA_ID + "='" + tipo + "'", null);
            if (c.moveToFirst()) {
                reply = true;
                System.out.println("Id encontrado =======" + reply);
            }

            c.close();
        } catch (Exception e) {
            System.out.println("Error al abrir la base de datos en la camara");

        }
        db.close();
        db = null;

        return reply;
    }

    private boolean existeFotoAux(String credito, String tipo) {
        boolean reply = false;
        SQLiteDatabase db = openOrCreateDatabase(DB.DB_NAME, Context.MODE_PRIVATE, null);
        try {
            Cursor c = db.rawQuery("SELECT " + DB.PR_MC_CREDITO +
                    " FROM " + DB.TABLE_NAME_IMAGENES_AUX +
                    " WHERE " + DB.PR_MC_CREDITO + "='" + credito + "' AND " + DB.CAT_PA_ID + "='" + tipo + "'", null);
            if (c.moveToFirst()) {
                reply = true;
                System.out.println("Id encontrado =======" + reply);
            }
            c.close();
        } catch (Exception e) {
            System.out.println("Error al abrir la base de datos en la camara");

        }
        db.close();
        db = null;
        return reply;
    }

}
