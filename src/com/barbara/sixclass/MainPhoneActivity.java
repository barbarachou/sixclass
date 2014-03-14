package com.barbara.sixclass;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.barbara.sixclass.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;
import com.umeng.update.UmengDownloadListener;
import com.umeng.update.UmengUpdateAgent;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainPhoneActivity extends SherlockActivity implements
		SearchView.OnQueryTextListener {
	PhoneAdapter adapter;
	private ListView listView;
	private String pw = "";
	private SharedPreferences password;
	public static ArrayList<Phone> nums = null;
	private ProgressDialog progressDialog;
	private static String file_url = "http://barbarachou.duapp.com/android6/sixclass.py?pw=";
	private String filePath = Environment.getExternalStorageDirectory()
			.toString() + "/sixclass/phone.dat";

	// private String pwPath = Environment.getExternalStorageDirectory()
	// .toString() + "/sixclass/pw.dat";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UmengUpdateAgent.update(this);
		UmengUpdateAgent.setUpdateOnlyWifi(false);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.listView1);

		File sdcardDir = Environment.getExternalStorageDirectory();
		String path = sdcardDir.getPath() + "/sixclass";
		File pathNew = new File(path);
		if (!pathNew.exists()) {
			pathNew.mkdirs();
		}
		password = getSharedPreferences("password", 0);

		getPhoneNum("请输入密码(您的学号)");
		nums = getHotel(filePath);
		adapter = new PhoneAdapter(nums);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parents, View view,
					int postion, long id) {
				Phone pn = nums.get(postion);
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
						+ pn.getNum()));
				startActivity(intent);
			}
		});

		listView.setTextFilterEnabled(true);
		UMFeedbackService.enableNewReplyNotification(this,
				NotificationType.AlertDialog);
		UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener() {
			@Override
			public void OnDownloadEnd(int result) {
				Toast.makeText(getApplicationContext(),
						"download result : " + result, Toast.LENGTH_SHORT)
						.show();
			}
		});

	}

	private void getPhoneNum(String str) {
		pw = password.getString("psw", "");
		if (pw.equals("")) {
			final EditText et = new EditText(this);
			new AlertDialog.Builder(this)
					.setTitle(str)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(et)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									pw = et.getText().toString();
									new DownloadFileFromURL().execute(file_url
											+ pw);
								}
							}).setNegativeButton("取消", null).show();
		} else {
			new DownloadFileFromURL().execute(file_url + pw);
		}
	}

	class DownloadFileFromURL extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainPhoneActivity.this,
					getText(R.string.update_title),
					getText(R.string.update_body), true, true);
		}

		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);
				URLConnection conection = url.openConnection();
				conection.connect();

				InputStream input = new BufferedInputStream(url.openStream(),
						8192);
				OutputStream output = new FileOutputStream(filePath);
				byte data[] = new byte[1024];
				while ((count = input.read(data)) != -1) {
					output.write(data, 0, count);
				}
				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {
			progressDialog.dismiss();
			if (openFile(filePath).equals("password error!")) {
				getPhoneNum("密码错误(您的学号)");
			} else {
				SharedPreferences.Editor editor = password
						.edit();
				editor.putString("psw", pw);
				editor.commit();
				// adapter.notifyDataSetChanged();
				nums = getHotel(filePath);
				adapter = new PhoneAdapter(nums);
				listView.setAdapter(adapter);
			}
		}
	}

	// 用户输入字符时激发该方法
	@Override
	public boolean onQueryTextChange(String newText) {
		if (TextUtils.isEmpty(newText)) {
			// 清除ListView的过滤
			// lv.clearTextFilter();
			adapter.getFilter().filter("");
			listView.clearTextFilter();
		} else {
			// 使用用户输入的内容对ListView的列表项进行过滤
			// lv.setFilterText(newText);
			adapter.getFilter().filter(newText.toString());
		}
		return true;
	}

	// 单击搜索按钮时激发该方法
	@Override
	public boolean onQueryTextSubmit(String query) {
		// 实际应用中应该在该方法内执行实际查询
		// 此处仅使用Toast显示用户输入的查询内容
		Toast.makeText(this, "您的选择是:" + query, Toast.LENGTH_SHORT).show();
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);

		// SearchManager searchManager = (SearchManager)
		// getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		if (null != searchView) {
			// searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(false);
		}
		searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(true);
		searchView.setQueryHint("查找");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.menu_settings:
			UMFeedbackService.openUmengFeedbackSDK(this);
			break;
		}
		return true;
	}

	private static class ViewHolder {
		public TextView name;
		public TextView num;
		public ImageButton dia;
		public ImageButton msm;
	}

	public class PhoneAdapter extends BaseAdapter implements Filterable {
		private ArrayList<Phone> datas;
		private ArrayList<Phone> filter;

		public PhoneAdapter(ArrayList<Phone> datas) {
			this.datas = datas;
			this.filter = datas;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			try {
				return filter.size();
			} catch (Exception e) {
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return filter.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				// LayoutInflater mInflater = (LayoutInflater) _context
				// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LayoutInflater mInflater = getLayoutInflater();
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.name = (TextView) convertView
						.findViewById(R.id.textView1);
				holder.num = (TextView) convertView
						.findViewById(R.id.textView2);
				holder.dia = (ImageButton) convertView.findViewById(R.id.dia);
				holder.msm = (ImageButton) convertView.findViewById(R.id.msm);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				// holder.spBubble.isChache = true;
			}

			final Phone phone = filter.get(position);
			holder.name.setText(phone.toString());
			holder.num.setText(phone.getNum());
			if (phone.getSex() == 1) {
				holder.name.setTextColor(android.graphics.Color.BLUE);
				holder.num.setTextColor(android.graphics.Color.BLUE);
			} else {
				holder.name.setTextColor(android.graphics.Color.BLACK);
				holder.num.setTextColor(android.graphics.Color.BLACK);
			}
			holder.dia.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri
							.parse("tel:" + phone.getNum()));
					startActivity(intent);
				}
			});
			holder.msm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse("smsto:" + phone.getNum()));
					startActivity(intent);
				}
			});
			return convertView;
		}

		@Override
		public Filter getFilter() {
			return new Filter() {
				@Override
				protected FilterResults performFiltering(
						CharSequence charSequence) {
					FilterResults results = new FilterResults();

					// If there's nothing to filter on, return the original data
					// for
					// your list
					if (charSequence == null || charSequence.length() == 0) {
						results.values = datas;
						results.count = datas.size();
					} else {
						ArrayList<Phone> filterResultsData = new ArrayList<Phone>();

						for (Phone data : datas) {
							// In this loop, you'll filter through originalData
							// and
							// compare each item to charSequence.
							// If you find a match, add it to your new ArrayList
							// I'm not sure how you're going to do comparison,
							// so
							// you'll need to fill out this conditional
							if (data.toString().contains(charSequence)) {
								filterResultsData.add(data);
							}
						}

						results.values = filterResultsData;
						results.count = filterResultsData.size();
					}

					return results;
				}

				@Override
				protected void publishResults(CharSequence charSequence,
						FilterResults filterResults) {
					filter = (ArrayList<Phone>) filterResults.values;
					notifyDataSetChanged();
				}
			};
		}

	}

	@SuppressLint("SdCardPath")
	public boolean fileIsExists() {
		File f = new File("/sdcard/sixclass/phone.dat");
		if (!f.exists()) {
			return false;
		}
		return true;
	}

	public void gotofb() {
		UMFeedbackService.openUmengFeedbackSDK(this);
	}

	private String openFile(String fileName) {
		FileInputStream fin;
		String res = "";
		int length;
		try {
			File file = new File(fileName);
			fin = new FileInputStream(file);
			length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	private ArrayList<Phone> getHotel(String fileName) {
		String res = openFile(fileName);
		ArrayList<Phone> list = new ArrayList<Phone>();
		String name;
		String num;
		int sex;
		// String city;
		JSONObject phone;
		try {
			JSONObject jsonObject = new JSONObject(res);
			JSONArray jsonArray = (JSONArray) jsonObject.get("all");
			for (int i = 0; i < jsonArray.length(); i++) {
				phone = jsonArray.getJSONObject(i);
				name = phone.getString("name");
				num = phone.getString("phone");
				sex = phone.getInt("sex");
				// city = phone.getString("city");
				list.add(new Phone(name, num, sex));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (list.isEmpty()) {
			list.add(new Phone("无联系人", "", 0));
		}
		return list;
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
