// app/src/main/java/com/example/dongman/InterestActivity.java
package com.example.dongman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.gridlayout.widget.GridLayout;

public class InterestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest);

        Toolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(InterestActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        Button btnComplete = findViewById(R.id.btnComplete);
        btnComplete.setOnClickListener(v -> {
            Intent intent = new Intent(InterestActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        GridLayout gridLayout = findViewById(R.id.gridSports);
        setupButtonToggle(gridLayout);
    }

    private void setupButtonToggle(GridLayout gridLayout) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View view = gridLayout.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;
                button.setOnClickListener(v -> toggleButtonState(button));
            }
        }
    }

    private void toggleButtonState(Button button) {
        boolean isSelected = button.isSelected();
        button.setSelected(!isSelected);

        if (button.isSelected()) {
            button.setBackgroundResource(R.drawable.sport_button_selected);
            button.setTextColor(getResources().getColor(R.color.black));
        } else {
            button.setBackgroundResource(R.drawable.sport_button_bg);
            button.setTextColor(getResources().getColor(R.color.black));
        }
    }
}
