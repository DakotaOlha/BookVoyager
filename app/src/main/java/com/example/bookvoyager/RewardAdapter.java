package com.example.bookvoyager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    private List<Reward> rewards;

    public RewardAdapter(List<Reward> rewards){
        this.rewards = rewards;
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder {
        ImageView rewardImage;
        TextView rewardText;

        public RewardViewHolder(View itemView) {
            super(itemView);
            rewardImage = itemView.findViewById(R.id.RewardImage);
            rewardText = itemView.findViewById(R.id.RewardText);
        }
    }


    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward, parent, false);
        return new RewardAdapter.RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewards.get(position);
        holder.rewardImage.setImageResource(R.drawable.awards_icon);
        holder.rewardText.setText(reward.description);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

}
