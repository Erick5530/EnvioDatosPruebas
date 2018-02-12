package ios.com.enviodedatos;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO code application logic here


        //String path = "C:\\Users\\Public\\FILEANDROID\\prueba.txt";
        // String path = "C:\\Users\\Public\\FILEANDROID\\4k.jpg";

        String path = Environment.getExternalStorageDirectory()+"/mcPruebas/4k.jpg";

        final File fl = new  File(path);

        final SendFile sf = new SendFile("http://mhprojects.esy.es/php/upload.php",fl.getName());
        // SendFile sf = new SendFile("https://pruebasmc.com.mx/WsMovilPruebas/api/RecibeArchivo","4k.jpg");
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    sf.doStart(new FileInputStream(fl.getPath()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
