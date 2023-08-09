package com.example.billcalculator;


import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.util.Log;



import java.util.ArrayList;
import java.util.List;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private EditText etTotalBill, etNumberOfPeople, etHangoutDetails; // Declare etHangoutDetails here
    private RadioGroup radioGroupOptions;
    private LinearLayout layoutCustomBreakdown;
    private TextView tvBreakdownResults;
    private Button btnCalculate ;
    private Spinner spinnerInputType;
    private Button btnSave;
    private Button btnShare;
    private Button btnExit;
    private List<String> hangoutDetailsList;

    private static final String PREF_KEY_BILL_BREAKDOWN_HISTORY = "bill_breakdown_history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTotalBill = findViewById(R.id.etTotalBill);
        etNumberOfPeople = findViewById(R.id.etNumberOfPeople);
        etHangoutDetails = findViewById(R.id.etHangoutDetails); // Initialize etHangoutDetails
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        layoutCustomBreakdown = findViewById(R.id.layoutCustomBreakdown);
        tvBreakdownResults = findViewById(R.id.tvBreakdownResults);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);
        btnShare = findViewById(R.id.btnShare);
        btnExit = findViewById(R.id.btnExit);
        spinnerInputType = findViewById(R.id.spinnerInputType);
        hangoutDetailsList = new ArrayList<>();

        //Set up the spinner for input type selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.input_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInputType.setAdapter(adapter);

        //Set the click listener for the "Calculate" button//
        Button btnCalculate = findViewById(R.id.btnCalculate);
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                calculateBillBreakdown();
            }
        });

        //Set a click listener for the "Exit" button
        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitConfirmationDialog();
            }
        });

        //Set the item selected listener for the input type spinner
        spinnerInputType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedInputType = parent.getItemAtPosition(position).toString();
                switch (selectedInputType) {
                    case "Percentage":
                        addPercentageInputFields();
                        break;
                    case "Ratio":
                        addRatioInputFields();
                        break;
                    case "Amount":
                        addAmountInputFields();
                        break;
                    case "Combination":
                        addCombinationInputFields();
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing at all

            }
        });
        //Set the checked change listener for the radio group
        radioGroupOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioCustomBreakdown) {
                    layoutCustomBreakdown.setVisibility(View.VISIBLE);
                } else {
                    layoutCustomBreakdown.setVisibility(View.GONE);
                }
                if (checkedId == R.id.radioCombinationBreakdown) {
                    layoutCustomBreakdown.setVisibility(View.VISIBLE);
                    addCombinationInputFields(); // Call method to add combination input fields
                }
            }
        });

        //Set the click listener for the "Save" button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHangoutDetails(); // Add hangout details and save in history
            }
        });

        //Set the click listener for the "Share" button
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareBillBreakdown();
            }
        });

        // Set the click listener for the "History" button
        Button historyButton = findViewById(R.id.btnHistory);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        // Make the "Save" and "Share" buttons permanently visible
        btnSave.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.VISIBLE);
    }
    private void addHangoutDetails() {
        // Get the hangout details entered by the user
        String hangoutDetails = etHangoutDetails.getText().toString().trim();

        // Calculate the bill breakdown
        calculateBillBreakdown();

        // Append the hangout details to the calculated result
        String billBreakdown = tvBreakdownResults.getText().toString().trim();
        String completeBillBreakdown = billBreakdown + "\nHangout Details: " + hangoutDetails;

        // Save the complete bill breakdown (including hangout details) in shared preferences
        saveBillHistory(completeBillBreakdown);

        // Clear the EditText for next input
        etHangoutDetails.getText().clear();
    }
    private void shareBillBreakdown() {
        String billBreakdown = tvBreakdownResults.getText().toString();
        if (!billBreakdown.isEmpty()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, billBreakdown);

            PackageManager pm = getPackageManager();
            List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(shareIntent, 0);

            // Create a list of package names for the sharing apps you want to include
            List<String> packageNames = new ArrayList<>();
            packageNames.add("com.whatsapp");
            packageNames.add("com.facebook.katana");
            packageNames.add("com.twitter.android");
            packageNames.add("com.google.android.gm"); // Gmail

            // Find the first matching sharing app from the resolve info list
            Intent chosenIntent = null;
            for (ResolveInfo resolveInfo : resolveInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (packageNames.contains(packageName)) {
                    chosenIntent = new Intent(shareIntent);
                    chosenIntent.setPackage(packageName);
                    break;
                }
            }
            if (chosenIntent != null) {
                // If a matching sharing app is found, start that app directly
                startActivity(chosenIntent);
            } else {
                // If no matching app is found, show the chooser dialog as before
                Intent chooserIntent = Intent.createChooser(shareIntent, "Share Bill Breakdown");
                startActivity(chooserIntent);
            }
        } else {
            Toast.makeText(this, "No bill breakdown to share.", Toast.LENGTH_SHORT).show();
        }
    }
    private void addCombinationInputFields() {
        int numberOfPeople = Integer.parseInt(etNumberOfPeople.getText().toString().trim());
        layoutCustomBreakdown.removeAllViews();

        for (int i = 0; i < numberOfPeople; i++) {
            // Add an EditText field for Amount
            EditText amountEditText = new EditText(this);
            amountEditText.setHint("Enter amount for Person " + (i + 1));
            amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            amountEditText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            layoutCustomBreakdown.addView(amountEditText);

            // Add an EditText field for Percentage
            EditText percentageEditText = new EditText(this);
            percentageEditText.setHint("Enter percentage for Person " + (i + 1));
            percentageEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            percentageEditText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            layoutCustomBreakdown.addView(percentageEditText);
        }
    }
    private void addAmountInputFields() {
        int numberOfPeople = Integer.parseInt(etNumberOfPeople.getText().toString().trim());
        layoutCustomBreakdown.removeAllViews();

        for (int i = 0; i < numberOfPeople; i++) {
            EditText editText = new EditText(this);
            editText.setId(View.generateViewId()); // Generate a unique ID for each EditText
            editText.setHint("Enter amount for Person: " + (i + 1));
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            layoutCustomBreakdown.addView(editText);
        }
    }
    private void addPercentageInputFields() {
        String numberOfPeopleStr = etNumberOfPeople.getText().toString().trim();
        if (!numberOfPeopleStr.isEmpty()) {
            int numberOfPeople = Integer.parseInt(numberOfPeopleStr);
            layoutCustomBreakdown.removeAllViews();
            for (int i = 0; i < numberOfPeople; i++) {
                EditText editText = new EditText(this);
                editText.setHint("Enter percentage for Person: " + (i + 1));
                editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL);
                layoutCustomBreakdown.addView(editText);
            }
        } else {
            // Show an error message using a Toast
            Toast.makeText(this, "Please enter the number of people.", Toast.LENGTH_SHORT).show();
        }
    }
    private void outDetails() {
        // Get the hangout details entered by the user
        String hangoutDetails = etHangoutDetails.getText().toString().trim();

        // Calculate the bill breakdown
        calculateBillBreakdown();

        // Append the hangout details to the calculated result
        String billBreakdown = tvBreakdownResults.getText().toString().trim();
        String completeBillBreakdown = billBreakdown + "\nHangout Details: " + hangoutDetails;

        // Save the complete bill breakdown (including hangout details) in shared preferences
        saveBillHistory(completeBillBreakdown);

        // Clear the EditText for next input
        etHangoutDetails.getText().clear();
    }

    private void addRatioInputFields() {
        int numberOfPeople = Integer.parseInt(etNumberOfPeople.getText().toString().trim());
        layoutCustomBreakdown.removeAllViews();
        for (int i = 0; i < numberOfPeople; i++) {
            EditText editText = new EditText(this);
            editText.setHint("Enter ratio for Person " + (i + 1));
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            layoutCustomBreakdown.addView(editText);
        }
    }

    private void calculateBillBreakdown() {
        String totalBillStr = etTotalBill.getText().toString().trim();
        String numberOfPeopleStr = etNumberOfPeople.getText().toString().trim();

        if (totalBillStr.isEmpty() || numberOfPeopleStr.isEmpty()) {
            // Show an error message if the total bill amount or number of people is empty
            Toast.makeText(this, "Please enter the total bill amount and number of people.", Toast.LENGTH_SHORT).show();
            return;
        }
        double totalBill = Double.parseDouble(totalBillStr);
        int numberOfPeople = Integer.parseInt(numberOfPeopleStr);

        if (radioGroupOptions.getCheckedRadioButtonId() == R.id.radioEqualBreakdown) {
            // Equal breakdown scenario
            double eachPersonToPay = totalBill / numberOfPeople;
            String result = String.format("Equal break-down: Total bill RM%.2f, number of people %d, " +
                    "each person to pay RM%.2f", totalBill, numberOfPeople, eachPersonToPay);
            tvBreakdownResults.setText(result);
        } else if (radioGroupOptions.getCheckedRadioButtonId() == R.id.radioCustomBreakdown) {
            // Custom breakdown scenario
            List<Double> customBreakdownList = new ArrayList<>();
            double totalCustomValue = 0.0;

            // Retrieve the custom ratios from the added EditText fields
            for (int i = 0; i < layoutCustomBreakdown.getChildCount(); i++) {
                View childView = layoutCustomBreakdown.getChildAt(i);
                if (childView instanceof EditText) {
                    String input = ((EditText) childView).getText().toString().trim();
                    if (!input.isEmpty()) {
                        double ratio = Double.parseDouble(input);
                        customBreakdownList.add(ratio);
                        totalCustomValue += ratio;
                    } else {
                        // Show an error message if any input field is empty
                        Toast.makeText(this, "Please enter all custom ratios.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            // Get the selected breakdown type (Percentage or Ratio)
            String selectedBreakdownType = spinnerInputType.getSelectedItem().toString();
            boolean isPercentageBreakdown = selectedBreakdownType.equals("Percentage");

            if (isPercentageBreakdown) {
                double totalPercentageValue = 0.0;
                for (double percentage : customBreakdownList) {
                    totalPercentageValue += percentage;
                }
                // Check if total percentage is exactly 100%
                if (Math.abs(totalCustomValue - 100.0) > 0.0001) {
                    Toast.makeText(this, "Total custom percentage should be 100%.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Generate the result string for custom percentage breakdown
                StringBuilder resultBuilder = new StringBuilder("Custom percentage break-down:\n");
                for (int i = 0; i < numberOfPeople; i++) {
                    double percentage = customBreakdownList.get(i);
                    double calculatedAmount = (percentage / 100) * totalBill;
                    resultBuilder.append(String.format("Person %d: %.2f%% - RM%.2f\n", i + 1, percentage, calculatedAmount));
                }

                tvBreakdownResults.setText(resultBuilder.toString());
            } else if (selectedBreakdownType.equals("Ratio")) {
                // Calculate the total ratio value
                double totalRatioValue = 0.0;
                for (double ratio : customBreakdownList) {
                    totalRatioValue += ratio;
                }

                // Adjust the ratios proportionally to sum up to the number of people
                double adjustmentFactor = numberOfPeople / totalRatioValue;
                for (int i = 0; i < customBreakdownList.size(); i++) {
                    double adjustedRatio = customBreakdownList.get(i) * adjustmentFactor;
                    customBreakdownList.set(i, adjustedRatio);
                }

                // Generate the result string for custom ratio breakdown
                double totalBillPerPerson = totalBill / numberOfPeople;
                StringBuilder resultBuilder = new StringBuilder("Custom ratio break-down:\n");
                for (int i = 0; i < numberOfPeople; i++) {
                    double ratio = customBreakdownList.get(i);
                    double amount = totalBillPerPerson * ratio;
                    resultBuilder.append(String.format("Person %d: Ratio %.2f - RM%.2f\n", i + 1, ratio, amount));
                }
                tvBreakdownResults.setText(resultBuilder.toString());
            } else if (selectedBreakdownType.equals("Combination")) {
                List<Double> customPercentageList = new ArrayList<>();
                double totalCustomPercentage = 0.0;

                // Retrieve the custom percentages from the added EditText Fields
                for (int i = 0; i < layoutCustomBreakdown.getChildCount(); i++) {
                    View childView = layoutCustomBreakdown.getChildAt(i);
                    if (childView instanceof EditText) {
                        String input = ((EditText) childView).getText().toString();
                        if (!input.isEmpty()) {
                            double percentage = Double.parseDouble(input);
                            customPercentageList.add(percentage);
                            totalCustomPercentage += percentage;
                        } else {
                            // Show an error message if any input field is empty
                            Toast.makeText(this, "Please enter all custom percentages.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                if (Math.abs(totalCustomPercentage - 100.0) > 0.0001 || Math.abs(totalCustomValue - totalBill) > 0.0001) {
                    Toast.makeText(this, "Total custom percentage should be 100% and Total amount input should be equal to the total amount entered above.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Calculate the total ratio value
                double totalRatioValue = 0.0;
                for (double ratio : customBreakdownList) {
                    totalRatioValue += ratio;
                }

                // Generate the result string for combination breakdown
                StringBuilder resultBuilder = new StringBuilder("Combination break-down:\n");
                for (int i = 0; i < numberOfPeople; i++) {
                    double ratio = customBreakdownList.get(i);
                    double percentage = customPercentageList.get(i);
                    double calculatedAmount = ((ratio / totalRatioValue) * totalBill) + ((percentage / 100) * totalBill);
                    resultBuilder.append(String.format("Person %d: Ratio %.2f - %.2f%% - RM%.2f\n", i + 1, ratio, percentage, calculatedAmount));
                }
                tvBreakdownResults.setText(resultBuilder.toString());
            }  else if (radioGroupOptions.getCheckedRadioButtonId() == R.id.radioCustomBreakdown) {
                // Custom breakdown by amount scenario
                List<Double> customAmounts = new ArrayList<>();
                double totalCustomAmount = 0.0;

                // Retrieve the custom amounts from the added EditText Fields
                for (int i = 0; i < layoutCustomBreakdown.getChildCount(); i++) {
                    View childView = layoutCustomBreakdown.getChildAt(i);
                    if (childView instanceof EditText) {
                        String input = ((EditText) childView).getText().toString();
                        if (!input.isEmpty()) {
                            double amount = Double.parseDouble(input);
                            customAmounts.add(amount);
                            totalCustomAmount += amount;
                        } else {
                            // Show an error message if any input field is empty
                            Toast.makeText(this, "Please enter all custom amounts.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                if (Math.abs(totalCustomAmount - totalBill) > 0.0001) {
                    Toast.makeText(this, "Total custom amount should be equal to the total bill amount entered above.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Generate the result string for custom amount breakdown
                StringBuilder resultBuilder = new StringBuilder("Custom amount break-down:\n");
                for (int i = 0; i < numberOfPeople; i++) {
                    double amount = customAmounts.get(i);
                    resultBuilder.append(String.format("Person %d: RM%.2f\n", i + 1, amount));
                }
                tvBreakdownResults.setText(resultBuilder.toString());
            }
            // Save the bill breakdown result in shared preferences
            btnSave.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
        }
    }
    // Method to handle the "Clear" button click
    public void clearInputs(View view) {
        etTotalBill.setText(""); // Clear the total bill amount input
        etNumberOfPeople.setText(""); // Clear the number of people input
        radioGroupOptions.clearCheck(); // Clear the radio button selection

        // Clear the custom break-down input fields
        layoutCustomBreakdown.removeAllViews();

        // Clear the results TextView
        tvBreakdownResults.setText("");

        // Make the "Save" and "Share" buttons permanently visible after clearing inputs
        btnSave.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.VISIBLE);
    }

    // Save the bill breakdown result in shared preferences
    private void saveBillHistory(String billBreakdown) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int historyCount = preferences.getInt("history_count", 0);

        // Generate a unique key for the current bill entry
        String key = "bill_history_" + historyCount;

        // Increment the history count for the next bill entry
        historyCount++;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("history_count", historyCount);
        editor.putString(key, billBreakdown);
        editor.apply();

        // Show a toast message to indicate successful save
        Toast.makeText(this, "Bill breakdown saved to history.", Toast.LENGTH_SHORT).show();
    }

    // Method to share the bill breakdown
    public void shareBillBreakdown(View view) {
        String billBreakdown = tvBreakdownResults.getText().toString();
        if (!billBreakdown.isEmpty()) {
            // Share the bill breakdown using an implicit intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, billBreakdown);
            startActivity(Intent.createChooser(shareIntent, "Share Bill Breakdown"));
        } else {
            Toast.makeText(this, "No bill breakdown to share.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to save hangout details in shared preferences
    private void saveHangoutDetails(String hangoutDetails) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentDetails = preferences.getString("hangout_details", "");
        String newDetails = currentDetails + hangoutDetails + "\n";
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("hangout_details", newDetails);
        editor.apply();
        Toast.makeText(this, "Hangout details saved.", Toast.LENGTH_SHORT).show();
    }

    //Method to show the exit confirmation dialog
    @Override
    public void onBackPressed(){
        showExitConfirmationDialog();
    }
    private void showExitConfirmationDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to exit?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exitApp();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, dismiss the dialog.
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    // Method to handle the exit logic
    private void exitApp() {
        finish(); // This will close the current activity and exit the app
    }
}
