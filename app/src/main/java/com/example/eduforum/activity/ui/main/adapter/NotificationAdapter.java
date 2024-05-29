package com.example.eduforum.activity.ui.main.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eduforum.R;
import com.example.eduforum.activity.ui.main.fragment.NotificationViewState;
import com.example.eduforum.databinding.ItemNotiBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    Context context;
    private List<NotificationViewState> notificationList;
    public NotificationAdapter(Context context, List<NotificationViewState> notificationList) {
        this.context = context;
        if (notificationList == null) {
            this.notificationList = new ArrayList<>();
        }
        else {
            this.notificationList = notificationList;
        }
    }

    public void setNotificationList(List<NotificationViewState> notificationList) {
        this.notificationList = notificationList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemNotiBinding itemNotiBinding = ItemNotiBinding.inflate(layoutInflater, parent, false);
        return new NotificationViewHolder(itemNotiBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.binding.moreOptionImageButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.noti_option_menu, popupMenu.getMenu());
            //popupMenu.setOnMenuItemClickListener(item -> {
            //TODO: Handle menu item click
            //});
            popupMenu.show();
        });
        holder.binding.notiCardView.setOnClickListener(v -> {
            ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.readComment);
            assert colorStateList != null;
            v.setBackgroundColor(colorStateList.getDefaultColor());
        });

    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ItemNotiBinding binding;
        public NotificationViewHolder(ItemNotiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bindingComponents(NotificationViewState notificationViewState) {
            if(notificationViewState.getTriggerBy().getProfilePicture()!=null){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(notificationViewState.getTriggerBy().getProfilePicture());
                Glide.with(binding.getRoot().getContext())
                        .load(storageReference)
                        .into(binding.avatarCardView);
            }
            //String communityName = notificationViewState.getCommunity().getCommunityName();
            //binding.commuNotiTextView.setText(communityName);
            //binding.timeNotiTextView.setText(notificationViewState.getTimeStamp().toDate().toString());
            switch (notificationViewState.getType()) {
                case 1:
                    binding.contentNotiTextView.setText(notificationViewState.getTriggerBy().getName() + " đã đăng một bình luận mới vào bài viết của bạn.");
                    break;
                case 2:
                    binding.contentNotiTextView.setText(notificationViewState.getTriggerBy().getName() + " đăng một post mới trong cộng đồng.");
                    break;
                case 3:
                    binding.contentNotiTextView.setText(notificationViewState.getTriggerBy().getName() + " đã trả lời bình luận của bạn.");
                    break;
                case 4: // new announcement
                    binding.contentNotiTextView.setText(notificationViewState.getTriggerBy().getName() + " đã bình luận bài viết bạn đã thích.");
                    break;
                case 5:
                    binding.contentNotiTextView.setText(notificationViewState.getTriggerBy().getName() + " đã bình luận vào bài viết bạn quan tâm.");
                    break;
            }

        }

        public void bind(NotificationViewState notificationViewState) {
            bindingComponents(notificationViewState);

        }
    }
}
