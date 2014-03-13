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
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainPhoneActivity extends SherlockActivity implements
SearchView.OnQueryTextListener{
	PhoneAdapter adapter;
	private ListView listView;
	public static ArrayList<Phone> nums = null;
	private ProgressDialog progressDialog;	
	private static String file_url = "http://0.203class.duapp.com/sixclass.py?pw=12085208";
	private String filePath = Environment.getExternalStorageDirectory().toString() + "/sixclass/phone.dat";
//	private ImageButton feedButton;
	//protected static final Context mContext = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmengUpdateAgent.update(this);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        
        showProgressDialog();
    	System.out.print("else");
    	new DownloadFileFromURL().execute(file_url);
    	  
    	//feedButton = (ImageButton) findViewById(R.id.feed);
		listView = (ListView) findViewById(R.id.listView1);
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

		UMFeedbackService.enableNewReplyNotification(this, NotificationType.AlertDialog);
		UmengUpdateAgent.setOnDownloadListener(new UmengDownloadListener(){
		    @Override
		    public void OnDownloadEnd(int result) {
		        Toast.makeText(getApplicationContext(), "download result : " + result , Toast.LENGTH_SHORT).show();
		    }           
		});
		
		/*ActionBar actionBar = getActionBar();
	    actionBar.setCustomView(R.layout.feed);
	    ImageButton search = (ImageButton) actionBar.getCustomView().findViewById(R.id.fb_btn);
	    search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gotofb();
			}	     
	    });
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
	        | ActionBar.DISPLAY_SHOW_HOME );*/
	  }
	
	// 用户输入字符时激发该方法
		@Override
		public boolean onQueryTextChange(String newText)
		{
			if (TextUtils.isEmpty(newText))
			{
				// 清除ListView的过滤
//				lv.clearTextFilter();
				adapter.getFilter().filter("");
				listView.clearTextFilter();
			}
			else
			{
				// 使用用户输入的内容对ListView的列表项进行过滤
//				lv.setFilterText(newText);
				adapter.getFilter().filter(newText.toString());
			}
			return true;
		}

		// 单击搜索按钮时激发该方法
		@Override
		public boolean onQueryTextSubmit(String query)
		{
			// 实际应用中应该在该方法内执行实际查询
			// 此处仅使用Toast显示用户输入的查询内容
			Toast.makeText(this, "您的选择是:" + query
					, Toast.LENGTH_SHORT).show();
			return false;
		}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		
//		 SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	        if (null != searchView )
	        {
//	            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
        switch(item.getItemId())
        {
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
			// TODO Auto-generated method stub
			return filter.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
//				LayoutInflater mInflater = (LayoutInflater) _context
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				LayoutInflater mInflater = getLayoutInflater();
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.name = (TextView) convertView.findViewById(R.id.textView1);
				holder.num = (TextView) convertView.findViewById(R.id.textView2);
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
			 holder.dia.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View v) {
			 // TODO Auto-generated method stub
			 Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
			 + phone.getNum()));
			 startActivity(intent);
			 }
			 });
			 holder.msm.setOnClickListener(new OnClickListener(){
			 @Override
			 public void onClick(View v) {
			 // TODO Auto-generated method stub
			 Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse("smsto:"
			 + phone.getNum()));
			 startActivity(intent);
			 }
			 });
			return convertView;
		}

		@Override
		public Filter getFilter() {
			return new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence charSequence) {
					FilterResults results = new FilterResults();

					// If there's nothing to filter on, return the original data for
					// your list
					if (charSequence == null || charSequence.length() == 0) {
						results.values = datas;
						results.count = datas.size();
					} else {
						ArrayList<Phone> filterResultsData = new ArrayList<Phone>();

						for (Phone data : datas) {
							// In this loop, you'll filter through originalData and
							// compare each item to charSequence.
							// If you find a match, add it to your new ArrayList
							// I'm not sure how you're going to do comparison, so
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
	public boolean fileIsExists(){
        File f=new File("/sdcard/sixclass/phone.dat");
        if(!f.exists()){
                return false;
        }
        return true;
    }
    public void gotofb(){
    	UMFeedbackService.openUmengFeedbackSDK(this);
    }

    
    private String openFile(String fileName) {
    	FileInputStream fin;
    	String res = "";    	
		int length;
		try {
			//fin = openFileInput(fileName);
			File file = new File(fileName);
			fin = new FileInputStream(file);
			length = fin.available();
			byte [] buffer = new byte[length];   
	        fin.read(buffer);       
	        res = EncodingUtils.getString(buffer, "UTF-8");   
	        fin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "can't find the file",
					Toast.LENGTH_SHORT).show();
		}   
        
        return res;
    }
    
    private ArrayList<Phone> getHotel(String fileName) {
    	String res = openFile(fileName);
    	System.out.print("res ok");
    	ArrayList<Phone> list = new ArrayList<Phone>();   	
		String name;
		String num;
		int sex;
//		String city;
		JSONObject phone;          
		try {
			JSONObject jsonObject = new JSONObject(res);
			JSONArray jsonArray = (JSONArray) jsonObject.get("all");
			for (int i = 0; i < jsonArray.length(); i++){
				//System.out.println(i);
				phone = jsonArray.getJSONObject(i);
				name = phone.getString("name");
				num = phone.getString("phone");
				sex = phone.getInt("sex");
//				city = phone.getString("city");
				list.add(new Phone(name, num, sex));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		if(list.isEmpty()){
			list.add(new Phone("name","phone",0));
		}
		System.out.print("list ok");
    	return list;
    }
    
   
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
	        try {
	            URL url = new URL(f_url[0]);
	            URLConnection conection = url.openConnection();
	            conection.connect();

	            // input stream to read file - with 8k buffer
	            InputStream input = new BufferedInputStream(url.openStream(), 8192);
	            
	            // Output stream to write file
	            File sdcardDir =Environment.getExternalStorageDirectory();
	            String path=sdcardDir.getPath()+"/sixclass";
	            File pathNew = new File(path);
	            if(!pathNew.exists()){
	            	pathNew.mkdirs();
	            }
	            OutputStream output = new FileOutputStream("/sdcard/sixclass/phone.dat");

	            byte data[] = new byte[1024];

	            while ((count = input.read(data)) != -1) {	                
	                // writing data to file
	                output.write(data, 0, count);
	            }

	            // flushing output
	            output.flush();
	            
	            // closing streams
	            output.close();
	            input.close();	           	            
	            
	        } catch (Exception e) {
	        	Log.e("Error: ", e.getMessage());
	        }
	        
	        return null;
		}
		

		/**
		 * After completing background task
		 * Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after the file was downloaded
			progressDialog.dismiss();
			//adapter.notifyDataSetChanged();
			nums = getHotel(filePath);	
			adapter = new PhoneAdapter(nums);
			listView.setAdapter(adapter);
			
			
			// Displaying downloaded image into image view
			// Reading image path from sdcard
			//String imagePath = Environment.getExternalStorageDirectory().toString() + "/downloadedfile.jpg";
			// setting downloaded into image view
			//my_image.setImageDrawable(Drawable.createFromPath(imagePath));
		}

	}
	
    private void showProgressDialog() {
		progressDialog = ProgressDialog.show(MainPhoneActivity.this,
				getText(R.string.update_title), getText(R.string.update_body), true,
				true);
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
