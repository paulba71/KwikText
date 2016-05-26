package com.example.paulba.kwiktext;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final String DEBUG_TAG="Debug Message";

    private static final int CONTEXTMENU_LV_SEND=1001;
    private static final int CONTEXTMENU_LV_EDIT=1002;
    private static final int CONTEXTMENU_LV_DELETE=1003;

    public static TemplateList templates;
    ListView lv;

    private String sendtoNumber="";
    private String sendMessage="";

    private static int selectedItemPos;

    private boolean AlertResult;



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onResume() {
        // Refresh the listview
        // Its a bit of a hack but it looks like I need to create a new adaptor and set that to the list view...
        MainListAdaptor adapter = new MainListAdaptor(this, templates, templates.MessageArray);
        lv.setAdapter(adapter);

        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialise the list of templates
        templates = new TemplateList(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    //        .setAction("Action", null).show();
                    Intent addNewIntent=new Intent(getApplicationContext(), AddNewActivity.class);

                    startActivity(addNewIntent);
                }
            });
        }

        // Initialise and fill the list view
        lv = (ListView) findViewById(R.id.listViewMain);

        MainListAdaptor adaptor = new MainListAdaptor(this, templates, templates.MessageArray);

        lv.setAdapter(adaptor);

        // Listview onclick handler
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendMessage = templates.MessageArray.get(position);
                sendtoNumber = "";
                String num = templates.NumberArray.get(position);
                if (num.contains("No Number") || num.isEmpty()) {
                    // enter a number or pick a contact
                    Toast.makeText(MainActivity.this, "Please choose a contact to send the SMS to", Toast.LENGTH_LONG).show();
                    doLaunchContactPicker(lv);
                } else {
                    sendtoNumber = templates.NumberArray.get(position);
                }

                if (!sendtoNumber.isEmpty() && !sendMessage.isEmpty()) {
                    SendSMS(sendMessage, sendtoNumber);
                }
            }
        });


        registerForContextMenu(lv);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo)
    {
        if (v.getId()==R.id.listViewMain)
        {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            String msg="";

            menu.setHeaderTitle("Template Item");
            menu.setHeaderIcon(android.R.drawable.ic_menu_set_as);
            // TODO - not supporting send right now
            //menu.add(Menu.NONE, CONTEXTMENU_LV_SEND,0, "Send");
            menu.add(Menu.NONE, CONTEXTMENU_LV_EDIT,0, "Edit");
            // only show delete if there are more than one items
            Adapter adapter=lv.getAdapter();
            // Set the menu title to the trunctated message...
            int itempos=info.position;
            if(itempos!=-1) {
                String itemtext = templates.MessageArray.get(itempos);
                menu.setHeaderTitle(itemtext);
            }
            if(adapter.getCount()>1)
                menu.add(Menu.NONE, CONTEXTMENU_LV_DELETE, 1, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        int itempos=(int)info.id;
        String itemtext=templates.MessageArray.get(itempos);
        String itemnumber=templates.NumberArray.get(itempos);
        String itemname=templates.FriendlyNameArray.get(itempos);
        if(menuItemIndex==CONTEXTMENU_LV_DELETE)
        {
            DeleteItem(itempos,itemtext);
        }

        if(menuItemIndex==CONTEXTMENU_LV_EDIT)
        {
            // Use the addactivity to edit the template too
            Intent addNewIntent=new Intent(getApplicationContext(), AddNewActivity.class);
            addNewIntent.putExtra("INTENT_MODE","MODE_EDIT");
            addNewIntent.putExtra("TEMPLATE_INDEX",itempos);
            addNewIntent.putExtra("TEMPLATE_MESSAGE",itemtext);
            addNewIntent.putExtra("TEMPLATE_NUMBER",itemnumber);
            addNewIntent.putExtra("TEMPLATE_NAME",itemname);
            startActivity(addNewIntent);
        }

        return true;
    }



    // Show the contacts picker if there is no number specified for the item
    public void doLaunchContactPicker(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    // Handle the results of the contact picker and get the phone number...
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Cursor cursor=null;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:

                    try {
                        Uri result = data.getData();
                        Log.v(DEBUG_TAG, "Got a result: "
                                + result.toString());
                        // get the contact id from the Uri
                        String id = result.getLastPathSegment();

                        // query for everything email
                        cursor = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                                new String[]{id}, null);

                        int numberIdx=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);


                        // let's just get the first email
                        if (cursor.moveToFirst()) {
                            sendtoNumber=cursor.getString(numberIdx);

                            Log.v(DEBUG_TAG, "Got number: " + sendtoNumber);
                            SendSMS(sendMessage,sendtoNumber);
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
                        break;
                    }
            }

        } else {
            // gracefully handle failure
            Log.w(DEBUG_TAG, "Warning: activity result not ok");
        }
    }

    private void SendSMS(String message, String number)
    {
        if(!message.isEmpty() && !number.isEmpty()) {
            // Send the SMS
            //TODO check if sure behind an option
            SmsManager sms= SmsManager.getDefault();
            try
            {
                sms.sendTextMessage(number,null,message,null,null);
                String confirmation = "SMS message: \n" + message + " \nhas been sent to: \n" + number;
                Toast.makeText(MainActivity.this, confirmation, Toast.LENGTH_SHORT).show();
                // TODO - Increment the send count
                // TODO - Add the last sent date
                // TODO - Add to history
            }
            catch(Exception ex)
            {
                Toast.makeText(MainActivity.this, "SMS failed to send. Try again (perhaps)", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void DeleteItem(int index, String text)
    {
        // ToDo check if sure behind an option
        if(templates.DeleteItem(index,text))
        {
            String str="Deleting - " + text;
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
            onResume();
        }
        else
        {
            Toast.makeText(MainActivity.this, "Problem deleting the template", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(id == R.id.action_newtemplate)
        {
            Intent addNewIntent=new Intent(getApplicationContext(), AddNewActivity.class);
            startActivity(addNewIntent);
        }

        if(id==R.id.action_resetlist)
        {
            onResetTemplates();
        }

        if(id==R.id.action_about)
        {
            Intent aboutIntent=new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(aboutIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onResetTemplates()
    {
        // Check if sure first...
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to reset to defaults? You will lose any customisation or other changes you may have made.");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //Yes button clicked - restore to defaults
                templates.ResetToDefaults();
                // Redraw the view...
                onResume();

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.paulba.kwiktext/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.paulba.kwiktext/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}

class MainListAdaptor extends ArrayAdapter<String>
{
    Context context;
    TemplateList items;
    ArrayList<String> strings;

    MainListAdaptor(Context c, TemplateList tl, ArrayList<String> messages)
    {
        super(c,R.layout.single_row,R.id.messagetextview,messages);
        this.context=c;
        this.items=tl;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row=inflater.inflate(R.layout.single_row,parent,false);

        ImageView rowImage=(ImageView) row.findViewById(R.id.imageView);
        TextView messageText=(TextView) row.findViewById(R.id.messagetextview);
        TextView nameText= (TextView) row.findViewById(R.id.frieldnlynametextview);

        String message=items.MessageArray.get(position);
        messageText.setText(message);
        String friendlyname=items.FriendlyNameArray.get(position);
        nameText.setText(friendlyname);
        String number= items.NumberArray.get(position);
        // TODO: make this an option...
        ContactsHelper ch=new ContactsHelper();
        Bitmap bmp=ch.GetContactsPicture(parent.getContext(),number);
        //Bitmap bmp=getContactsDetails(parent.getContext(),number);
        if(bmp!=null) {
            // Scale it to 48 by 48
            Bitmap scaled=Bitmap.createScaledBitmap(bmp,200,200,true);
            // Now make a circular cut out
            Bitmap cutout=getCircularBitmap(scaled);
            rowImage.setImageBitmap(cutout);
        }
        return row;
    }

    public static  Bitmap getContactsDetails(Context context, String address) {
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

    public static Bitmap getCircularBitmap(Bitmap bitmap)
    {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public void updateList()
    {
        this.notifyDataSetChanged();
    }

    public void updateList(TemplateList tl)
    {
        items=tl;
        //clear();
        //addAll(tl.MessageArray);
        this.notifyDataSetChanged();
    }

    public void addItem(String item)
    {
        add(item);
    }
}
