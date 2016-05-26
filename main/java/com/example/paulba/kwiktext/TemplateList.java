package com.example.paulba.kwiktext;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by paulba on 04/05/16.
 */
public class TemplateList implements Serializable{
    public static ArrayList<TemplateItem> templates;
    private transient Context context;
    private transient static String statefile;

    // Lists for each of the data items
    public ArrayList<String> MessageArray;
    public ArrayList<String> FriendlyNameArray;
    public ArrayList<String> NumberArray;
    public ArrayList<String> SendCountArray;
    public ArrayList<String> LastSentArray;

    public TemplateList(){
        // Try to load from disk
        templates=new ArrayList<TemplateItem>();

        String filePath = context.getFilesDir().getPath().toString() + "/templates.txt";
        statefile=filePath;
        if(LoadTemplates()==false) {
            LoadDefaults();
            SaveTemplates();
        }
    }

    public TemplateList(Context c)
    {
        templates=new ArrayList<TemplateItem>();
        context=c;

        String filePath = context.getFilesDir().getPath().toString() + "/templates.txt";
        statefile=filePath;
        if(!LoadTemplates()) {
            LoadDefaults();
            SaveTemplates();
        }
        FillIndivdualArrays();
    }

    public void SetContext(Context c)
    {
        context=c;
    }

    public void finalize() throws Throwable{
        SaveTemplates();
        super.finalize();
    }

    public static void SaveTemplates(){
        try{
            File templatesFile=new File(statefile);
            if(!templatesFile.exists())
                templatesFile.createNewFile();
            FileOutputStream fos=new FileOutputStream(statefile);
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
            FileInputStream fis=new FileInputStream(statefile);
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

    private void ClearAll()
    {
        templates.clear();
        MessageArray.clear();
        FriendlyNameArray.clear();
        NumberArray.clear();
        SendCountArray.clear();
        LastSentArray.clear();
    }

    private static void LoadDefaults(){
        System.out.println("Loading Defaults");
        TemplateItem newitem =new TemplateItem("On my way!");
        templates.add(newitem);
        newitem=new TemplateItem("Leaving in 5");
        templates.add(newitem);
        newitem=new TemplateItem("Leaving in 10");
        templates.add(newitem);
        newitem=new TemplateItem("In a meeting. All OK?");
        templates.add(newitem);
        newitem=new TemplateItem("Need anything home");
        templates.add(newitem);

    }

    private void FillIndivdualArrays()
    {
        if(MessageArray!=null)
            MessageArray.clear();
        else
            MessageArray=new ArrayList<>();

        if(FriendlyNameArray!=null)
            FriendlyNameArray.clear();
        else
            FriendlyNameArray=new ArrayList<>();

        if(NumberArray!=null)
            NumberArray.clear();
        else
            NumberArray=new ArrayList<>();

        if(SendCountArray!=null)
            SendCountArray.clear();
        else
            SendCountArray=new ArrayList<>();

        if(LastSentArray!=null)
            LastSentArray.clear();
        else
            LastSentArray=new ArrayList<>();


        for (TemplateItem ti: templates)
        {
            MessageArray.add(ti.Message);
            FriendlyNameArray.add(ti.FriendlyName);
            NumberArray.add(ti.Number);
            SendCountArray.add("");
            if(ti.LastSent!=null)
                LastSentArray.add(ti.LastSent.toString());
            else
                LastSentArray.add("Never sent");
        }
    }

    public void ResetToDefaults()
    {
        ClearAll();
        LoadDefaults();
        FillIndivdualArrays();
        SaveTemplates();
    }

    public boolean AddNew(TemplateItem ti, boolean AddToHead)
    {
        if(AddToHead)
        {
            templates.add(0,ti);
        }
        else
        {
            templates.add(ti);
        }
        SaveTemplates();
        FillIndivdualArrays();
        return true;
    }

    public boolean DeleteItem(int index, String message)
    {
        try
        {
            if(index>=templates.size()) // outside the array
                return false;
            if(!MessageArray.get(index).equals(message)) // string does not match
                return false;
            // All ok at this point
            templates.remove(index);
            SaveTemplates();
            FillIndivdualArrays();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean EditItem(int index, String originalMessage, String message, String name, String number)
    {
        try
        {
            // Make sure it is in within the bounds of the array
            if(index>templates.size()-1)
                return false;
            // Make sure the original message is the same
            String storedMessage=MessageArray.get(index);
            if(!storedMessage.equals(originalMessage))
                return false;

            // OK we are good to go now
            TemplateItem ti=templates.remove(index);
            ti.Number=number;
            ti.Message=message;
            ti.FriendlyName=name;
            templates.add(index,ti);
            SaveTemplates();
            FillIndivdualArrays();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
