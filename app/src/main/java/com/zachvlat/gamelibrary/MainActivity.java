package com.zachvlat.gamelibrary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GameAdapter gameAdapter;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        gameAdapter = new GameAdapter();
        recyclerView.setAdapter(gameAdapter);

        // Try to load saved data from internal storage
        loadJsonFromInternalStorage();

        Button btnLoadJson = findViewById(R.id.btnLoadJson);
        btnLoadJson.setOnClickListener(v -> openFilePicker());

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                readJsonFromUri(uri);
            }
        });
    }

    // Method to save JSON to internal storage
    private void saveJsonToInternalStorage(String json) {
        try {
            FileOutputStream fos = openFileOutput("games.json", Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save JSON", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to load JSON from internal storage
    private void loadJsonFromInternalStorage() {
        try {
            FileInputStream fis = openFileInput("games.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String json = jsonBuilder.toString();
            List<Game> games = JsonUtils.parseJson(json);
            gameAdapter.setGames(games);
        } catch (FileNotFoundException e) {
            // No saved data, that's OK, app can start without it
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load saved JSON", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to read JSON from the file picker
    private void readJsonFromUri(Uri uri) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String json = jsonBuilder.toString();

            // Save the loaded JSON to internal storage
            saveJsonToInternalStorage(json);

            List<Game> games = JsonUtils.parseJson(json);
            gameAdapter.setGames(games);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load JSON", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        filePickerLauncher.launch(intent);
    }
}