package com.quipmate2.features;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.quipmate2.R;
import com.quipmate2.constants.AppProperties;
import com.quipmate2.loadwebimageandcache.ImageLoader;
import com.quipmate2.utils.CommonMethods;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StatusUpdate extends Activity implements OnClickListener {
	
	private ImageButton updateStatus , attachPhoto , attachMood;
	private ImageView profilePic , selectedImg;
	private TextView txtSelectedImg, profileName;
	private Session session;
	private String action="post_status";
	private EditText status;
	private JSONArray jsonTask;
	private JSONObject result;
	private String content;
	private ArrayList<NameValuePair> nameValuePairs;
	
	private boolean photoAttached;
	private String actionPhoto="photo_upload";
	private String imagepath;
	private int moodNumber;
	final static int RESULT_LOAD_IMAGE=0;
	final static int RESULT_SELECT_MOOD=2;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_update);
		getActionBar().setTitle("Update Status");
		init();
		
		updateStatus.setOnClickListener(this);
		attachPhoto.setOnClickListener(this);
		attachMood.setOnClickListener(this);
	}

	private void init() {
		// TODO Auto-generated method stub
		photoAttached = false;
		moodNumber = -1;
		session = new Session(StatusUpdate.this);
		updateStatus = (ImageButton) findViewById(R.id.update_status);
		attachPhoto = (ImageButton) findViewById(R.id.attach_photo);
		attachMood = (ImageButton) findViewById(R.id.mood);
		status = (EditText) findViewById(R.id.status_content);
		profilePic = (ImageView) findViewById(R.id.profilepic);
		profileName = (TextView) findViewById(R.id.tv_name);
		selectedImg = (ImageView) findViewById(R.id.selected_image);
		txtSelectedImg = (TextView) findViewById(R.id.tv_selected_image);
		
		
		profileName.setText(session.getValue(AppProperties.MY_PROFILE_NAME));
		Thread showPic = new Thread() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				final ImageLoader load = new ImageLoader(StatusUpdate.this);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						load.DisplayImage(session.getValue(AppProperties.MY_PROFILE_PIC),profilePic);
					}
				});
			}
		};
		showPic.start();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id=v.getId();
		switch(id){
		case R.id.update_status:
			content = status.getText().toString();
            if((!content.equals(null) && !content.equals("") && photoAttached==false) || moodNumber != -1){	
            new UpdateStatus().execute();
            }
            else{
            	if(photoAttached){
                nameValuePairs = new  ArrayList<NameValuePair>();
                System.out.println(imagepath);
                nameValuePairs.add(new BasicNameValuePair("photo_box",imagepath));
                nameValuePairs.add(new BasicNameValuePair(AppProperties.ACTION, actionPhoto));
                nameValuePairs.add(new BasicNameValuePair("photo_description",status.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("photo_hidden_profileid", 
                		session.getValue(AppProperties.PROFILE_ID)));
                new PhotoUpload().execute();
                
            	}
                       }
            break;
            
		case R.id.attach_photo:
			Intent pick = new Intent();
			pick.setType("image/*");  
			pick.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(pick, RESULT_LOAD_IMAGE);
			break;
			
		case R.id.mood:
			Intent moodpick= new Intent(this,MoodSelect.class);
			startActivityForResult(moodpick, RESULT_SELECT_MOOD);
			
		}
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
			
			Uri selectedImageUri = data.getData();
			System.out.println(selectedImageUri);
            imagepath = getPath(selectedImageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
            selectedImg.setImageBitmap(bitmap);
            txtSelectedImg.setText("Uploading file path:" + imagepath);
            txtSelectedImg.setVisibility(View.VISIBLE);
		    photoAttached = true;
		    status.setHint(getString(R.string.status_hint_photo));
		    attachMood.setVisibility(View.INVISIBLE);
		   
            } 
		
			if (requestCode == RESULT_SELECT_MOOD && resultCode == RESULT_OK && data != null){
				moodNumber = data.getIntExtra("mood_number", -1) + 1;
				attachPhoto.setVisibility(View.INVISIBLE);
				String feeling=getResources().getStringArray(R.array.moods)[moodNumber-1];
				TypedArray moodimg = getResources().obtainTypedArray(R.array.mood_icons);
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), moodimg.getResourceId(moodNumber-1, -1));
				if(bitmap != null){
				selectedImg.setImageBitmap(bitmap);
				txtSelectedImg.setText("- feeling " + feeling);
				txtSelectedImg.setVisibility(View.VISIBLE);
				status.setHint(getString(R.string.mood_hint));
				moodimg.recycle();
				//new UpdateStatus().execute();
				}
			}
		
		} 
	
	
	public void post(String url, List<NameValuePair> nameValuePairs) {
	    HttpClient httpClient = new DefaultHttpClient();
	    HttpContext localContext = new BasicHttpContext();
	    HttpPost httpPost = new HttpPost(url);

	    try {
	        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

	        for(int index=0; index < nameValuePairs.size(); index++) {
	            if(nameValuePairs.get(index).getName().equalsIgnoreCase("photo_box")) {
	                // If the key equals to "photo_box", we use FileBody to transfer the data
	                entity.addPart(nameValuePairs.get(index).getName(), new FileBody(new File (nameValuePairs.get(index).getValue())));
	            } else {
	                // Normal string data
	                entity.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));
	            }
	        }

	        httpPost.setEntity(entity);
	        
	        HttpResponse response = httpClient.execute(httpPost, localContext);
	        String error=EntityUtils.toString(response.getEntity());
	        System.out.println(error);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
	
	public class PhotoUpload extends AsyncTask<Void, Void, Void>
	{
		ProgressDialog pDialog;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			pDialog = new ProgressDialog(StatusUpdate.this);
			pDialog.setMessage("Uploading image");
			pDialog.show();
		}
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			post(AppProperties.URL,nameValuePairs);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
		if(pDialog.isShowing())
			pDialog.dismiss();
		}
	}
	
  public class UpdateStatus extends AsyncTask<Void, Void, Void>
  {
	  ProgressDialog pdialog;
	  @Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		pdialog = new ProgressDialog(StatusUpdate.this);
		pdialog.setMessage("Updating Status");
		pdialog.show();
	}

	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		
		//no mood, call status update api
		if(moodNumber == -1){
			try{
		List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
		apiParams.add(new BasicNameValuePair(AppProperties.ACTION, action));
		apiParams.add(new BasicNameValuePair(AppProperties.PAGE,content));
		apiParams.add(new BasicNameValuePair(AppProperties.PROFILE_ID, session
				.getValue(AppProperties.PROFILE_ID)));
		jsonTask = CommonMethods.loadJSONData(AppProperties.URL, AppProperties.METHOD_GET, apiParams);
		result = jsonTask.getJSONObject(0);
		
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		}
		//mood attached, call mood api
		else{
			try{
			List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
			apiParams.add(new BasicNameValuePair(AppProperties.ACTION, "mood"));
			apiParams.add(new BasicNameValuePair(AppProperties.PROFILE_ID,
					session.getValue(AppProperties.PROFILE_ID)));
			apiParams.add(new BasicNameValuePair("mood_desc", status.getText().toString()));
			apiParams.add(new BasicNameValuePair("mood",moodNumber+""));
			jsonTask = CommonMethods.loadJSONData(AppProperties.URL, AppProperties.METHOD_GET, apiParams );
			result = jsonTask.getJSONObject(0);
			}
			catch(JSONException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result2) {
		// TODO Auto-generated method stub
		try{
		if(result.has(AppProperties.ACK)){
			String ack = result.getString(AppProperties.ACK);
			if(ack.equals("true")){
				Toast.makeText(StatusUpdate.this, getResources().getString(R.string.success_status),Toast.LENGTH_LONG).show();
			}
		}
		if(result.has(getString(R.string.error))){
			Toast.makeText(StatusUpdate.this, getResources().getString(R.string.status_error),Toast.LENGTH_LONG).show();	
		}
	}
		catch(JSONException e){
			e.printStackTrace();
		}
		if(pdialog.isShowing())
			pdialog.dismiss();
		
		finish();
	} 
  }



}
