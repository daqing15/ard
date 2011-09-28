package com.mixed.activity;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.zxing.client.androidtest.R;
import com.mixed.entity.ContactInfo;

public class ContactInfoAdapter extends BaseAdapter {
	
	ArrayList<ContactInfo> itemList;
	LayoutInflater inflater;
	
	public ContactInfoAdapter(ArrayList<ContactInfo> list, Context context) {
		this.itemList = list;
		this.inflater = LayoutInflater.from(context);
		if(this.itemList == null){//没有结果
			this.itemList = new ArrayList<ContactInfo>();
		}
	}
	
	@Override
	public int getCount() {
		return itemList.size();
	}
	
	public ArrayList<ContactInfo> getItemList(){
		return this.itemList;
	}
	
	public void setItemList(ArrayList<ContactInfo> itemList) {
		this.itemList = itemList;
	}

	@Override
	public Object getItem(int position) {
		return itemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		convertView = inflater.inflate(R.layout.contact_item, null);
		holder = new ViewHolder();
		holder.name = (TextView) convertView.findViewById(R.id.userName);
		holder.phone = (TextView) convertView.findViewById(R.id.phone);
		holder.email = (TextView) convertView.findViewById(R.id.email);
		
		convertView.setTag(holder);
		
		holder.name.setText(itemList.get(position).userName);
		holder.phone.setText(itemList.get(position).phone);
		holder.email.setText(itemList.get(position).email);
		
		return convertView;
	}

	class ViewHolder {
		TextView name;
		TextView phone;
		TextView email;
	}
	
	class ViewProgressHolder {
		TextView text;
	}
	
}
