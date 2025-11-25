package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    private List<Product> carrito;

    private CarritoActivity activity;

    public CarritoAdapter(CarritoActivity activity, List<Product> carrito) {
        this.activity = activity;
        this.carrito = carrito;
    }

    @Override
    public CarritoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CarritoViewHolder holder, int position) {
        Product producto = carrito.get(position);

        if (producto.getImageUrl() != null && !producto.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(producto.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.ivProduct);
        } else {
            holder.ivProduct.setImageResource(R.drawable.ic_launcher_background);
        }
        
        holder.tvName.setText(producto.getName());
        holder.tvPrice.setText(producto.getPriceFormatted());
        holder.tvQuantity.setText(String.valueOf(producto.getQuantity()));

        holder.btnIncrease.setOnClickListener(v -> {
            producto.increaseQuantity();
            CarritoManager.actualizarCantidadProducto(producto, producto.getQuantity());
            notifyItemChanged(position);
            activity.actualizarTotal();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (producto.getQuantity() > 1) {
                producto.setQuantity(producto.getQuantity() - 1);
                CarritoManager.actualizarCantidadProducto(producto, producto.getQuantity());
                notifyItemChanged(position);
            } else {
                CarritoManager.eliminarProducto(producto);
                carrito = CarritoManager.getCarrito();
                notifyDataSetChanged();
            }
            activity.actualizarTotal();
        });
    }


    @Override
    public int getItemCount() {
        return carrito.size();
    }

    static class CarritoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity;
        Button btnIncrease, btnDecrease;

        public CarritoViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
        }
    }

}
