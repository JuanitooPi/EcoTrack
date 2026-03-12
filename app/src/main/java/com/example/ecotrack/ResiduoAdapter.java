package com.example.ecotrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ResiduoAdapter extends RecyclerView.Adapter<ResiduoAdapter.ViewHolder> {

    private List<Residuo> residuos;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Residuo residuo);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(Residuo residuo);
    }

    public ResiduoAdapter(List<Residuo> residuos, OnItemClickListener listener,
                          OnItemLongClickListener longClickListener) {
        this.residuos = residuos;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_residuo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Residuo residuo = residuos.get(position);

        String fechaFormateada = DateUtils.formatearFechaMostrar(residuo.getFecha());
        holder.tvTipo.setText(residuo.getTipo());
        holder.tvCantidad.setText(String.format("%.2f %s", residuo.getCantidad(), residuo.getUnidad()));
        holder.tvFechaHora.setText(String.format("%s %s", fechaFormateada, residuo.getHora()));
        holder.tvUbicacion.setText(residuo.getUbicacion());

        // Indicador de sincronización
        if (residuo.getSincronizado() == 1) {
            holder.tvSincronizado.setText("✓ Sincronizado");
            holder.tvSincronizado.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvSincronizado.setText("⏳ Pendiente");
            holder.tvSincronizado.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(residuo));
        holder.itemView.setOnLongClickListener(v -> longClickListener.onItemLongClick(residuo));
    }

    @Override
    public int getItemCount() {
        return residuos.size();
    }

    public void actualizarLista(List<Residuo> nuevosResiduos) {
        this.residuos = nuevosResiduos;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvCantidad, tvFechaHora, tvUbicacion, tvSincronizado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
            tvSincronizado = itemView.findViewById(R.id.tvSincronizado);
        }
    }
}