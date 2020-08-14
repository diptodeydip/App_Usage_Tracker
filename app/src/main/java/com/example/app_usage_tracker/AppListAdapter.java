package com.example.app_usage_tracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<AppUsageInfo> appsUsageInfo, appsUsageInfoOriginal;
    private final String TAG = "ahtrap";

    public AppListAdapter(Context context, ArrayList<AppUsageInfo> appsUsageInfo) {
        this.context = context;
        this.appsUsageInfo = appsUsageInfo;
        this.appsUsageInfoOriginal = appsUsageInfo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppUsageInfo app = appsUsageInfo.get(position);
        holder.appName.setText(app.getAppName());
        String time = getTimeFromMillisecond(app.getTimeInForeground());
        holder.appUsage.setText(time);
        holder.appIcon.setImageDrawable(app.getAppIcon());
        String lastUsedTime = ImportantMethods.getTimeInAgoFromMillisecond(app.getLastTimeUsed());
        holder.lastTimeUsed.setText(lastUsedTime);
        holder.parentLayout.setOnClickListener( view -> {
//            Intent intent = new Intent(context, AppDetails.class);
//            context.startActivity(intent);
//            showToast(Long.toString(app.getTimeInForeground()));
            showToast(app.toString());
        });
    }

    @Override
    public int getItemCount() {
        return appsUsageInfo.size();
    }


    public void sortByAppName(boolean ascending){
        Collections.sort(appsUsageInfo, (o1, o2) -> {
            String app1 = o1.getAppName();
            String app2 = o2.getAppName();
            int compare = app1.compareToIgnoreCase(app2);
            if(ascending)
                return compare;
            else
                return -1 * compare;
        });
        notifyDataSetChanged();
    }

    public void sortByPackageName(boolean ascending){
        Collections.sort(appsUsageInfo, (o1, o2) -> {
            String app1 = o1.getPackageName();
            String app2 = o2.getPackageName();
            int compare = app1.compareToIgnoreCase(app2);
            if(ascending)
                return compare;
            else
                return 1 - compare;
        });
        notifyDataSetChanged();
    }

    public void sortByUsageTime(boolean ascending){
        Collections.sort(appsUsageInfo, (o1, o2) -> {
            long app1 = o1.getTimeInForeground();
            long app2 = o2.getTimeInForeground();
            int compare = Long.compare(app1, app2);
            if(ascending)
                return compare;
            else
                return -1 * compare;
        });
        notifyDataSetChanged();
    }

    public void sortByLastOpened(boolean ascending){
        Collections.sort(appsUsageInfo, (o1, o2) -> {
            long app1 = o1.getLastTimeUsed();
            long app2 = o2.getLastTimeUsed();
            int compare = Long.compare(app1, app2);
            if(ascending)
                return compare;
            else
                return -1 * compare;
        });
        notifyDataSetChanged();
    }


    public void clearAll() {
        appsUsageInfo.clear();
        notifyDataSetChanged();
    }

    public void removeAt(int index){
        Toast.makeText(context, appsUsageInfo.get(index).toString(), Toast.LENGTH_SHORT).show();
        appsUsageInfo.remove(index);
//        notifyItemRemoved(index);
//        notifyItemRangeChanged(index, appsUsageInfo.size());
        notifyDataSetChanged();
    }

    public void removeSystemApps(){
        ArrayList<AppUsageInfo> withoutSystemApp = new ArrayList<>();
        int len = appsUsageInfo.size();
        for(AppUsageInfo app:appsUsageInfo){
            if(!app.isSystemApp())
                withoutSystemApp.add(app);
        }
        appsUsageInfo = withoutSystemApp;
        notifyDataSetChanged();
    }

    private String getTimeFromMillisecond(Long time){
        String timeString = "";
        Long second = time / 1000;
        Long min = second / 60;
        Long hour = min / 60;
        Long min_hour = min % 60;
        if(hour > 0)
            timeString += hour + " hour " + min_hour + " min";
        else
            timeString = min_hour + " min";
        return timeString;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void showToast(int message) {
        showToast(Integer.toString(message));
    }

    private void showToast(String... messages) {
        String fullMessage = "";
        for (String message : messages) {
            fullMessage += message + " ";
        }
        showToast(fullMessage);
    }

    private void showToast(int... messages) {
        String fullMessage = "";
        for (int message : messages) {
            fullMessage += message + " ";
        }
        showToast(fullMessage);
    }

    private void showLog(String message) {
        Log.v(TAG, message);
    }

    private void showLog(int message) {
        showLog(Integer.toString(message));
    }

    private void showLog(int... messages) {
        String fullMessage = "";
        for (int message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }

    private void showLog(String... messages) {
        String fullMessage = "";
        for (String message : messages) {
            fullMessage += message + " ";
        }
        showLog(fullMessage);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName, appUsage, lastTimeUsed;
        LinearLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.avatar);
            appName = itemView.findViewById(R.id.app_name);
            appUsage = itemView.findViewById(R.id.usage_time);
            lastTimeUsed = itemView.findViewById(R.id.last_time_used);
            parentLayout = itemView.findViewById(R.id.item_layout);
        }
    }
}
