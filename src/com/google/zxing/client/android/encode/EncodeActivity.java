/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.encode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.FinishListener;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.androidtest.R;
import com.mixed.activity.ContactDetailActivity;
import com.mixed.entity.ContactInfo;

/**
 * This class encodes data from an Intent into a QR code, and then displays it full screen so that
 * another person can scan it with their device.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class EncodeActivity extends Activity {

  private static final String TAG = EncodeActivity.class.getSimpleName();

  private static final int MAX_BARCODE_FILENAME_LENGTH = 24;

  public static final int REQUEST_CODE_GENERATE = 1;

  private QRCodeEncoder qrCodeEncoder;
  
  private ContactInfo contact;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    Intent intent = getIntent();
    contact = (ContactInfo) intent.getBundleExtra(Intents.Encode.DATA).getSerializable("ContactInfo");
    if (intent != null) {
      String action = intent.getAction();
      if (action.equals(Intents.Encode.ACTION) || action.equals(Intent.ACTION_SEND)) {
        setContentView(R.layout.encode);
        return;
      }
    }
    finish();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, Menu.FIRST, 0, "保存");
    //menu.add(0, Menu.FIRST, 0, R.string.menu_share).setIcon(android.R.drawable.ic_menu_share);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (qrCodeEncoder == null) { // Odd
      Log.w(TAG, "No existing barcode to send?");
      return true;
    }

    String contents = qrCodeEncoder.getContents();
    Bitmap bitmap;
    try {
      bitmap = qrCodeEncoder.encodeAsBitmap();
    } catch (WriterException we) {
      Log.w(TAG, we);
      return true;
    }

    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
    	//保存图片到SD上
    	File sdCardDir = Environment.getExternalStorageDirectory();
    	sdCardDir = new File(sdCardDir, "TwoCodeContact");
    	
    	FileOutputStream fos = null;        
    	try {
    	  if(!sdCardDir.exists()){
    	     sdCardDir.mkdir();
    	  }
	        File saveFile = new File(sdCardDir, makeBarcodeFileName(contents) + ".png");
	        Log.i(TAG, saveFile.getAbsolutePath());
            fos = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException fnfe) {
          Log.w(TAG, "找不到文件  due to " + fnfe);
          showErrorMessage(R.string.msg_unmount_usb);
          return true;
        } catch (IOException e) {
        	Log.e(TAG, "文件读写出现异常情况");
        	if (fos != null) {
                try {
                  fos.close();
                } catch (IOException ioe) {
                }
            }
		} finally {
          if (fos != null) {
            try {
              fos.close();
            } catch (IOException ioe) {
            }
          }
        }
    }
    
//    File bsRoot = new File(Environment.getExternalStorageDirectory(), "TwoCodeContact");
//    File barcodesRoot = new File(bsRoot, "TwoCodeContact");
//    if (!barcodesRoot.exists() && !barcodesRoot.mkdirs()) {
//      Log.w(TAG, "���ܴ����ļ��� " + barcodesRoot);
//      showErrorMessage(R.string.msg_unmount_usb);
//      return true;
//    }
//    File barcodeFile = new File(barcodesRoot, makeBarcodeFileName(contents) + ".png");
//    barcodeFile.delete();
//    FileOutputStream fos = null;
//    try {
//      fos = new FileOutputStream(nFile);
//      bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
//      Toast.makeText(this, "��ɶ�ά��ɹ�!", Toast.LENGTH_SHORT);
//    } catch (FileNotFoundException fnfe) {
//      Log.w(TAG, "���ܷ����ļ� " + nFile + " due to " + fnfe);
//      showErrorMessage(R.string.msg_unmount_usb);
//      return true;
//    } finally {
//      if (fos != null) {
//        try {
//          fos.close();
//        } catch (IOException ioe) {
//        }
//      }
//    }
    
    Intent intent = new Intent(this, ContactDetailActivity.class);
    intent.putExtra("ContactInfo", contact);
    startActivity(intent);
    finish();

    //���?SMS���ͣ������ŵ�Activity
//    Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
//    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " - " +
//        qrCodeEncoder.getTitle());
//    intent.putExtra(Intent.EXTRA_TEXT, qrCodeEncoder.getContents());
//    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + barcodeFile.getAbsolutePath()));
//    intent.setType("image/png");
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//    startActivity(Intent.createChooser(intent, null));
    return true;
  }
  
  static Pattern pattern = Pattern.compile("\\d{11}");
  
  private static String makeBarcodeFileName(CharSequence contents) {
    //int fileNameLength = Math.min(MAX_BARCODE_FILENAME_LENGTH, contents.length());
    String fileName = "";
    Matcher matcher = pattern.matcher(contents);
    String number = "";
    if(matcher.find()){
    	number = matcher.group();
    }
//    for (int i = 0; i < fileNameLength; i++) {
//      char c = contents.charAt(i);
//      if ((c >= '0' && c <= '9')) { //(c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || 
//        fileName.append(c);
//      } else {
//        fileName.append('_');
//      }
//    }
    if(!"".equals(number)){
    	fileName = number;
    }
    return fileName;
  }

  @Override
  protected void onResume() {
    super.onResume();
    // This assumes the view is full screen, which is a good assumption
    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    Display display = manager.getDefaultDisplay();
    int width = display.getWidth();
    int height = display.getHeight();
    int smallerDimension = width < height ? width : height;
    smallerDimension = smallerDimension * 7 / 8;

    Intent intent = getIntent();
    try {
      qrCodeEncoder = new QRCodeEncoder(this, intent, smallerDimension);
      setTitle(getString(R.string.app_name) + " - " + qrCodeEncoder.getTitle());
      Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
      ImageView view = (ImageView) findViewById(R.id.image_view);
      view.setImageBitmap(bitmap);
      TextView contents = (TextView) findViewById(R.id.contents_text_view);
      contents.setText(qrCodeEncoder.getDisplayContents());
    } catch (WriterException e) {
      Log.e(TAG, "Could not encode barcode", e);
      showErrorMessage(R.string.msg_encode_contents_failed);
      qrCodeEncoder = null;
    } catch (IllegalArgumentException e) {
      Log.e(TAG, "Could not encode barcode", e);
      showErrorMessage(R.string.msg_encode_contents_failed);
      qrCodeEncoder = null;
    }
  }

  private void showErrorMessage(int message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(message);
    builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
    builder.setOnCancelListener(new FinishListener(this));
    builder.show();
  }
  
  //��д��ȡҳ��ش�ֵ
  @Override
//  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  
//  	switch (requestCode) {
//  	case REQUEST_CODE_GENERATE:
//  		if (resultCode == RESULT_OK) {
//				Toast.makeText(this, "����뱣���ά��ɹ�!", Toast.LENGTH_LONG);
//			}
//			break;
//		}
//    }
  
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    if (requestCode == 0) {
	        if (resultCode == RESULT_OK) {
	            String contents = intent.getStringExtra("SCAN_RESULT");
	            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
	            // Handle successful scan
	            Log.i(TAG, contents + " : " + format);
	        } else if (resultCode == RESULT_CANCELED) {
	            // Handle cancel
	        }
	    }
	}
}
