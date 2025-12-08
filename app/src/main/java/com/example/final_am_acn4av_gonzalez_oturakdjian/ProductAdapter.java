package com.example.final_am_acn4av_gonzalez_oturakdjian;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    private Context context;
    private List<Product> productList;
    private OnAddToCartListener listener;

    public ProductAdapter(Context context, List<Product> productList, OnAddToCartListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
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
        String precioFormateado = String.format(java.util.Locale.getDefault(), "$%.2f", product.getPrice());
        holder.tvPrice.setText(precioFormateado);

        holder.btnAddToCart.setOnClickListener(v -> {
            Toast.makeText(context, product.getName() + " agregado al carrito", Toast.LENGTH_SHORT).show();
            if (listener != null) {
                listener.onAddToCart(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice;
        Button btnAddToCart;

        public ProductViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}
