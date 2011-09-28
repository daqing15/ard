package com.mixed.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.zxing.client.androidtest.R;
import com.mixed.entity.ContactInfo;

public class ContactMainActivity extends Activity {

	private static final int DIALOG_KEY = 0;
	SimpleAdapter adapter; // 数据存储Adapter
	ContactInfoAdapter contactAdapter;
	private static final String TAG = "ContactMainActivity";
	protected static final int REQUEST_SUC_CODE = 1;

	private ArrayList<ContactInfo> contactList = new ArrayList<ContactInfo>();

	private LinearLayout mainLinearLayout;
	
	private TelephonyManager telephonyManager;
	
	// 所有联系人的ListView
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact);

		listView = (ListView) findViewById(R.id.lv_userlist);

		// 获取通讯录的线程
		new GetContactTask().execute("");

		//List时间监听
		listView.setOnItemClickListener(new OnItemClickListener() {
			/**
			 * List每一项被选中时触发的事件
			 */
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(ContactMainActivity.this,
						ContactDetailActivity.class);
				ContactInfo info = (ContactInfo) arg0.getItemAtPosition(arg2);
				intent.putExtra("ContactInfo", info);
				Toast.makeText(ContactMainActivity.this, info.userName,
						Toast.LENGTH_SHORT);
				startActivityForResult(intent, REQUEST_SUC_CODE);
			}
		});

		mainLinearLayout = (LinearLayout) findViewById(R.id.list_ll);
		// adapter = new SimpleAdapter(this, contactList, R.layout.contact_item,
		// new String[]{"userName", "phone"},
		// new int[]{R.id.userName, R.id.phone});

	}

	private class GetContactTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			getLocalContact(); // 本地通讯录
			getSIMContact("content://icc/adn"); //SIM通讯录common use
			getSIMContact("content://sim/adn"); //SIM通讯录
			return "";
		}

		@Override
		protected void onPreExecute() {
			showDialog(DIALOG_KEY);
		}

		/*
		 *
		 */
		@Override
		public void onPostExecute(String re) {
			if (contactList.size() == 0) {
				Drawable dr = getResources().getDrawable(R.drawable.nodata_bg);
				mainLinearLayout.setBackgroundDrawable(dr);
				setTitle("联系人");
			} else {
				contactAdapter = new ContactInfoAdapter(contactList,
						ContactMainActivity.this);
				listView.setAdapter(contactAdapter);
				listView.setTextFilterEnabled(true);
			}
			removeDialog(DIALOG_KEY);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_KEY:
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("正获取联系人...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		}
		return null;
	}

	/**
	 * 本地通讯录
	 */
	public void getLocalContact() {
		// 
		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
			//contentResolver.query(Contacts.People.CONTENT_URI, null, null, null, null);
		while (cursor.moveToNext()) {
			ContactInfo cci = new ContactInfo();
			
			String cid = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			if(Integer.parseInt(hasPhone) > 0){
				Cursor pC = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + cid, null, null);
				while(pC.moveToNext()){
					String phoneNumber = pC.getString(pC.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//					cci.phone += phoneNumber +"-";
					cci.phone = phoneNumber;
					break;
				}
				pC.close();
			}
			
			
			// 联系人姓名
			final int nameIndex = cursor.getColumnIndex(People.DISPLAY_NAME);
			cci.userName = cursor.getString(nameIndex);
			
			//联系人ID
//			long contactId = cursor.getLong(cursor.getColumnIndex(Contacts.People._ID));
//			int numberFieldColumnIndex = cursor.getColumnIndex("number");
//			String ph = cursor.getString(numberFieldColumnIndex);
//			
//			// 联系人电话
//			Cursor phtonCur = contentResolver.query(
//					Contacts.People.CONTENT_URI, null, 
//					Contacts.People._ID + "=" + contactId, null, null);
//			while(phtonCur.moveToNext()){
//				final int phoneIdx = phtonCur.getColumnIndex(Contacts.People.NUMBER);
//				if(phtonCur.getString(phoneIdx) != null){
//					cci.phone = phtonCur.getString(phoneIdx);
//					break;
//				}
//			}
//			phtonCur.close();
			if(cci.phone == null || "".equals(cci.phone)){
				cci.phone = "18933252879";
			}
			cci.phone = restorePhone(cci.phone);

//			final String[] PROJECTION = new String[] {
//					Contacts.ContactMethods._ID, Contacts.ContactMethods.KIND, Contacts.ContactMethods.DATA };
			Cursor emailCur = contentResolver.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=\'" + cid + "\'", null, null);
			while (emailCur.moveToNext()) 
			{                 
				String emailAddress = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				cci.email = emailAddress;
			}  
			emailCur.close();
//			while (emailCur.moveToNext()) {
//				String emailAddress = emailCur.getString(emailCur
//						.getColumnIndex(Contacts.ContactMethods.DATA));
//				if(emailAddress != null){
//					cci.email = emailAddress;
//					break;
//				}
//			}

//			 final int emailIdx =
//			 cursor.getColumnIndex(People.PRIMARY_EMAIL_ID);
//			 cci.email = cursor.getString(emailIdx);

			if (isUserPhone(cci.phone)) {
				contactList.add(cci);
			}
		}
		System.out.println(contactList.size());
		cursor.close();
	}

	private boolean IsContain(ArrayList<ContactInfo> list, String un) {
		for (int i = 0; i < list.size(); i++) {
			if (un.equals(list.get(i).phone)) {
				return true;
			}
		}
		return false;
	}

	private boolean isUserPhone(String phone) {
		boolean flag = false;
		if (phone.length() == 11) {
			if (phone.startsWith("13") || phone.startsWith("15")
					|| phone.startsWith("18")) {
				flag = true;
			}
		}
		return flag;
	}

	/*
	 */
	private String restorePhone(String phone) {
		if (phone != null) {
			if (phone.startsWith("+86")) {
				phone = phone.substring(3);
			} else if (phone.startsWith("86")) {
				phone = phone.substring(2);
			}
		} else {
			phone = "";
		}
		return phone;
	}

	/**
	 * 
	 * @param string
	 */
	public void getSIMContact(String url) {
		Cursor cursor = null;
		try {
			Intent intent = new Intent();
			intent.setData(Uri.parse(url));
			Uri uri = intent.getData();
			cursor = getContentResolver().query(uri, null, null, null, null);
			while (cursor.moveToNext()) {
				ContactInfo cci = new ContactInfo();
				final int nameIndex = cursor.getColumnIndex(People.NAME);
				cci.userName = cursor.getString(nameIndex);

				final int phoneIdx = cursor.getColumnIndex(People.NUMBER);
				cci.phone = cursor.getString(phoneIdx);
				cci.phone = restorePhone(cci.phone);
				if (isUserPhone(cci.phone) && IsContain(contactList, cci.phone)) {
					contactList.add(cci);
				}
			}
			cursor.close();
		} catch (Exception ex) {
			if (cursor != null) {
				cursor.close();
			}
			Log.i(TAG, "从SIM路径" + url + "获取联系人失败");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
