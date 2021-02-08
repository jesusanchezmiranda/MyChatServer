package com.jesus.mychatserver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private boolean run = true;
    private Socket client;
    private DataInputStream flujoE;
    private DataOutputStream flujoS;
    private Thread listeningThread;
    private EditText et1;
    private Button btSend;
    private TextView tv1;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        et1 = findViewById(R.id.et1);
        tv1 = findViewById(R.id.tv1);

        UtilThread.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                startClient("10.0.2.2", 5000);
            }
        });

        findViewById(R.id.btSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et1.getText().toString();
                Thread hebra = new Thread(){
                    @Override
                    public void run() {
                        try {
                            Log.v("XYZ", text);
                            flujoS.writeUTF(text);
                        } catch (IOException ex) {
                            Log.v("XYZ", ex.getLocalizedMessage());
                        }
                    }
                };
                hebra.start();
                et1.setText("");
            }
        });

    }


    public void startClient(String host, int port){
        try {
            client = new Socket(host, port);
            flujoE = new DataInputStream(client.getInputStream());
            flujoS = new DataOutputStream(client.getOutputStream());
            listeningThread = new Thread(){
                @Override
                public void run() {
                    while(run){
                        try {
                            text = flujoE.readUTF();
                            Log.v("XYZ", text);
                            tv1.post(new Runnable() {
                                @Override
                                public void run() {
                                    tv1.append(text);
                                }
                            });

                        } catch (IOException ex) {

                        }
                    }
                }

            };
            listeningThread.start();



        } catch (IOException ex) {


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeClient();
    }

    public void closeClient(){
        String texto = "XYZXY";

        new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    Log.v("XXXXX", texto);
                    flujoS.writeUTF(texto);
                } catch (IOException ex) {
                }

            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            /* METODO PARA CERRAR LA APLICACION */

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Are you sure?")
                    .setPositiveButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                closeClient();
                                finishAndRemoveTask();

                            } else {
                                closeClient();
                                finish();
                                System.exit(0);
                            }

                        }
                    });
            builder.create();
            builder.show();

        }

        return super.onOptionsItemSelected(item);
    }
}