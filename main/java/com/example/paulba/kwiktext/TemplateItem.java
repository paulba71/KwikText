package com.example.paulba.kwiktext;

import android.graphics.Bitmap;
import android.media.Image;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

/**
 * Created by paulba on 04/05/16.
 */
public class TemplateItem implements Serializable {
    public String Message;
    public String FriendlyName;
    public String Number;
    public int SendCount;
    public Date LastSent;


    public TemplateItem()
    {
        Message="";
        FriendlyName="";
        Number="";
        SendCount=0;
        LastSent=null;

    }

    public TemplateItem(String message)
    {
        Message=message;
        FriendlyName="Choose recipient from contacts";
        Number="No Number";
        SendCount=0;
        LastSent=null;
    }
}
