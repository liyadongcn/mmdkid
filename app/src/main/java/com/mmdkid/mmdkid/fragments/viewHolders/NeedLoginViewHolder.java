package com.mmdkid.mmdkid.fragments.viewHolders;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.mmdkid.mmdkid.LoginActivity;
import com.mmdkid.mmdkid.R;
import com.mmdkid.mmdkid.models.Model;

public class NeedLoginViewHolder extends ModelViewHolder {
    private static final String TAG = "NeedLoginViewHolder";

    private TextView mLoginView;

    public NeedLoginViewHolder(final View itemView) {
        super(itemView);
        mLoginView = (TextView) itemView.findViewById(R.id.tvLogin);
        mLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(itemView.getContext(), LoginActivity.class);
                itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public void bindHolder(Model model) {

    }
}
