package edu.cmu.pocketsphinx.demo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;
import androidx.annotation.Nullable;
import android.util.Log;

public class MyDBHandler extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "speech2.db";
    public static final String TABLE = "Audio";
    public static final String COLUMN1 = "KEYWORD";
    public static final String COLUMN2 = "RING";
    public static final String COLUMN_ID = "_id";
    public MyDBHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE '" + TABLE + "'(KEYWORD TEXT,_id INTEGER PRIMARY KEY AUTOINCREMENT);";


        sqLiteDatabase.execSQL(query);
        ContentValues values = new ContentValues();
        values.put("KEYWORD", "yay");
        values.put("_id",1);
        sqLiteDatabase.insert(TABLE,null,values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(sqLiteDatabase);
    }
    public void addKeyword(String keyword){
        //Log.d("Result" , keyword);
        SQLiteDatabase db = getWritableDatabase();
        String query = "UPDATE '" + TABLE + "'SET '" + COLUMN1 + "'='" + keyword + "'WHERE '" + COLUMN_ID + "'=1";
        db.execSQL(query);

    }
    public String getKeyword(){
        String key = "";
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT '"+ COLUMN1 +"'FROM '" + TABLE + "'WHERE '"+COLUMN_ID+"'=1";
        Cursor cursor = db.rawQuery(query,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){

            key += cursor.getString(cursor.getColumnIndex(COLUMN1))!=null ? cursor.getString(cursor.getColumnIndex(COLUMN1)) : "" ;
            cursor.close();

        }
        db.close();
        Log.d("Result",key+"a");
        return key;
    }


}
