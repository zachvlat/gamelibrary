package com.zachvlat.gamelibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private GameAdapter gameAdapter;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private SearchView searchView;
    private ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        gameAdapter = new GameAdapter();
        recyclerView.setAdapter(gameAdapter);

        searchView = findViewById(R.id.searchView);
        chipGroup = findViewById(R.id.chipGroup); // Get ChipGroup for filter chips

        loadJsonFromInternalStorage();

        // Replace old Button with FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openFilePicker());

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                readJsonFromUri(uri);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                gameAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                gameAdapter.filter(newText);
                return true;
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
            createFilterChips(games); // Create filter chips based on the sources
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
            createFilterChips(games); // Create filter chips based on the sources
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load JSON", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to create filter chips dynamically from sources
    private void createFilterChips(List<Game> games) {
        // Clear existing chips
        chipGroup.removeAllViews();

        // Use a map to count games per source
        Map<String, Integer> sourceCountMap = new HashMap<>();
        for (Game game : games) {
            for (String source : game.getSources()) {
                sourceCountMap.put(source, sourceCountMap.getOrDefault(source, 0) + 1);
            }
        }

        // Create a chip for each unique source
        for (Map.Entry<String, Integer> entry : sourceCountMap.entrySet()) {
            String source = entry.getKey();
            int count = entry.getValue();

            Chip chip = new Chip(this);
            chip.setText(source + " (" + count + ")"); // Update chip text to include count
            chip.setCheckable(true); // Make the chips selectable

            chip.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    // When the chip is selected, filter by that source
                    gameAdapter.filterBySource(source);
                } else {
                    // When the chip is unselected, remove the source filter
                    gameAdapter.removeSourceFilter(source);
                }
            });

            chipGroup.addView(chip); // Add chip to the ChipGroup
        }
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        filePickerLauncher.launch(intent);
    }
}
