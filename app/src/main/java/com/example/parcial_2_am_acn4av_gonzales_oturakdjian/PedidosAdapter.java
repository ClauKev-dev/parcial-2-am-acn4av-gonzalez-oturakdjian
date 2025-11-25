package com.example.parcial_2_am_acn4av_gonzales_oturakdjian;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<Order> pedidosList;
    private PedidosEnCursoActivity activity;
    private Map<Integer, Boolean> expandedItems;

    public PedidosAdapter(PedidosEnCursoActivity activity, List<Order> pedidosList) {
        this.activity = activity;
        this.pedidosList = pedidosList;
        this.expandedItems = new HashMap<>();
    }

    @Override
    public PedidoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PedidoViewHolder holder, int position) {
        Order pedido = pedidosList.get(position);

        // Formatear fecha
        String fecha = PedidosEnCursoActivity.formatearFecha(pedido.getCreatedAt());
        holder.tvFecha.setText(fecha);

        // Formatear total
        String total = String.format(Locale.getDefault(), "Total: $%.2f", pedido.getTotal());
        holder.tvTotal.setText(total);

        // Mostrar estado
        if (holder.tvStatus != null) {
            String status = pedido.getStatus();
            if (status == null) {
                status = "en_curso";
            }
            String statusText = "";
            int statusColor = 0xFF666666;
            switch (status) {
                case "en_curso":
                    statusText = "En Curso";
                    statusColor = 0xFF70CC7C;
                    break;
                case "completado":
                    statusText = "Completado";
                    statusColor = 0xFF4CAF50;
                    break;
                case "cancelado":
                    statusText = "Cancelado";
                    statusColor = 0xFFFF4444;
                    break;
                default:
                    statusText = status;
                    break;
            }
            holder.tvStatus.setText(statusText);
            holder.tvStatus.setTextColor(statusColor);
        }

        if (pedido.getProducts() != null && !pedido.getProducts().isEmpty()) {
            ProductosPedidoAdapter productosAdapter = new ProductosPedidoAdapter(pedido.getProducts());
            holder.recyclerProductos.setLayoutManager(new LinearLayoutManager(activity));
            holder.recyclerProductos.setAdapter(productosAdapter);
        }

        boolean isExpanded = expandedItems.get(position) != null && expandedItems.get(position);
        holder.llProductos.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.ivExpand.setRotation(isExpanded ? 180 : 0);

        holder.itemView.setOnClickListener(v -> {
            boolean newExpandedState = !isExpanded;
            expandedItems.put(position, newExpandedState);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return pedidosList.size();
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvTotal, tvStatus;
        ImageView ivExpand;
        LinearLayout llProductos;
        RecyclerView recyclerProductos;

        public PedidoViewHolder(View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivExpand = itemView.findViewById(R.id.ivExpand);
            llProductos = itemView.findViewById(R.id.llProductos);
            recyclerProductos = itemView.findViewById(R.id.recyclerProductos);
        }
    }

    private static class ProductosPedidoAdapter extends RecyclerView.Adapter<ProductosPedidoAdapter.ProductoViewHolder> {
        private List<Product> productos;

        public ProductosPedidoAdapter(List<Product> productos) {
            this.productos = productos;
        }

        @Override
        public ProductoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_pedido, parent, false);
            return new ProductoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductoViewHolder holder, int position) {
            Product producto = productos.get(position);

            // Cargar imagen
            if (producto.getImageUrl() != null && !producto.getImageUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(producto.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .centerCrop()
                        .into(holder.ivProduct);
            } else {
                holder.ivProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            holder.tvName.setText(producto.getName());
            holder.tvQuantity.setText("Cantidad: " + producto.getQuantity());
            double subtotal = producto.getPrice() * producto.getQuantity();
            holder.tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        }

        @Override
        public int getItemCount() {
            return productos != null ? productos.size() : 0;
        }

        static class ProductoViewHolder extends RecyclerView.ViewHolder {
            ImageView ivProduct;
            TextView tvName, tvQuantity, tvPrice;

            public ProductoViewHolder(View itemView) {
                super(itemView);
                ivProduct = itemView.findViewById(R.id.ivProduct);
                tvName = itemView.findViewById(R.id.tvName);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }
        }
    }
}

