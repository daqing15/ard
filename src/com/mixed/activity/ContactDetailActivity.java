package com.mixed.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.Contacts.GroupMembership;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.google.zxing.client.androidtest.R;
import com.mixed.entity.ContactInfo;

/**
 * 联系人的详细页面
 * @author chendaqing
 *
 */
public class ContactDetailActivity extends Activity {
	
	private static final String TAG = ContactDetailActivity.class.getSimpleName();
	private static final int MAX_BARCODE_FILENAME_LENGTH = 24;
	
	private QRCodeEncoder qrCodeEncoder;
	
	protected ContactInfo contact;
	
	Button btn_createTwoCode;
	Button btn_scanQrCode;
	Button btn_goback;
	View twoCodeView = null;
	AlertDialog twoCodeDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_detail);
		
		btn_createTwoCode = (Button) findViewById(R.id.btn_createTwoCode);
		btn_goback = (Button) findViewById(R.id.btn_goback);
		btn_scanQrCode = (Button) findViewById(R.id.btn_scanQrCode);
		
		LayoutInflater inflater = LayoutInflater.from(this);
		twoCodeView = inflater.inflate(R.layout.two_code_info, null);
		
		//获取传递过来的数据
		Intent intent = getIntent();
		contact = (ContactInfo) intent.getSerializableExtra("ContactInfo");
		
		initButtonListener();
	}

	private void initButtonListener() {
		btn_createTwoCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Bundle bundle = new Bundle();
			    //TODO 把联系人信息放到上下文中
			    bundle.putString(Contacts.Intents.Insert.NAME, contact.userName);
			    bundle.putString(Contacts.Intents.Insert.PHONE, contact.phone);
			    bundle.putString(Contacts.Intents.Insert.EMAIL, contact.email);
			    bundle.putSerializable("ContactInfo", contact);
			    encodeBarcode("CONTACT_TYPE", bundle);
				
//				if(twoCodeDialog == null){
//					Builder builder = new AlertDialog.Builder(ContactDetailActivity.this);
//					builder.setTitle(contact.userName + "����Ƭ��ά��");
//					builder.setView(twoCodeView);
//					
//					builder.setPositiveButton("����", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							
//							Toast.makeText(ContactDetailActivity.this, "��ά��", Toast.LENGTH_LONG).show();
//						}
//					});
//					
//					builder.setNegativeButton("����", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.cancel();
//						}
//					});
//					twoCodeDialog = builder.create();
//				}
				//twoCodeDialog.show();
			}
		});
		
		btn_goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		btn_scanQrCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO扫描Activity
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			    startActivityForResult(intent, 0);
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    if (requestCode == 0) {
	          if (resultCode == RESULT_OK) {
	        	  String contents = intent.getStringExtra(Intents.Scan.RESULT);
	        	  Log.i(TAG, contents);
			      if(contents != null && !"".equals(contents)){
			    	  String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
			    	  showDialog(R.string.result_succeeded, "格式：" + format + "\n内容" + contents, contents);
			      }
		      } else if (resultCode == RESULT_CANCELED) {
		    	  showDialog(R.string.result_failed, getString(R.string.result_failed_why), null);
		      }
	      } /*else {
	    	  showDialog(R.string.result_failed, getString(R.string.result_failed_why));
	      }*/
	}
	
	private void showDialog(int title, final CharSequence message, final String contents) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(title);
	    builder.setMessage(message);
	    if(contents!=null && message.toString().indexOf("取消") == -1){//&& contents.toString().indexOf("ȡ��") == -1
		    builder.setPositiveButton("增加进通讯录", new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int which) {
		    		String paramCode = contents;
		    		String[] params = paramCode.split(";");
		    		ContactInfo info = new ContactInfo();
		    		for(String param : params){
		    			 //MECARD:N:����Ժ;TEL:18966666666;EMAIL:dsx@it.com;;
		    			 if(param.indexOf("N:") != -1){
		    				 String[] ss = param.split(":");
		    				 info.userName = ss[2];
		    			 }
		    			 if(param.indexOf("TEL") != -1){
		    				 String[] ss = param.split(":");
		    				 info.phone = ss[1];
		    			 }
		    			 if(param.indexOf("EMAIL") != -1){
		    				 String[] ss = param.split(":");
		    				 info.email = ss[1];
		    			 }
		    		}
		    		ContentValues cttValues = new ContentValues();
		    		cttValues.put(Contacts.People.NAME, info.userName);
		    		cttValues.put(Contacts.People.STARRED, 1);
		    		Uri newUri = getContentResolver().insert(Contacts.People.CONTENT_URI, cttValues);
		    		if(newUri != null){
		    			//ADD INTO Group
		    			long pid = ContentUris.parseId(newUri);
		    			ContentValues group = new ContentValues();
		    			group.put(GroupMembership.PERSON_ID, pid);
		    			group.put(GroupMembership.GROUP_ID, 1);
		    			//Uri groupUpd = 
		    			getContentResolver().insert(GroupMembership.CONTENT_URI, group);
		    			
		    			//电话
		    			if(info.phone != null){
		    				ContentValues mobileValues = new ContentValues(); 
		                    Uri mobileUri = Uri.withAppendedPath(newUri, 
		                            Contacts.People.Phones.CONTENT_DIRECTORY); 
		                    mobileValues.put(Contacts.Phones.NUMBER, info.phone); 
		                    mobileValues.put(Contacts.Phones.TYPE, Contacts.Phones.TYPE_MOBILE); 
		                    //Uri phoneUpdate = 
		                    getContentResolver().insert(mobileUri, mobileValues); 
		    			}
		    			
		    			//邮箱
		    			if(info.email != null){
		    				ContentValues emailValues = new ContentValues(); 
		    				Uri emailUri = Uri.withAppendedPath(newUri, 
		    						Contacts.People.ContactMethods.CONTENT_DIRECTORY); 
		    				emailValues.put(Contacts.ContactMethods.KIND, Contacts.KIND_EMAIL); 
		    				emailValues.put(Contacts.ContactMethods.TYPE, Contacts.ContactMethods.TYPE_WORK); 
		    	            emailValues.put(Contacts.ContactMethods.DATA, info.email); 
		    	            //Uri emailUpdate = 
		    	            getContentResolver().insert(emailUri, emailValues); 
		    			}
		    			
		    			//公司
		    			if(info.company != null){
		    				ContentValues orgValue = new ContentValues();
		    				Uri orgUri = Uri.withAppendedPath(
		    						newUri, Contacts.Organizations.CONTENT_DIRECTORY);
		    				orgValue.put(Contacts.Organizations.COMPANY, info.company);
		    				orgValue.put(Contacts.Organizations.TYPE, Contacts.Organizations.TYPE_WORK);
		    				//Uri orgUpd = 
		    				getContentResolver().insert(orgUri, orgValue);
		    			}
		    			
		    			//地址ַ
		    			if(info.address != null){
		    			    ContentValues addressValues = new ContentValues(); 
		                    Uri addressUri = Uri.withAppendedPath(newUri, Contacts.People.ContactMethods.CONTENT_DIRECTORY); 
		                    addressValues.put(Contacts.ContactMethods.KIND, Contacts.KIND_POSTAL); 
		                    addressValues.put(Contacts.ContactMethods.TYPE, Contacts.ContactMethods.TYPE_HOME); 
		                    addressValues.put(Contacts.ContactMethods.DATA, info.address); 
		                    //Uri addressUpdate = 
		                    getContentResolver().insert(addressUri,	addressValues); 
		    			}
		    		}
		    		
		    	}
			});
	    } else {
	    	builder.setPositiveButton("确定", null);
	    }
	    builder.show();
	  }
	
//	

	private void encodeBarcode(String type, Bundle data) {
	    Intent intent = new Intent(Intents.Encode.ACTION);//"com.google.zxing.client.android.ENCODE"
	    intent.putExtra(Intents.Encode.TYPE, type);//"ENCODE_TYPE"
	    intent.putExtra(Intents.Encode.DATA, data);//"ENCODE_DATA"
	    startActivity(intent);
	}
	
//	 @Override
//	  protected void onResume() {
//	    super.onResume();
//	    // This assumes the view is full screen, which is a good assumption
//	    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
//	    Display display = manager.getDefaultDisplay();
//	    int width = display.getWidth();
//	    int height = display.getHeight();
//	    int smallerDimension = width < height ? width : height;
//	    smallerDimension = smallerDimension * 7 / 8;
//
//	    Intent intent = getIntent();
//	    intent.setAction(Intents.Encode.ACTION);
//	    try {
//	      qrCodeEncoder = new QRCodeEncoder(this, intent, smallerDimension);
//	      setTitle(getString(R.string.app_name) + " - " + qrCodeEncoder.getTitle());
//	      Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
//	      ImageView view = (ImageView) findViewById(R.id.image_view);
//	      view.setImageBitmap(bitmap);
//	      TextView contents = (TextView) findViewById(R.id.contents_text_view);
//	      contents.setText(qrCodeEncoder.getDisplayContents());
//	    } catch (WriterException e) {
//	      Log.e(TAG, "Could not encode barcode", e);
//	      showErrorMessage(R.string.msg_encode_contents_failed);
//	      qrCodeEncoder = null;
//	    } catch (IllegalArgumentException e) {
//	      Log.e(TAG, "Could not encode barcode", e);
//	      showErrorMessage(R.string.msg_encode_contents_failed);
//	      qrCodeEncoder = null;
//	    }
//	  }
//	protected boolean generateQRCode() {
//		if (qrCodeEncoder == null) { // Odd
//			Log.w(TAG, "No existing barcode to send?");
//			return true;
//		}
//		String contents = qrCodeEncoder.getContents();
//		Bitmap bitmap;
//		try {
//			bitmap = qrCodeEncoder.encodeAsBitmap();
//		} catch (WriterException we) {
//			Log.w(TAG, we);
//			return true;
//		}
//		File bsRoot = new File(Environment.getExternalStorageDirectory(), "BarcodeScanner");
//		File barcodesRoot = new File(bsRoot, "TwoCodeContact");
//		if (!barcodesRoot.exists() && !barcodesRoot.mkdirs()) {
//			Log.w(TAG, "Couldn't make dir " + barcodesRoot);
//			showErrorMessage(R.string.msg_unmount_usb);
//			return true;
//		}
//		File barcodeFile = new File(barcodesRoot, makeBarcodeFileName(contents)
//				+ ".png");
//		barcodeFile.delete();
//		FileOutputStream fos = null;
//		try {
//			fos = new FileOutputStream(barcodeFile);
//			bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
//		} catch (FileNotFoundException fnfe) {
//			Log.w(TAG, "Couldn't access file " + barcodeFile + " due to "
//					+ fnfe);
//			showErrorMessage(R.string.msg_unmount_usb);
//			return true;
//		} finally {
//			if (fos != null) {
//				try {
//					fos.close();
//				} catch (IOException ioe) {
//					// do nothing
//				}
//			}
//		}
//		return false;
//	}
//	}
}
