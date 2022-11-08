package com.example.datasensor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datasensor.fragments.ResultadosFragment;
import com.example.datasensor.fragments.SensoresFragment;

import java.io.Serializable;
import java.text.CollationElementIterator;
import java.util.HashMap;
import java.util.Map;

import io.grpc.internal.SharedResourceHolder;

public class MainActivity extends AppCompatActivity {

    private SensoresFragment sensoresFragment = new SensoresFragment();

    // Toolbar
    private ImageButton toolbar_to_return;
    private ImageView toolbar_logo;
    private TextView toolbar_title;

    public Boolean stopHandlers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        toolbar_to_return = findViewById(R.id.toolbar_to_return);
        toolbar_logo = findViewById(R.id.toolbar_logo);
        toolbar_title = findViewById(R.id.toolbar_title);

        replaceFragmentSensores(sensoresFragment);
    }


    // Controlar pulsacion del boton atras
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        stopHandlers = true;
        if (keyCode == event.KEYCODE_BACK) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                AlertDialog dialog = builder.setMessage(
                        Html.fromHtml("<font color='#000000'>¿Desea salir de DataSensor?</font>")
                        )
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                }
                dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.rosado_medio));
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.rosado_medio));
                    }
                });
                dialog.show();
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                onBackPressed();
            } else if (getSupportFragmentManager().getBackStackEntryCount() == 2) {
                FragResultadosBackPressed();
            }


        }

        return false;
    }

    public void LogoToolbarMostrar() {
        toolbar_logo.setVisibility(View.VISIBLE);
    }

    public void LogoToolbarOcultar() {
        toolbar_logo.setVisibility(View.GONE);
    }

    public void BtnRegresarMostrar() {
        toolbar_to_return.setVisibility(View.VISIBLE);
        toolbar_to_return.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                stopHandlers = true;
                if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    onBackPressed();
                } else if (getSupportFragmentManager().getBackStackEntryCount() == 2) {
                    FragResultadosBackPressed();
                }
            }
        });
    }

    public void BtnRegresarOcultar() {
        toolbar_to_return.setVisibility(View.GONE);
    }

    public void TitleToolbar(String title) {
        toolbar_title.setText(title);
    }

    private void replaceFragmentSensores(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void replaceFragmentResultados(String sensorDB, Map<String, Serializable> docIsRegister) {
        Bundle datoAEnviar = new Bundle();
        datoAEnviar.putString("tipoSensor", sensorDB);
        datoAEnviar.putSerializable("doc", (Serializable) docIsRegister);

        ResultadosFragment resultadosFragment = new ResultadosFragment();
        resultadosFragment.setArguments(datoAEnviar);
        replaceFragment(resultadosFragment);
    }

    public void FragResultadosBackPressed() {
        onBackPressed();
        onBackPressed();
    }

    public void mostrarDialogConexionRed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_sin_conexion_red, null);

        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();

        Button btn_wifi_error = view.findViewById(R.id.dialog_btn_wifi_error);
        btn_wifi_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

}

