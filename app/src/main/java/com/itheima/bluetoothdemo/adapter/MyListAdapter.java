package com.itheima.bluetoothdemo.adapter;

import java.util.ArrayList;

import com.itheima.bluetoothdemo.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyListAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<BluetoothDevice> mList;
	private LayoutInflater mInflater;

	public MyListAdapter(Context context, ArrayList<BluetoothDevice> list) {
		this.mContext = context;
		this.mList = list;
		this.mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.item_bluetooth, null);
		} else {
			view = convertView;
		}

		TextView tv_item_name = (TextView) view.findViewById(R.id.tv_item_name);
		TextView tv_item_address = (TextView) view.findViewById(R.id.tv_item_address);

		BluetoothDevice bluetoothDevice = mList.get(position);
		tv_item_name.setText(bluetoothDevice.getName());		// 设置蓝牙名称
		tv_item_address.setText(bluetoothDevice.getAddress());// 设置蓝牙地址

		return view;
	}

	/**
	 * 刷新数据显示
	 * @param list
	 */
	public void setData(ArrayList<BluetoothDevice> list) {
		this.mList = list;
		notifyDataSetChanged();
	}
}
