package com.ahtrapotpid.appusagetracker.applist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahtrapotpid.appusagetracker.R;
import com.ahtrapotpid.appusagetracker.TargetInfo;

import java.util.ArrayList;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder>  {
    Context context;
    ArrayList<TargetInfo> targetsInfo;
    int mode;

    public HistoryListAdapter(Context context, ArrayList<TargetInfo> targetsInfo, int mode) {
        this.context = context;
        this.targetsInfo = targetsInfo;
        this.mode = mode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.target_history_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TargetInfo targetInfo = targetsInfo.get(position);

        holder.date.setText(targetInfo.getStringDate());
        holder.usage.setText("Usage: "+targetInfo.getStringUsage());
        holder.target.setText("Target: "+targetInfo.getStringTarget());
        holder.percentage.setText(targetInfo.getStringPercentage());
    }

    @Override
    public int getItemCount() {
        return targetsInfo.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, percentage, usage, target;
        LinearLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.target_date);
            percentage = itemView.findViewById(R.id.used_percentage);
            usage = itemView.findViewById(R.id.usage_time);
            target = itemView.findViewById(R.id.target_time);
            parentLayout = itemView.findViewById(R.id.target_history_list_item_layout);
        }
    }
}
