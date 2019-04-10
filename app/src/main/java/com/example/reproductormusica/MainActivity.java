package com.example.reproductormusica;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    ListView trackListView;

    TextView nombreCancionActualTextView;
    TextView trackProgressTextView;
    ImageView previousTrackImageView;
    ImageView playImageView;
    ImageView nextImageView;

    ImageView soundImageview;
    SeekBar volumeSeekBar;
    SeekBar trackSeekBar;

    ArrayList<Integer> tracksID;
    ArrayList<String> tracksNames;

    int trackIdActual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        trackListView = findViewById(R.id.tracksListView);
        nombreCancionActualTextView = findViewById(R.id.nombreCancionActual);
        previousTrackImageView = findViewById(R.id.previousButton);
        playImageView = findViewById(R.id.playButton);
        nextImageView = findViewById(R.id.nextButton);
        soundImageview = findViewById(R.id.volumeImageView);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        trackSeekBar = findViewById(R.id.trackSeekBar);
        trackProgressTextView = findViewById(R.id.trackProgressTextView);

        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        trackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mediaPlayer.seekTo(progress);
                }
                actualizarTrackProgressText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        tracksID = new ArrayList<>();
        tracksNames = new ArrayList<>();

        // Poblando la lista
        Field[] elementosRaw = R.raw.class.getFields();
        for (Field field : elementosRaw){
            try{
                int id = field.getInt(field);
                tracksID.add(id);
                tracksNames.add(getResources().getResourceEntryName(id));
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        tracksNames = corregirNombres(tracksNames);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tracksNames);
        trackListView.setAdapter(adapter);

        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("Posicion: " + String.valueOf(position));
                reproducirCancion(position);
            }
        });

        nombreCancionActualTextView.setText(tracksNames.get(0));
        mediaPlayer = MediaPlayer.create(this, tracksID.get(0));
        trackIdActual = 0;

        actualizarTrackSeekBar();


        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                trackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                /*if (mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()){
                    siguienteCancion();
                }*/
            }
        }, 0, 500);


    }

    public void actualizarTrackProgressText(){
        int progresoSegundos = mediaPlayer.getCurrentPosition() / 1000 ;
        int minutos = progresoSegundos / 60;
        int segundos = (progresoSegundos % 60) ;
        trackProgressTextView.setText(String.format("%02d:%02d",minutos,segundos));
        /*System.out.println("Minutos: " + String.valueOf(minutos));
        System.out.println("Segundos: " + String.valueOf(segundos));*/
    }

    public void actualizarTrackSeekBar(){
        trackSeekBar.setMax(mediaPlayer.getDuration());
        trackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
    }

    public void reproducirCancion(int indice){
        nombreCancionActualTextView.setText(tracksNames.get(indice));
        try {
            mediaPlayer.stop();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), tracksID.get(indice));
            mediaPlayer.start();
            playImageView.setImageResource(R.drawable.pause);
        }
        trackIdActual = indice;
        actualizarTrackSeekBar();
    }

    public void pausarReproducir(View view){
        if (mediaPlayer.isPlaying()){
            playImageView.setImageResource(R.drawable.play);
            mediaPlayer.pause();
        } else {
            playImageView.setImageResource(R.drawable.pause);
            mediaPlayer.start();
        }
    }

    public void siguienteCancion(View view){
        int indice = ++trackIdActual;
        if (indice >= tracksID.size()){
            trackIdActual = 0;
        } else {
            trackIdActual = indice;
        }
        reproducirCancion(trackIdActual);
        actualizarTrackSeekBar();
    }

    public void siguienteCancion(){
        int indice = ++trackIdActual;
        if (indice >= tracksID.size()){
            trackIdActual = 0;
        } else {
            trackIdActual = indice;
        }
        reproducirCancion(trackIdActual);
        actualizarTrackSeekBar();
    }

    public void anteriorCancion(View view){
        if(mediaPlayer.getCurrentPosition() > 2000){
            mediaPlayer.seekTo(0);
        } else {
            int indice = --trackIdActual;
            if (indice < 0){
                trackIdActual = 0;
            } else {
                trackIdActual = indice;
            }
            reproducirCancion(trackIdActual);
        }
        actualizarTrackSeekBar();
    }

    public ArrayList<String> corregirNombres(ArrayList<String> nombres){
        ArrayList<String> nombresCorregidos = new ArrayList<>();
        for(String nombre : nombres){
            String nuevoNombre = "";
            List<String> nombresSeparados = Arrays.asList(nombre.split("_"));
            for (String n : nombresSeparados){
                nuevoNombre += n + " ";
            }
            nombresCorregidos.add(nuevoNombre);
        }
        return nombresCorregidos;
    }
}
