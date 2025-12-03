package com.example.boki;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.boki.databinding.ActivityMainBinding;
import com.example.boki.databinding.AddoperationsDialogBoxBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    //View binding for the views
    ActivityMainBinding bindingMain;
    AddoperationsDialogBoxBinding dialogBinding;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Inflate and set the content view for MainActivity (Correct)
        bindingMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingMain.getRoot());

        // -- SETUP the Dialog Box --

        // 1. Create the Dialog object
        final Dialog addoperations_dialog = new Dialog(MainActivity.this);

        // 2. Inflate the layout for the dialog using ITS OWN binding class
         dialogBinding = AddoperationsDialogBoxBinding.inflate(getLayoutInflater());

        // 3. Set the inflated view as the com.google.ai.client.generativeai.type.content for the DIALOG, NOT the activity
        addoperations_dialog.setContentView(dialogBinding.getRoot());

        // 5. Configure the Dialog's window
        if (addoperations_dialog.getWindow() != null) {
            // Set the layout size
            addoperations_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //crate background for the dialog box  then add design then add the background to the dialog box
            addoperations_dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_bg));
        }


        //set cancel able as false -> which means if the user click outside the dialogue box the dialogue box will not disappear
        addoperations_dialog.setCancelable(false);
        // -- END SETUP THE Dialog Box --






        //set FAB listener to open the dialog
        bindingMain.fab.setOnClickListener(v -> addoperations_dialog.show());


        //then add both Listener to both buttons

        //one close the dialog
        dialogBinding.cancelDialogBtn.setOnClickListener(v -> {

            //set all Edit text
            dialogBinding.operationName.setText("");
            dialogBinding.operationAmount.setText("");

            //close the dialog
            addoperations_dialog.dismiss();
        });


        //one save the expenses in the DB
        dialogBinding.saveOprationBtn.setOnClickListener(v -> {
            //send toast
            Toast.makeText(MainActivity.this,"save Successfuly"+dialogBinding.operationName.getText().toString()+" "+dialogBinding.operationAmount.getText().toString(),Toast.LENGTH_SHORT).show();
            //set all Edit text
            dialogBinding.operationName.setText("");
            dialogBinding.operationAmount.setText("");
            //close the dialog
            addoperations_dialog.dismiss();
        });

        //-- START TO SETUP THE DATA AND TIME PICKER --
        dialogBinding.datePickerBtn.setOnClickListener(v -> {
            openDatePickerDialog();
        });

        dialogBinding.timePickerBtn.setOnClickListener(v -> {
            openTimePickerDialog();
        });
        //-- END TO SETUP THE DATA AND TIME PICKER --












        // --- BOTTOM NAVIGATION SETUP  ---
        bindingMain.bnvBottom.setOnApplyWindowInsetsListener(null);
        bindingMain.bnvBottom.getMenu().getItem(2).setEnabled(false);


        // Set the 'home' item as selected in the navigation bar
        bindingMain.bnvBottom.setSelectedItemId(R.id.home);
        //default start fragment
        replacFragment(new HomeFragment());         //call replace fragment method


        //on item selected listener for bnv_bottom
        bindingMain.bnvBottom.setOnItemSelectedListener(item -> {

            // Use if-else if instead of switch
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replacFragment(new HomeFragment());
            } else if (itemId == R.id.budget) {
                replacFragment(new BudgetFragment());
            } else if (itemId == R.id.operations) {
                replacFragment(new OperationsFragment());
            } else if (itemId == R.id.expenses) {
                replacFragment(new ExpensesFragment());
            }
            return true;
        });
    } // end of onCreate

    //replace fragment method
    private void replacFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    //openDatePickerDialog method
    private void openDatePickerDialog(){
        DatePickerDialog openDatePickerDialog = new DatePickerDialog(MainActivity.this,R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                //disply the date in the button text
                dialogBinding.datePickerBtn.setText(dayOfMonth+"/"+(month+1)+"/"+year);

            }
        }, 2025, 1, 1);
        openDatePickerDialog.show();
    }

    //openTimePickerDialog method
    private  void openTimePickerDialog(){
        TimePickerDialog dialog = new TimePickerDialog(MainActivity.this, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //disply in the button text
                dialogBinding.timePickerBtn.setText(hourOfDay+":"+minute);
            }
        }, 15, 50, false);
        dialog.show();
    }
}