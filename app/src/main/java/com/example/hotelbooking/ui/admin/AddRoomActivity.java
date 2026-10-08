package com.example.hotelbooking.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbooking.R;
import com.example.hotelbooking.database.Repo;
import com.example.hotelbooking.model.HotelRoom;
import com.example.hotelbooking.utils.Fmt;

public class AddRoomActivity extends AppCompatActivity {

    private static final String[] CODES = {"room_single", "room_double", "room_family", "room_suite"};
    private static final String[] LABELS = {"Ảnh: Phòng đơn", "Ảnh: Phòng đôi", "Ảnh: Phòng gia đình", "Ảnh: Phòng suite"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        ImageView imgPreview = findViewById(R.id.imgPreview);
        Spinner spImage = findViewById(R.id.spImage);
        EditText etName = findViewById(R.id.etName);
        EditText etBeds = findViewById(R.id.etBeds);
        EditText etGuests = findViewById(R.id.etGuests);
        EditText etPrice = findViewById(R.id.etPrice);
        EditText etDesc = findViewById(R.id.etDesc);

        spImage.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LABELS));
        spImage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                imgPreview.setImageResource(Fmt.drawableRes(AddRoomActivity.this, CODES[pos]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String beds = etBeds.getText().toString().trim();
            String guests = etGuests.getText().toString().trim();
            String price = etPrice.getText().toString().trim();
            if (name.isEmpty() || beds.isEmpty() || guests.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ tên, số giường, số khách và giá", Toast.LENGTH_SHORT).show();
                return;
            }

            HotelRoom r = new HotelRoom();
            r.name = name;
            r.image = CODES[spImage.getSelectedItemPosition()];
            r.beds = Integer.parseInt(beds);
            r.guests = Integer.parseInt(guests);
            r.price = Double.parseDouble(price);
            r.description = etDesc.getText().toString().trim();
            r.busy = false;
            Repo.get().addRoom(r);

            Toast.makeText(this, "Đã thêm phòng", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
