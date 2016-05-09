package com.example.paulba.kwiktext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by paulba on 04/05/16.
 */
public class TemplateList implements Serializable{
    public static ArrayList<TemplateItem> templates;

    public void TemplateList(){
        // Try to load from disk
        if(LoadTemplates()==false) {
            LoadDefaults();
        }
    }

    public void finalize() throws Throwable{
        SaveTemplates();
        super.finalize();
    }

    public static void SaveTemplates(){
        try{
            FileOutputStream fos=new FileOutputStream("templates");
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            oos.writeObject(templates);
            oos.close();
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static boolean LoadTemplates(){
        try{
            FileInputStream fis=new FileInputStream("templates");
            ObjectInputStream ois=new ObjectInputStream(fis);
            templates=(ArrayList<TemplateItem>)ois.readObject();
            ois.close();
            fis.close();
            System.out.println("Templates loaded");
            return true;
        }catch(IOException ioe) {
            ioe.printStackTrace();
            return false;
        }catch(ClassNotFoundException cnfe){
            System.out.println("Class not found");
            cnfe.printStackTrace();
            return false;
        }
    }

    private static void LoadDefaults(){
        System.out.println("Loading Defaults");
        TemplateItem newitem =new TemplateItem("On my way!");
        templates.add(newitem);
        newitem.Message ="Leaving in 5";
        templates.add(newitem);
        newitem.Message ="Leaving in 10";
        templates.add(newitem);
        newitem.Message ="In a meeting. All ok?";
        templates.add(newitem);
        newitem.Message ="Need anything home?";
        templates.add(newitem);

    }

}
