package com.example.paulba.kwiktext;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by paulba on 23/05/16.
 */
public class History {


    private static ArrayList history;
    private static String statefile="";
    private static Context context;

    public History (Context c)
    {
        context=c;
        statefile = context.getFilesDir().getPath().toString() + "/history.txt";
    }

    public static void SaveMessages(){
        try{
            File templatesFile=new File(statefile);
            if(!templatesFile.exists())
                templatesFile.createNewFile();
            FileOutputStream fos=new FileOutputStream(statefile);
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            oos.writeObject(history);
            oos.close();
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void AddHistory(String message, String number)
    {

    }
}
