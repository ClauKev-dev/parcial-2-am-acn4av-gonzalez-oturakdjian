package com.example.final_am_acn4av_gonzalez_oturakdjian;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder> {
    private List<PaymentMethod> paymentMethods;
    private OnPaymentMethodClickListener listener;
    private String selectedPaymentMethodId;

    public interface OnPaymentMethodClickListener {
        void onPaymentMethodClick(PaymentMethod paymentMethod);
    }

    public PaymentMethodAdapter(List<PaymentMethod> paymentMethods, OnPaymentMethodClickListener listener) {
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    public void setSelectedPaymentMethodId(String id) {
        this.selectedPaymentMethodId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod paymentMethod = paymentMethods.get(position);
        holder.bind(paymentMethod, paymentMethod.getId().equals(selectedPaymentMethodId));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodClick(paymentMethod);
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods != null ? paymentMethods.size() : 0;
    }

    static class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPaymentMethodName;
        private TextView tvPaymentMethodNumber;
        private ImageView ivSelected;

        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaymentMethodName = itemView.findViewById(R.id.tv_payment_method_name);
            tvPaymentMethodNumber = itemView.findViewById(R.id.tv_payment_method_number);
            ivSelected = itemView.findViewById(R.id.iv_selected);
        }

        public void bind(PaymentMethod paymentMethod, boolean isSelected) {
            tvPaymentMethodName.setText(paymentMethod.getDisplayName());
            tvPaymentMethodNumber.setText(paymentMethod.getDisplayNumber());
            ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }
}



