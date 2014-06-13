package com.quipmate2.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.quipmate2.R;
import com.quipmate2.constants.AppProperties;
import com.quipmate2.utils.CommonMethods;
import com.quipmate2.utils.NetworkHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;


public class Login extends Activity implements OnClickListener {
	private ProgressBar progressBar;
	EditText etemail, etpassword;
	private Button blogin;
	Session session;
	String email, password;
	private JSONObject last;
	private boolean isPressed = false;
	private JSONArray result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login);
		initView();
		
		//hide the keyboard
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		session = new Session(Login.this);
		if (session.hasKey(AppProperties.PARAM_EMAIL)) {
			etemail.setText(session.getValue(AppProperties.PARAM_EMAIL));
		}
		if (session.hasKey(AppProperties.PARAM_PASSWORD)) {
			etpassword.setText(session.getValue(AppProperties.PARAM_PASSWORD));
		}	
	}
	private void initView() {
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		progressBar.setVisibility(View.GONE);
		etemail = (EditText) findViewById(R.id.etemail);
		etpassword = (EditText) findViewById(R.id.etpassword);
		blogin = (Button) findViewById(R.id.blogin);
		blogin.setOnClickListener(this);
	}

	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (isPressed) {
			return;
		}
		isPressed = true;
		if (v.getId() == R.id.blogin) {
			new AttempLogin().execute();
		}
		isPressed = false;
		
	}

	public class AttempLogin extends AsyncTask<Void, Void, Void>
	{
		
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			//progressBar.setVisibility(View.VISIBLE);

			System.out.println("Trying to log you in");
			email = etemail.getText().toString();
			password = etpassword.getText().toString();

			session.setValue(AppProperties.PARAM_EMAIL, email);
			session.setValue(AppProperties.PARAM_PASSWORD, password);
			session.commit();

			try {

				if (NetworkHelper.checkNetworkConnection(Login.this)) {
					List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
					apiParams.add(new BasicNameValuePair(AppProperties.ACTION,
							AppProperties.LOGIN));
					apiParams.add(new BasicNameValuePair(
							AppProperties.PARAM_PASSWORD, password));
					apiParams.add(new BasicNameValuePair(AppProperties.PARAM_EMAIL,
							email));
					result = CommonMethods.loadJSONData(AppProperties.URL, AppProperties.METHOD_POST, apiParams);
					System.out.println(result);
					if(result != null){
						last = result.getJSONObject(0);
						
					}
					if (last != null) {

						if (last.has(AppProperties.ACK)
								&& last.getString(AppProperties.ACK).equals(
										AppProperties.ACK_CODE)) {
							session.setValue(AppProperties.MY_PROFILE_NAME, 
									last.getString(AppProperties.MY_PROFILE_NAME));
							session.setValue(AppProperties.MY_PROFILE_PIC,
									last.getString(AppProperties.MY_PROFILE_PIC));
							session.setValue(AppProperties.SESSION_ID,
									last.getString(AppProperties.SESSION_ID));
							session.setValue(AppProperties.PROFILE_ID,
									last.getString(AppProperties.MY_PROFILE_ID));
							session.setValue(AppProperties.SESSION_NAME,
									last.getString(AppProperties.SESSION_NAME));
							
							if (session.commit()) {

								Intent intent = new Intent(Login.this, WelcomeActivity.class);
								startActivity(intent);
								finish();

							} else {
								System.out.println("Some problem in Sigin. Please try again.");
							}
						} else if (last.has(getString(R.string.error))) {
							
							JSONObject error = last
									.getJSONObject(getString(R.string.error));
							if (error.getString(getString(R.string.code)).equals(
									AppProperties.WRONG_CREDENTIAL_CODE)) {
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										progressBar.setVisibility(View.INVISIBLE);
										CommonMethods.ShowInfo(Login.this,
												getString(R.string.invalid_credential))
												.show();
									}
								});
								
							}
						}
					}
				} else {
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							progressBar.setVisibility(View.INVISIBLE);
							CommonMethods.ShowInfo(Login.this,
									getString(R.string.network_error)).show();
						}
					});
					
				}
			} catch (JSONException e) {
				// should not happen
				e.printStackTrace();
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			
			if(progressBar.getVisibility()==View.VISIBLE)
			progressBar.setVisibility(View.INVISIBLE);
		}
		
	}

}
