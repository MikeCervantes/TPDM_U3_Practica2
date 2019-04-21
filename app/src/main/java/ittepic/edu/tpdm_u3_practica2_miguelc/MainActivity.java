package ittepic.edu.tpdm_u3_practica2_miguelc;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText nombre, sabor, precio;
    Button insertar, eliminar, consultar;
    Spinner spinner;
    List<Alimento_Bebida> datosAliBeb;
    List<String> ramas;
    ListView listado;
    FirebaseFirestore servicioBaseDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nombre = findViewById(R.id.editText);
        sabor = findViewById(R.id.editText2);
        precio = findViewById(R.id.editText3);

        insertar = findViewById(R.id.button);
        eliminar = findViewById(R.id.button2);
        consultar = findViewById(R.id.button3);

        listado = findViewById(R.id.listado);
        spinner = findViewById(R.id.spinner);
        servicioBaseDatos = FirebaseFirestore.getInstance();

        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarAliBeb();
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarAliBeb();
            }
        });

        consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consultarTodos();
            }
        });
    }

    private void consultarTodos() {
        datosAliBeb=new ArrayList<>();
        ramas=new ArrayList<>();
        servicioBaseDatos.collection("Alimento").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot registro:task.getResult()){
                        Map<String,Object> datos=registro.getData();

                        ramas.add(registro.getId());
                        Alimento_Bebida aliBeb=new Alimento_Bebida(datos.get("nombre").toString(),datos.get("sabor").toString(),
                                datos.get("precio").toString());
                        datosAliBeb.add(aliBeb);
                    }
                    ponerloEnListView();
                }else{
                    Toast.makeText(MainActivity.this,"NO HAY DATOS DE ALIMENTOS",Toast.LENGTH_SHORT).show();
                }
            }
        });
        servicioBaseDatos.collection("Bebida").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot registro:task.getResult()){
                        Map<String,Object> datos=registro.getData();

                        ramas.add(registro.getId());
                        Alimento_Bebida aliBeb=new Alimento_Bebida(datos.get("nombre").toString(),datos.get("sabor").toString(),
                                datos.get("precio").toString());
                        datosAliBeb.add(aliBeb);
                    }
                    ponerloEnListView();
                }else{
                    Toast.makeText(MainActivity.this,"NO HAY DATOS DE BEBIDAS",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ponerloEnListView() {
        if(datosAliBeb.size()==0){
            return;
        }
        String[] datos=new String[datosAliBeb.size()];
        for(int i=0;i<datos.length;i++){
            Alimento_Bebida aliBeb=datosAliBeb.get(i);
            datos[i]=aliBeb.nombre+"\n"+aliBeb.sabor+"\n"+aliBeb.precio;
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,datos);
        listado.setAdapter(adapter);

        listado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alerta=new AlertDialog.Builder(MainActivity.this);
                final EditText ide=new EditText(MainActivity.this);

                ide.setText(ramas.get(position));

                alerta.setTitle("ATENCÓN").setMessage("Este es el ID del registro:")
                        .setView(ide)
                        .setPositiveButton("Ok",null)
                        .show();
            }
        });
    }

    private void eliminarAliBeb() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        final View contenido=getLayoutInflater().inflate(R.layout.alert,null);
        alerta.setTitle("ATENCIÓN")
                .setMessage("ESCRIBA EL NOMBRE DEL ALIMENTO/BEBIDA")
                .setView(contenido)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText idEliminar=contenido.findViewById(R.id.idEliminar);
                        Spinner categoria=contenido.findViewById(R.id.categoria);

                        if (categoria.getSelectedItemPosition()==0){
                            Toast.makeText(MainActivity.this, "SELECCIONA UNA CATEGORÍA", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (idEliminar.getText().toString().isEmpty()) {
                            Toast.makeText(MainActivity.this, "EL ID ESTÁ VACÍO", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        eliminarAliBeb(idEliminar.getText().toString(),categoria.getSelectedItem().toString());
                    }
                }).setNegativeButton("Cancelar", null).show();
    }

    private void eliminarAliBeb(String idEliminar,String categoria) {
        servicioBaseDatos.collection(categoria).document(idEliminar).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this,"SE ELIMINÓ!",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"NO SE ENCONTRÓ COINCIDENCIA",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertarAliBeb() {
        if (spinner.getSelectedItemPosition() == 0) {
            Toast.makeText(MainActivity.this, "SELECCIONA UNA CATEGORÍA", Toast.LENGTH_LONG).show();
            return;
        }
        Alimento_Bebida aliBeb = new Alimento_Bebida(nombre.getText().toString(), sabor.getText().toString(), precio.getText().toString());
        servicioBaseDatos.collection(spinner.getSelectedItem().toString()).document(nombre.getText().toString()).set(aliBeb)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(MainActivity.this, "SE INSERTÓ CORRECTAMENTE", Toast.LENGTH_SHORT).show();
                        nombre.setText("");
                        sabor.setText("");
                        precio.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "ERROR, NO SE PUDO INSERTAR", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
