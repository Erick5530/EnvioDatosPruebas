package ios.com.enviodedatos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DB.TABLE_NAME_IMAGENES + "(" +
                DB.PR_MC_CREDITO + " TEXT, " +
                DB.FECHA_VISITA + " TEXT, " +
                DB.CAT_PA_ID + " INTEGER, " +
                DB.IMAGEN + " TEXT, " +
                DB.V_TMP_ID + " TEXT, " +
                DB.V_TMP_USUARIO + " TEXT, " +
                "NOMBRE_IMAGEN" + " TEXT);"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
