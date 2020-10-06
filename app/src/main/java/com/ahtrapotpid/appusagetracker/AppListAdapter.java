package com.ahtrapotpid.appusagetracker;

import android.content.Context;
import android.content.Intent;
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
    public static final String TAG = "temp";

    public AppListAdapter(Context context, ArrayList<AppUsageInfo> appsUsageInfo) {
        this.context = context;
        this.appsUsageInfo = appsUsageInfo;
        this.appsUsageInfoOriginal = new ArrayList<>(appsUsageInfo);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_list_item, parent, false);
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
        String lastUsedTime = ImportantStuffs.getTimeInAgoFromMillisecond(app.getLastTimeUsed());
        holder.lastTimeUsed.setText(lastUsedTime);
        holder.parentLayout.setOnClickListener( view -> gotoAppDetails(app.getPackageName()));
    }

    @Override
    public int getItemCount() {
        return appsUsageInfo.size();
    }


    public void sort(int sortBy, boolean ascending){
        switch (sortBy){
            case R.id.sort_by_last_installed:
                sortByLastInstalled(ascending);
                break;
            case R.id.sort_by_last_used:
                sortByLastOpened(ascending);
                break;
            case R.id.sort_by_name:
                sortByAppName(ascending);
                break;
            default:
                sortByUsageTime(ascending);
        }
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

    public void sortByUsageTime(boolean ascending){
        Collections.sort(appsUsageInfo, (o1, o2) -> {
            long app1 = o1.getTimeInForeground();
            long app2 = o2.getTimeInForeground();
            int compare = Long.compare(app1, app2);
            if(compare == 0){
                String app1name = o1.getAppName();
                String app2Name = o2.getAppName();
                int compare2 = app1name.compareToIgnoreCase(app2Name);
                return compare2;
            }
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
            if(compare == 0){
                String app1name = o1.getAppName();
                String app2Name = o2.getAppName();
                int compare2 = app1name.compareToIgnoreCase(app2Name);
                return compare2;
            }
            if(ascending)
                return compare;
            else
                return -1 * compare;
        });
        notifyDataSetChanged();
    }

    public void sortByLastInstalled(boolean ascending){
        Collections.sort(appsUsageInfo, (o1, o2) -> {
            long app1 = o1.getInstallationTime();
            long app2 = o2.getInstallationTime();
            int compare = Long.compare(app1, app2);
            if(compare == 0){
                String app1name = o1.getAppName();
                String app2Name = o2.getAppName();
                int compare2 = app1name.compareToIgnoreCase(app2Name);
                return compare2;
            }
            if(ascending)
                return compare;
            else
                return -1 * compare;
        });
        notifyDataSetChanged();
    }

    public void filterApps(boolean filterSystemApp, boolean filterUnusedApp){
//        String debugString = "";
//        if(filterSystemApp && filterUnusedApp)
//            debugString = "Do not filter";
//        else if(filterSystemApp && !filterUnusedApp)
//            debugString = "Filter system apps only";
//        else if(!filterSystemApp && filterUnusedApp)
//            debugString = "Filter unused apps only";
//        else if(!filterSystemApp && !filterUnusedApp)
//            debugString = "Filter system and unused apps";
//        showToast(debugString);

        ArrayList<AppUsageInfo> filteredApps = new ArrayList<>();
        for(AppUsageInfo app:appsUsageInfoOriginal){
            if( !(filterSystemApp && app.isSystemApp()) && !(filterUnusedApp && app.getTimeInForeground() == 0) )
                filteredApps.add(app);
        }
        appsUsageInfo = filteredApps;
        notifyDataSetChanged();
    }

    public void gotoAppDetails(String packageName){
        Intent intent = new Intent(context, AppDetails.class);
        intent.putExtra("packageName", packageName);
        intent.putExtra("mode", 1 - AppListTabbed.currentTabIndex);
        context.startActivity(intent);
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
            parentLayout = itemView.findViewById(R.id.app_list_item_layout);
        }
    }
}
