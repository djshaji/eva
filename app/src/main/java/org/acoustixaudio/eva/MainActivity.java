package org.acoustixaudio.eva;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    Skin skin ;
    org.acoustixaudio.eva.UI ui ;
    public ConstraintLayout root ;
    Utils utils = new Utils(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        skin = new Skin(this);
        ui = new UI(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        skin.reset () ;
        try {
            ui.create();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        skin.load ();
    }
}