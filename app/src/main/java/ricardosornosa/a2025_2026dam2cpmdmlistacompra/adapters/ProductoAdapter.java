package ricardosornosa.a2025_2026dam2cpmdmlistacompra.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.List;

import ricardosornosa.a2025_2026dam2cpmdmlistacompra.Constantes;
import ricardosornosa.a2025_2026dam2cpmdmlistacompra.MainActivity;
import ricardosornosa.a2025_2026dam2cpmdmlistacompra.R;
import ricardosornosa.a2025_2026dam2cpmdmlistacompra.models.ProductoModel;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoVH> {
    private List<ProductoModel> objects;
    private int resource;
    private Context context;

    private SharedPreferences sp;
    private Gson gson;

    public ProductoAdapter(List<ProductoModel> objects, int resource, Context context) {
        this.objects = objects;
        this.resource = resource;
        this.context = context;

        this.sp = context.getSharedPreferences(Constantes.DATOS,
                context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    private void guardarInformacion() {
        String productosJson = gson.toJson(objects);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constantes.LISTA_PRODUCTOS, productosJson);
        editor.apply();
    }

    @NonNull
    @Override
    public ProductoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View productoView = LayoutInflater.from(context).inflate(resource, null);
        productoView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return new ProductoVH(productoView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoVH holder, int position) {
        ProductoModel p = objects.get(position);
        holder.lblNombre.setText(p.getNombre());
        holder.txtCantidad.setText(String.valueOf(p.getCantidad()));
        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarEliminar(holder.getBindingAdapterPosition()).show();
            }
        });

        holder.txtCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int cantidad = Integer.parseInt(s.toString());
                    p.setCantidad(cantidad);
                } catch (NumberFormatException nfe) {
                    holder.txtCantidad.setHint("0");
                    p.setCantidad(0);
                } catch (Exception e) {
                    Toast.makeText(
                            context,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modificarProducto(p).show();
            }
        });
    }

    private AlertDialog modificarProducto(ProductoModel p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Editar producto");
        builder.setCancelable(false);

        View productoViewAlert = LayoutInflater.from(context)
                .inflate(R.layout.producto_view_alert, null);
        EditText txtNombre = productoViewAlert.findViewById(
                R.id.txtNombreProductoViewAlert
        );
        EditText txtCantidad = productoViewAlert.findViewById(
                R.id.txtCantidadProductoViewAlert
        );
        EditText txtImporte = productoViewAlert.findViewById(
                R.id.txtImporteProductoViewAlert
        );
        TextView lblTotal = productoViewAlert.findViewById(
                R.id.lblTotalProductoViewAlert
        );
        builder.setView(productoViewAlert);

        TextWatcher tw = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {

                    int cantidad = Integer.parseInt(txtCantidad.getText().toString());
                    float importe = Float.parseFloat(txtImporte.getText().toString());
                    float total = cantidad * importe;

                    NumberFormat nf = NumberFormat.getCurrencyInstance();
                    lblTotal.setText(nf.format(total));

                } catch (Exception e) {
                    Toast.makeText(
                            context,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };

        txtCantidad.addTextChangedListener(tw);
        txtImporte.addTextChangedListener(tw);

        txtNombre.setText(p.getNombre());
        txtCantidad.setText(String.valueOf(p.getCantidad()));
        txtImporte.setText(String.valueOf(p.getImporte()));

        builder.setNegativeButton("CANCELAR", null);
        builder.setPositiveButton("MODIFICAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombre = txtNombre.getText().toString();
                String cantidadS = txtCantidad.getText().toString();
                String importeS = txtImporte.getText().toString();

                if (!nombre.isEmpty() && !cantidadS.isEmpty() &&
                        !importeS.isEmpty()) {
                    int cantidad = Integer.parseInt(cantidadS);
                    float importe = Float.parseFloat(importeS);

                    p.setNombre(nombre);
                    p.setCantidad(cantidad);
                    p.setImporte(importe);
                    p.actualizarTotal();

                    notifyItemChanged(objects.indexOf(p));

                    guardarInformacion();
                }
            }
        });

        return builder.create();
    }

    private AlertDialog confirmarEliminar(int posicion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¿Seguro?");
        builder.setCancelable(true);

        builder.setNegativeButton("NO", null);
        builder.setPositiveButton("SÍ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                objects.remove(posicion);
                notifyItemRemoved(posicion);

                guardarInformacion();
            }
        });

        return builder.create();
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public class ProductoVH extends RecyclerView.ViewHolder {
        private TextView lblNombre;
        private EditText txtCantidad;
        private ImageButton btnEliminar;

        public ProductoVH(@NonNull View itemView) {
            super(itemView);
            lblNombre = itemView.findViewById(R.id.lblNombreProductoViewModel);
            txtCantidad = itemView.findViewById(R.id.txtCantidadProductoViewModel);
            btnEliminar = itemView.findViewById(R.id.btnEliminarProductoViewModel);
        }
    }
}
