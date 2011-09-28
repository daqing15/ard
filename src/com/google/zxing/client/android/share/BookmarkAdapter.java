/*
 * Copyright (C) 2011 ZXing authors
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

package com.google.zxing.client.android.share;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.client.androidtest.R;

final class BookmarkAdapter extends BaseAdapter {
  private final Context context;
  private final Cursor cursor;

  public BookmarkAdapter(Context context, Cursor cursor) {
    this.context = context;
    this.cursor = cursor;
  }

  public int getCount() {
    return cursor.getCount();
  }

  public Object getItem(int index) {
    // Not used, so no point in retrieving it.
    return null;
  }

  public long getItemId(int index) {
    return index;
  }

  public View getView(int index, View view, ViewGroup viewGroup) {
    LinearLayout layout;
    if (view == null || !(view instanceof LinearLayout)) {
      LayoutInflater factory = LayoutInflater.from(context);
      layout = (LinearLayout) factory.inflate(R.layout.bookmark_picker_list_item, viewGroup, false);
    } else {
      layout = (LinearLayout) view;
    }

    cursor.moveToPosition(index);
    String title = cursor.getString(BookmarkPickerActivity.TITLE_COLUMN);
    ((TextView) layout.findViewById(R.id.bookmark_title)).setText(title);
    String url = cursor.getString(BookmarkPickerActivity.URL_COLUMN);
    ((TextView) layout.findViewById(R.id.bookmark_url)).setText(url);
    return layout;
  }
}
