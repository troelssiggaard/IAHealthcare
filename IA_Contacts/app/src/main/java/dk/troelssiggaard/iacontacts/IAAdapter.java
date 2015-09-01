package dk.troelssiggaard.iacontacts;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ts.
 */
public class IAAdapter extends BaseAdapter {
        private Context context;
        private List<ListItem> items;

        public IAAdapter(Context context, List<ListItem> items) {
            this.context = context;
            this.items = items;
        }

        private class ViewHolder {
            ImageView profilePicture;
            TextView textView;           // Name
            TextView textView2;          // Title, Department, Hospital
            ImageView interruptStatus;   // Interruptibility icon
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.listview_single, null);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.textView);
                holder.textView2 = (TextView) convertView.findViewById(R.id.textView2);
                holder.profilePicture = (ImageView) convertView.findViewById(R.id.profilePicture);
                holder.interruptStatus = (ImageView) convertView.findViewById(R.id.interruptStatusIcon);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListItem item = (ListItem) getItem(position);

            holder.profilePicture.setImageResource(item.getProfilePicture());
            holder.textView.setText(item.getName());
            holder.textView2.setText(item.getTitle()+", "+item.getDepartment());
            holder.interruptStatus.setImageResource(item.getIntPicture());

            return convertView;
        }


        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.indexOf(getItem(position));
        }


}
