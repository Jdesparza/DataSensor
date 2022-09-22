package com.example.datasensor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datasensor.R;

import java.util.ArrayList;

public class ItemInformacion_Resultados_RV_Adapter extends RecyclerView.Adapter<ItemInformacion_Resultados_RV_Adapter.ViewHolderInfo> {

    ArrayList<String> tituloInfo;
    ArrayList<String> resultadoInfo;

    public ItemInformacion_Resultados_RV_Adapter(ArrayList<String> tituloInfo, ArrayList<String> resultadoInfo) {
        this.tituloInfo = tituloInfo;
        this.resultadoInfo = resultadoInfo;
    }

    @Override
    public ViewHolderInfo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_informacion_resultados, null, false);
        return new ViewHolderInfo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderInfo holder, int position) {
        holder.asignarInfo(tituloInfo.get(position), resultadoInfo.get(position));
    }

    @Override
    public int getItemCount() {
        return tituloInfo.size();
    }

    public static class ViewHolderInfo extends RecyclerView.ViewHolder {

        TextView tv_titleinfo_resultados, tv_resultinfo_resultados;

        public ViewHolderInfo(@NonNull View itemView) {
            super(itemView);
            tv_titleinfo_resultados = (TextView) itemView.findViewById(R.id.tv_titleinfo_resultados);
            tv_resultinfo_resultados = (TextView) itemView.findViewById(R.id.tv_resultinfo_resultados);
        }

        public void asignarInfo(String tituloInfo, String resultadoInfo) {
            tv_titleinfo_resultados.setText(tituloInfo);
            tv_resultinfo_resultados.setText(resultadoInfo);
        }
    }
}
