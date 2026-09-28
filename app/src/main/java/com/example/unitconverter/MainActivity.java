package com.example.unitconverter;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.view.inputmethod.EditorInfo;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerCategory, spinnerFromUnit, spinnerToUnit;
    private TextInputEditText etInputValue;
    private MaterialButton btnConvert, btnClear;
    private TextView tvResult;

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private final String[] categories = {
            "Length", "Weight / Mass", "Temperature", "Volume", "Area", "Speed", "Time"
    };

    private final Map<String, Map<String, Double>> unitFactors = new LinkedHashMap<>();
    private final DecimalFormat resultFormat = new DecimalFormat("#,##0.###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Dynamically pad only top (status bar) and bottom (gesture bar)
            view.setPadding(0, insets.top, 0, insets.bottom);
            return windowInsets;
        });

        // Dynamic status bar contrast
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES);
        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(!isDarkMode);

        buildUnitTables();

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerFromUnit = findViewById(R.id.spinnerFromUnit);
        spinnerToUnit = findViewById(R.id.spinnerToUnit);

        etInputValue = findViewById(R.id.etInputValue);
        etInputValue.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                etInputValue.clearFocus();
                //performConversion(); // Automatically calculates result on tick
                return true;
            }
            return false;
        });

        btnConvert = findViewById(R.id.btnConvert);
        btnClear = findViewById(R.id.btnClear);
        tvResult = findViewById(R.id.tvResult);

        // Setup Category Exposed Dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, categories);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setText(categories[0], false);

        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            populateUnitSpinners(categories[position]);
            tvResult.setText("0.0");
        });

        // Clear (AC) Listener
        btnClear.setOnClickListener(v -> {
            etInputValue.setText("");
            etInputValue.setError(null);
            tvResult.setText("0.0");
        });

        btnConvert.setOnClickListener(v -> performConversion());

        // Initialize units for default category
        populateUnitSpinners(categories[0]);
    }

    private void buildUnitTables() {
        // Length (base: meters)
        Map<String, Double> length = new LinkedHashMap<>();
        length.put("Millimeters (mm)", 0.001);
        length.put("Centimeters (cm)", 0.01);
        length.put("Meters (m)", 1.0);
        length.put("Kilometers (km)", 1000.0);
        length.put("Inches (in)", 0.0254);
        length.put("Feet (ft)", 0.3048);
        length.put("Yards (yd)", 0.9144);
        length.put("Miles (mi)", 1609.344);
        unitFactors.put("Length", length);

        // Weight / Mass (base: grams)
        Map<String, Double> weight = new LinkedHashMap<>();
        weight.put("Milligrams (mg)", 0.001);
        weight.put("Grams (g)", 1.0);
        weight.put("Kilograms (kg)", 1000.0);
        weight.put("Metric Tons (t)", 1000000.0);
        weight.put("Ounces (oz)", 28.349523125);
        weight.put("Pounds (lb)", 453.59237);
        unitFactors.put("Weight / Mass", weight);

        // Temperature (custom formulas)
        Map<String, Double> temp = new LinkedHashMap<>();
        temp.put("Celsius (°C)", 1.0);
        temp.put("Fahrenheit (°F)", 1.0);
        temp.put("Kelvin (K)", 1.0);
        unitFactors.put("Temperature", temp);

        // Volume (base: liters)
        Map<String, Double> volume = new LinkedHashMap<>();
        volume.put("Milliliters (mL)", 0.001);
        volume.put("Liters (L)", 1.0);
        volume.put("Cubic Meters (m³)", 1000.0);
        volume.put("US Gallons (gal)", 3.785411784);
        volume.put("US Quarts (qt)", 0.946352946);
        volume.put("US Cups (cup)", 0.2365882365);
        unitFactors.put("Volume", volume);

        // Area (base: square meters)
        Map<String, Double> area = new LinkedHashMap<>();
        area.put("Square Meters (m²)", 1.0);
        area.put("Square Kilometers (km²)", 1000000.0);
        area.put("Square Feet (ft²)", 0.09290304);
        area.put("Acres", 4046.8564224);
        area.put("Hectares (ha)", 10000.0);
        unitFactors.put("Area", area);

        // Speed (base: m/s)
        Map<String, Double> speed = new LinkedHashMap<>();
        speed.put("Meters/sec (m/s)", 1.0);
        speed.put("Kilometers/hour (km/h)", 0.277777778);
        speed.put("Miles/hour (mph)", 0.44704);
        speed.put("Knots", 0.514444444);
        unitFactors.put("Speed", speed);

        // Time (base: seconds)
        Map<String, Double> time = new LinkedHashMap<>();
        time.put("Seconds (s)", 1.0);
        time.put("Minutes (min)", 60.0);
        time.put("Hours (hr)", 3600.0);
        time.put("Days", 86400.0);
        unitFactors.put("Time", time);
    }

    private void populateUnitSpinners(String category) {
        Map<String, Double> units = unitFactors.get(category);
        if (units == null) return;

        String[] unitNames = units.keySet().toArray(new String[0]);

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, unitNames);
        spinnerFromUnit.setAdapter(fromAdapter);
        spinnerFromUnit.setText(unitNames[0], false);

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, unitNames);
        spinnerToUnit.setAdapter(toAdapter);

        if (unitNames.length > 1) {
            spinnerToUnit.setText(unitNames[1], false);
        } else {
            spinnerToUnit.setText(unitNames[0], false);
        }
    }

    private void performConversion() {
        hideKeyboard();
        etInputValue.clearFocus();

        String inputText = etInputValue.getText() != null ? etInputValue.getText().toString().trim() : "";
        if (inputText.isEmpty()) {
            Toast.makeText(this, "Please enter a value to convert", Toast.LENGTH_SHORT).show();
            etInputValue.setError("");
            return;
        }

        double inputValue;
        try {
            inputValue = Double.parseDouble(inputText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid numeric value", Toast.LENGTH_SHORT).show();
            etInputValue.setError("");
            return;
        }

        String category = spinnerCategory.getText().toString();
        String fromUnit = spinnerFromUnit.getText().toString();
        String toUnit = spinnerToUnit.getText().toString();

        if (category.isEmpty() || fromUnit.isEmpty() || toUnit.isEmpty()) return;

        double result;
        if (category.equals("Temperature")) {
            result = convertTemperature(inputValue, fromUnit, toUnit);
        } else {
            Map<String, Double> units = unitFactors.get(category);
            if (units == null || !units.containsKey(fromUnit) || !units.containsKey(toUnit)) return;

            Double fromFactor = units.get(fromUnit);
            Double toFactor = units.get(toUnit);

            if (fromFactor == null || toFactor == null || toFactor == 0.0) return;

            double baseValue = inputValue * fromFactor;
            result = baseValue / toFactor;
        }

        tvResult.setText(resultFormat.format(result));
    }

    private double convertTemperature(double value, String fromUnit, String toUnit) {
        double celsius;
        if (fromUnit.startsWith("Celsius")) {
            celsius = value;
        } else if (fromUnit.startsWith("Fahrenheit")) {
            celsius = (value - 32) * 5.0 / 9.0;
        } else {
            celsius = value - 273.15;
        }

        if (toUnit.startsWith("Celsius")) {
            return celsius;
        } else if (toUnit.startsWith("Fahrenheit")) {
            return (celsius * 9.0 / 5.0) + 32;
        } else {
            return celsius + 273.15;
        }
    }
}