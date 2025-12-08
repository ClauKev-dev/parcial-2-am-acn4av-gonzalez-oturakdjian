package com.example.final_am_acn4av_gonzalez_oturakdjian;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class LootboxProductAdapter extends RecyclerView.Adapter<LootboxProductAdapter.LootboxProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public LootboxProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public LootboxProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lootbox_product, parent, false);
        return new LootboxProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LootboxProductViewHolder holder, int position) {
        Product product = productList.get(position);

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.ic_launcher_background);
        }
        
        holder.tvName.setText(product.getName());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class LootboxProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName;

        public LootboxProductViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}

