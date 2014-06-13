package com.quipmate2.adapter;

import java.util.ArrayList;

import com.example.quipmate2.R;
import com.quipmate2.features.NotificationInfo;
import com.quipmate2.features.Notifications;
import com.quipmate2.loadwebimageandcache.ImageLoader;
import com.quipmate2.utils.CommonMethods;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NotifsAdapter extends BaseAdapter {
	private ArrayList<NotificationInfo> notifList;
	private ImageLoader imgDownloader;

	public NotifsAdapter(ArrayList<NotificationInfo> notifList2, Notifications context) {
		// TODO Auto-generated constructor stub
	     notifList = notifList2;
	     imgDownloader = new ImageLoader(context);
	}
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return notifList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return notifList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		NotifViewHolder notifviewholder;
		if(view == null){
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		view = inflater.inflate(R.layout.single_notif, parent,
				false);
		notifviewholder = new NotifViewHolder();
		notifviewholder.notifImage = (ImageView) view.findViewById(R.id.notif_image);
		notifviewholder.notifDesc = (TextView) view.findViewById(R.id.notif_desc);
		notifviewholder.notifTime = (TextView) view.findViewById(R.id.notif_time);
		view.setTag(notifviewholder);
		}
		else{
			notifviewholder = (NotifViewHolder) view.getTag();
		}
		
		final NotificationInfo notif = notifList.get(position);
		if(notif != null){
			imgDownloader.DisplayImage(notif.actionBy.getImageURL(),notifviewholder.notifImage);
			String name=notif.actionBy.getName();
			notifviewholder.notifDesc.setText(name+" "+CommonMethods.getNotifDesc(notif.actionType));
			
			long tc=(System.currentTimeMillis()-Long.parseLong(notif.time)*1000)/1000;
			String time=null;
			
			if(tc<=60){
				time="1 seconds ago";
			}
			else{
				tc/=60;
				if(tc >= 1 && tc < 60){
					time=(tc+1)+" minutes ago";
				}
				else{
					tc/=60;
					if(tc >= 1 && tc < 24){
						time = (tc+1) +" hours ago";
					}
					else{
						tc=tc/24;
						if(tc >= 1 && tc <30){
							time = (tc+1)+ " days ago";
						}
						else{
							tc = tc/30;
							if(tc >= 1 && tc <= 12){
								time = (tc+1) +" months ago";
							}
							else{
								time = "more than a year ago";
							}
						}
					}
				}
			}
				
			notifviewholder.notifTime.setText(time);
		}
		return view;
	}
	
	static class NotifViewHolder{
		ImageView notifImage;
		TextView notifDesc;
		TextView notifTime;
	}
	
	

}
