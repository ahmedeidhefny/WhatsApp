package com.ahmed.eid.whatsapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class InboxChatAdapter extends RecyclerView.Adapter<InboxChatAdapter.InboxChatViewHolder> {

    List<MassageModel> massages;

    FirebaseAuth mAuthn;

    public InboxChatAdapter(List<MassageModel> massages) {
        this.massages = massages;
    }

    @NonNull
    @Override
    public InboxChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View myView;
        myView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_inbox_chat, viewGroup, false);
        return new InboxChatViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxChatViewHolder holder, int i) {
        MassageModel massage = massages.get(i);
        if (massage.getmType().equals("text")) {
            holder.massageReceiver.setVisibility(View.GONE);
            if (massage.getmFrom().equals(mAuthn.getCurrentUser().getUid())) {
                holder.massageSender.setText(massage.getmText());
            } else {
                holder.massageSender.setVisibility(View.GONE);
                holder.massageReceiver.setVisibility(View.VISIBLE);
                holder.massageReceiver.setText(massage.getmText());
            }
        }

    }

    @Override
    public int getItemCount() {
        return massages.size();
    }

    public class InboxChatViewHolder extends RecyclerView.ViewHolder {

        TextView massageReceiver, massageSender;

        public InboxChatViewHolder(@NonNull View itemView) {
            super(itemView);
            massageSender = itemView.findViewById(R.id.massage_sender_text);
            massageReceiver = itemView.findViewById(R.id.massage_receiver_text);
            mAuthn = FirebaseAuth.getInstance();
        }
    }
}
