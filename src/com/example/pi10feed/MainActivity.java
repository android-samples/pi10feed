package com.example.pi10feed;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	ListView mListView;
	ArrayList<String> mData = new ArrayList<String>();
	ArrayList<String> mUrls = new ArrayList<String>();
	ArrayAdapter<String> mAdapter;
	//HashMap<String, String> mTitleToUrl = new HashMap<String, String>();
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// ローディング非表示
		findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);

		// 初期項目
		mData.add("no data");
		mUrls.add("no data");
		
		// ListView準備
		mListView = (ListView)findViewById(R.id.listView1);
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mData);
		mListView.setAdapter(mAdapter);
		
		// クリック時
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
				String title = mData.get(position);
				String url = mUrls.get(position);
				// Toast.makeText(MainActivity.this, title, Toast.LENGTH_SHORT).show();
				// URL起動
				try{
					Toast.makeText(MainActivity.this, url, Toast.LENGTH_SHORT).show();
					Uri uri = Uri.parse(url);
					Intent i = new Intent(Intent.ACTION_VIEW,uri);
					startActivity(i);
				}
				catch(Exception ex){
					Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		String msg = "";
		switch(id){
		case R.id.action_settings: msg="A"; break;
		case R.id.action_settings1: msg="B"; break;
		case R.id.action_settings2: msg="C"; break;
		}
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// 更新ボタン
	public void buttonMethodRefresh(View button){
		refresh();
	}
	
	// 更新処理
	public void refresh(){
		// 通信タスク生成
		if(mTask != null)return;
		mTask = new HttpTask();
		// タスク実行
		mTask.execute("http://b.hatena.ne.jp/hotentry.rss");
	}
	
	// 結果表示
	public void showResult(String html){
		// データクリア
		mData.clear();
		mUrls.clear();
		
		// HTMLをJsoupで分解
		Document doc = Jsoup.parse(html);
		Elements items = doc.select("item");
		String msg = "";
		for(int i = 0; i < items.size(); i++){
			try{
				Element item = items.get(i);
				String title = item.getElementsByTag("title").get(0).text();
				String link = item.getElementsByTag("link").get(0).text();
				// 結果追記
				mData.add(title);
				mUrls.add(link);
			}
			catch(Exception ex){
				mData.add(ex.toString());
				mUrls.add("error");
			}
		}
		
		// データ変更を通知
		mAdapter.notifyDataSetChanged();
	}
	
	// 通信タスク
	HttpTask mTask = null;
	class HttpTask extends AsyncTask<String, Integer, String>{
		String mUrl = "";
		
		// オプション：事前準備
		@Override
		protected void onPreExecute() {
			// ローディング表示
			findViewById(R.id.progressBar1).setVisibility(View.VISIBLE);
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		// 必須：バッググラウンド処理を書く
		// ※AsyncTask の cancel を呼び出すと、doInBackground は InterruptedException がおきて終了して、その後 onPostExecute ではなく onCancelled が呼ばれました。
		@Override
		protected String doInBackground(String... params) {
			if(params.length == 0)return null;
			String result = null;
			try{
				mUrl = params[0];
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(mUrl);
				HttpResponse response = client.execute(request);
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
					result = EntityUtils.toString(response.getEntity());
				}
				else{
					result = "NOT OK";
				}
			}
			catch(Exception ex){
				result = ex.toString();
			}
			return result;
		}

		// オプション：進捗状況をUIスレッドで表示する処理
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
		
		// オプション：事後処理（バックグラウンド処理が完了し、UIスレッドに反映する処理を書く）（キャンセル時は呼ばれない）
		@Override
		protected void onPostExecute(String result) {
			// 結果表示
			showResult(result);
			mTask = null;
			// ローディング非表示
			findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
			super.onPostExecute(result);
		}

		// オプション：キャンセルした状態（cancelが呼ばれた状態）でdoInBackgroundを抜けたときに呼ばれる
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

	}

}
