package com.itheima.bluetoothdemo;

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

	private BluetoothAdapter mBluetoothAdapter;
	private OutputStream outputStream;

	private BroadcastReceiver myBlueToothReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
				Toast.makeText(getApplicationContext(), "��ʼɨ��", 0).show();// ��ʼɨ��Ĺ㲥
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
				Toast.makeText(getApplicationContext(), "ɨ�����", 0).show();// ɨ����ɵĹ㲥
			} else if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				// �ҵ�һ�����������豸�Ĺ㲥
				// ��ȡ�������豸
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mListDatas.add(device); // �������豸��ӵ�����
				// ��listview����adapter
				mListAdapter.setData(mListDatas);// ����adapter����
			}
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_open_bluetooth:			// ������
				if (mBluetoothAdapter.isEnabled())
					return;
				mBluetoothAdapter.enable();
				break;
			case R.id.btn_close_bluetooth:			// �ر�����
				if (!mBluetoothAdapter.isEnabled())
					return;
				mBluetoothAdapter.disable();
				break;
			case R.id.btn_start_discovery:			// ɨ������
				mListDatas.clear();
				mListAdapter.setData(mListDatas);
				mBluetoothAdapter.startDiscovery(); 
				break;
			case R.id.btn_close_discovery:			// ֹͣɨ��
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
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // ��ȡ����������
	}
	
	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		// ��ʼɨ��
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); 
		// ɨ�����
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); 
		// �ҵ����õ������豸
		filter.addAction(BluetoothDevice.ACTION_FOUND); 
		registerReceiver(myBlueToothReceiver, filter);
	}

	/**
	 * ��ʼ���ؼ�
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

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// ��ȡ��ѡ�е�����
				BluetoothDevice bluetoothDevice = mListDatas.get(position);
				// ��������
				connectBlueTooth(bluetoothDevice);
			}
		});
	}

	private void sendCmd(int type) {
		if (outputStream == null)
			return;
		
		try {
			byte[] b = new byte[5];
			b[0] = (byte) 0x01;		// 1
			b[1] = (byte) 0x99;		// -103
			if (type == 1) {		// ����
				b[2] = (byte) 0x10;	// 16
				b[3] = (byte) 0x10;
			} else if (type == 2) {	// �ص�
				b[2] = (byte) 0x11;	// 17
				b[3] = (byte) 0x11;
			} else if (type == 3) {	// �㶯
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
	 * ��������
	 * 
	 * @param bluetoothDevice
	 */
	private void connectBlueTooth(final BluetoothDevice bluetoothDevice) {
		new Thread() {
			public void run() {
				try {
					UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
					BluetoothSocket bluetoothSocket = bluetoothDevice
							.createRfcommSocketToServiceRecord(uuid);
					bluetoothSocket.connect();							// ���ӵĲ���
					outputStream = bluetoothSocket.getOutputStream(); 	// ��ȡ����ָ��Ĳ���
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(BlueToothActivity.this, 
									"���ӳɹ�", Toast.LENGTH_SHORT).show();
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
