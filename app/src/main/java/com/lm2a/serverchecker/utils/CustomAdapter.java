package com.lm2a.serverchecker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.lm2a.serverchecker.R;
import com.lm2a.serverchecker.model.Host;

import java.util.ArrayList;
import java.util.List;


public class CustomAdapter extends BaseAdapter {

    private Activity activity;
    private List<Host> hosts;
    private static LayoutInflater inflater = null;
    public Resources res;
    Host tempValues = null;
    int i = 0;

    public CustomAdapter(Activity activity, List<Host> hosts, Resources resLocal) {
        this.activity = activity;
        this.hosts = hosts;
        res = resLocal;
        inflater = (LayoutInflater) activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount() {
        if (hosts.size() <= 0) {
            return 1;
        }
        return hosts.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {

        public TextView hostName;
        public CheckBox notification;
        public CheckBox email;
        public ImageView checkResult;

    }


    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if (convertView == null) {

            vi = inflater.inflate(R.layout.item_host, null);

            holder = new ViewHolder();
            holder.notification = (CheckBox) vi.findViewById(R.id.cb_notification);
            holder.email = (CheckBox) vi.findViewById(R.id.cb_email);
            holder.hostName = (TextView) vi.findViewById(R.id.tv_host);
            holder.checkResult = (ImageView) vi.findViewById(R.id.iv_check_result);

            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        if (hosts.size() <= 0) {
            holder.hostName.setText("No Data");

        } else {
            tempValues = null;
            tempValues = hosts.get(position);

            holder.hostName.setText(tempValues.getHost());
            holder.notification.setChecked(tempValues.isNotification());
            holder.email.setChecked(tempValues.isEmails());

            if (tempValues.isLastCheck()) {
                holder.checkResult.setImageResource(R.mipmap.green);
            } else {
                holder.checkResult.setImageResource(R.mipmap.red);
            }

        }
        return vi;
    }


}