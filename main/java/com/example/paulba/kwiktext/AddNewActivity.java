package com.example.paulba.kwiktext;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



public class AddNewActivity extends AppCompatActivity {

    private TemplateList templates=null;

    private static final int CONTACT_PICKER_RESULT = 1001;
    private static final String DEBUG_TAG="Debug Message";
    private static int editindex;
    private static String originalmessage;
    private static boolean modeEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        editindex=-1;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);

        templates=MainActivity.templates;
        int templateCount=templates.MessageArray.size();

        Bundle extras=getIntent().getExtras();
        if(extras != null) // Edit mode not add mode
        {
            Button btnMain=(Button)findViewById(R.id.buttonAdd);
            btnMain.setText("Save");
            TextView title=(TextView)findViewById(R.id.textViewMode);
            title.setText("Edit Template");

            // Get the values passed int
            int itemIndex=extras.getInt ("TEMPLATE_INDEX");
            String itemtext=extras.getString("TEMPLATE_MESSAGE");
            String itemnumber=extras.getString("TEMPLATE_NUMBER");
            String itemname=extras.getString("TEMPLATE_NAME");

            if(itemname.equals("Choose recipient from contacts"))
                itemname="";
            if(itemnumber.equals("No Number"))
                itemnumber="";

            // Fill out the UX with them
            editindex=itemIndex;
            EditText etMessage=(EditText)findViewById(R.id.editMessage);
            EditText etName=(EditText)findViewById(R.id.editName);
            EditText etNumber=(EditText)findViewById(R.id.editNumber);

            etMessage.setText(itemtext);
            originalmessage=itemtext;
            etName.setText(itemname);
            etNumber.setText(itemnumber);

            modeEdit=true;

        }
        else
        {
            Button btnMain=(Button)findViewById(R.id.buttonAdd);
            btnMain.setText("Add");
            TextView title=(TextView)findViewById(R.id.textViewMode);
            title.setText("Add New Template");

            modeEdit=false;
        }
    }

    public void SetTemplatesList(TemplateList t)
    {
        templates=t;
    }

    public void onCancelButtonClick(View v)
    {
        onBackPressed();
    }

    public void onChooseFromContactsButtonClick(View v)
    {
        doLaunchContactPicker(v);
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
                        int nameIdx= cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);


                        // let's just get the first email
                        if (cursor.moveToFirst()) {
                            String sendtoNumber=cursor.getString(numberIdx);
                            String sendToName=cursor.getString(nameIdx);

                            Log.v(DEBUG_TAG, "Got number: " + sendtoNumber);
                            EditText etNumber=(EditText)findViewById(R.id.editNumber);
                            etNumber.setText(sendtoNumber, TextView.BufferType.EDITABLE);
                            EditText etName=(EditText)findViewById(R.id.editName);
                            etName.setText(sendToName,TextView.BufferType.EDITABLE);
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

    public void onAddButtonClick(View v)
    {
        boolean canEdit=false;
        EditText etMessage=(EditText)findViewById(R.id.editMessage);
        EditText etName=(EditText)findViewById(R.id.editName);
        EditText etNumber=(EditText)findViewById(R.id.editNumber);
        CheckBox cbAddToHead=(CheckBox)findViewById(R.id.checkBoxFirst);

        String msg=etMessage.getText().toString();
        String name=etName.getText().toString();
        if(name.isEmpty())
            name="Choose recipient from contacts";
        String number=etNumber.getText().toString();
        if(number.isEmpty())
            number="No Number";
        boolean addToHead= cbAddToHead.isChecked();

        if(!msg.isEmpty() /*&& !name.isEmpty()*/ )
            canEdit=true;

        if(canEdit && !modeEdit)    // Add mode
        {
            //Toast.makeText(AddNewActivity.this, "Add", Toast.LENGTH_SHORT).show();
            TemplateItem ti = new TemplateItem();

            ti.Message=msg;
            ti.Number = number;
            ti.FriendlyName=name;
            ti.SendCount = 0;
            ti.LastSent=null;

            templates.AddNew(ti,addToHead);

            onBackPressed();
        }

        if(canEdit && modeEdit)
        {
            if(!msg.isEmpty()) {
                templates.EditItem(editindex, originalmessage,msg,name,number );
            }
            onBackPressed();
        }
    }
}
