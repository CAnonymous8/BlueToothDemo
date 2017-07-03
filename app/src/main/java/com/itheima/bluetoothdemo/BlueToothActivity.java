package com.itheima.bluetoothdemo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.ParcelUuid;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import com.itheima.bluetoothdemo.adapter.MyListAdapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class BlueToothActivity extends Activity {

	private ListView mListView;
	private MyListAdapter mListAdapter;
	private ArrayList<BluetoothDevice> mListDatas = new ArrayList<BluetoothDevice>();

	// 操作和管理蓝牙的类
	private BluetoothAdapter mBluetoothAdapter;

	private OutputStream outputStream;

	private BroadcastReceiver myBlueToothReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
				Toast.makeText(getApplicationContext(), "开始扫描", 0).show();// 开始扫描的广播
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
				Toast.makeText(getApplicationContext(), "扫描完成", 0).show();// 扫描完成的广播
			} else if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				// 找到一个可用蓝牙设备的广播
				// 获取出蓝牙设备
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (!mListDatas.contains(device)){
					mListDatas.add(device); // 将蓝牙设备添加到集合
					// 给listview设置adapter
					mListAdapter.setData(mListDatas);// 设置adapter数据
				}
			}
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_open_bluetooth:			// 打开蓝牙
				if (mBluetoothAdapter.isEnabled())
					return;
				mBluetoothAdapter.enable();
				break;
			case R.id.btn_close_bluetooth:			// 关闭蓝牙
				if (!mBluetoothAdapter.isEnabled())
					return;
				mBluetoothAdapter.disable();
				break;
			case R.id.btn_start_discovery:			// 扫描蓝牙
				mListDatas.clear();
				mListAdapter.setData(mListDatas);
				mBluetoothAdapter.startDiscovery(); 
				break;
			case R.id.btn_close_discovery:			// 停止扫描
				mBluetoothAdapter.cancelDiscovery(); 
				break;
			case R.id.btn_open_light:
				sendCmd(1);
				break;
			case R.id.btn_close_light:
				sendCmd(2);
				break;
			case R.id.btn_open_once:
				sendCmd(3);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blue_tooth);
		initView();
		initReceiver();
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 获取蓝牙适配器
	}
	
	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		// 开始扫描
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); 
		// 扫描完成
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); 
		// 找到可用的蓝牙设备
		filter.addAction(BluetoothDevice.ACTION_FOUND); 
		registerReceiver(myBlueToothReceiver, filter);
}

	/**
	 * 初始化控件
	 */
	private void initView() {
		findViewById(R.id.btn_open_bluetooth).setOnClickListener(mOnClickListener);
		findViewById(R.id.btn_close_bluetooth).setOnClickListener(mOnClickListener);

		findViewById(R.id.btn_start_discovery).setOnClickListener(mOnClickListener);
		findViewById(R.id.btn_close_discovery).setOnClickListener(mOnClickListener);

		findViewById(R.id.btn_open_light).setOnClickListener(mOnClickListener);
		findViewById(R.id.btn_close_light).setOnClickListener(mOnClickListener);
		findViewById(R.id.btn_open_once).setOnClickListener(mOnClickListener);

		mListView = (ListView) findViewById(R.id.lv_bluetooth_devices);
		mListAdapter = new MyListAdapter(BlueToothActivity.this, mListDatas);
		mListView.setAdapter(mListAdapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) @Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 获取出选中的蓝牙
				BluetoothDevice bluetoothDevice = mListDatas.get(position);
				// 连接蓝牙
				connectBlueTooth(bluetoothDevice);
			}
		});
	}

	private void sendCmd(int type) {
		if (outputStream == null)
			return;

		// 协议
		try {
			byte[] b = new byte[5];
			b[0] = (byte) 0x01;		// 1
			b[1] = (byte) 0x99;		// -103

			if (type == 1) {		// 开灯
				b[2] = (byte) 0x10;	// 16
				b[3] = (byte) 0x10;
			} else if (type == 2) {	// 关灯
				b[2] = (byte) 0x11;	// 17
				b[3] = (byte) 0x11;
			} else if (type == 3) {	// 点动
				b[2] = (byte) 0x19;	// 25
				b[3] = (byte) 0x19;
			}
			b[4] = (byte) 0x99; 	// -103
			
			outputStream.write(b);
			outputStream.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 连接蓝牙
	 * 
	 * @param bluetoothDevice
	 */
	private void connectBlueTooth(final BluetoothDevice bluetoothDevice) {
		new Thread() {
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) public void run() {
				try {
					// 连接到一个蓝牙设备
					UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
					BluetoothSocket bluetoothSocket = bluetoothDevice
							.createRfcommSocketToServiceRecord(uuid);

					bluetoothSocket.connect();							// 连接的操作
					outputStream = bluetoothSocket.getOutputStream(); 	// 获取发送指令的操作
					ParcelUuid[] uuids = bluetoothSocket.getRemoteDevice().getUuids();

					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(BlueToothActivity.this, 
									"连接成功", Toast.LENGTH_SHORT).show();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBlueToothReceiver);
	}
}
