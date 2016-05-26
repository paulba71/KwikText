package com.example.paulba.kwiktext;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.example.paulba.kwiktext.R;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by paulba on 24/05/16.
 */
public class ContactsHelper {

    private static final String DEBUG_TAG="Debug Message";

    public String GetNumber(Context c, Intent data)
    {
        String strNumber="";
        Cursor cursor=null;
        try {
            Uri result = data.getData();
            Log.v(DEBUG_TAG, "Got a result: "
                    + result.toString());
            // get the contact id from the Uri
            String id = result.getLastPathSegment();

            // query for everything email
            cursor = c.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                    new String[]{id}, null);

            int numberIdx=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);


            // let's just get the first email
            if (cursor.moveToFirst()) {
                strNumber=cursor.getString(numberIdx);

                Log.v(DEBUG_TAG, "Got number: " + strNumber);
            } else {
                Log.w(DEBUG_TAG, "No results");
            }
        }
        catch (Exception e) {
            Log.e(DEBUG_TAG, "Failed to get email data", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return strNumber;
    }

    public Bitmap GetContactsPicture(Context context, String address) {
        Bitmap bp = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.defaultpersonicon);
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + address + "'";
        Cursor phones = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection,
                null, null);
        while (phones.moveToNext()) {
            String image_uri = phones.getString(phones.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

            if (image_uri != null) {
                try {
                    bp = MediaStore.Images.Media
                            .getBitmap(context.getContentResolver(),
                                    Uri.parse(image_uri));

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return bp;
    }

}
