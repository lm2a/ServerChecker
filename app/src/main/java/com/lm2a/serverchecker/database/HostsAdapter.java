package com.lm2a.serverchecker.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lm2a.serverchecker.R;
import com.lm2a.serverchecker.model.Host;

import java.util.ArrayList;

public class HostsAdapter extends ArrayAdapter<Host> {
    public HostsAdapter(Context context, ArrayList<Host> hosts) {
       super(context, 0, hosts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       // Get the data item for this position
/*       Host host = getItem(position);
       // Check if an existing view is being reused, otherwise inflate the view
       if (convertView == null) {
          convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_host, parent, false);
       }
       // Lookup view for data population
       TextView tvHost = (TextView) convertView.findViewById(R.id.tvHost);
       TextView tvPrt = (TextView) convertView.findViewById(R.id.tvPort);
       // Populate the data into the template view using the data object
        tvHost.setText(host.getHost());
        tvPrt.setText(host.getPort());
       // Return the completed view to render on screen*/
       return convertView;
   }
}