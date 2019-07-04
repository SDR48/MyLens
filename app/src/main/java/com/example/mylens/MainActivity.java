package com.example.mylens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button snapBtn;
    private Button detectBtn;
    private Button imports;
    private ImageView imageView;
    private TextView txtView;
    private Bitmap imageBitmap;
    FirebaseVisionImage image;
    private static final int REQUEST_CALL = 1;
ImageButton call,search,whatsapp,youtube;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        snapBtn = findViewById(R.id.snapBtn);
        detectBtn = findViewById(R.id.detectBtn);
        imageView = findViewById(R.id.imageView);
        txtView = findViewById(R.id.textView);
        call = findViewById(R.id.call);
        imports = findViewById(R.id.button);
        search=findViewById(R.id.search);
        whatsapp=findViewById(R.id.whatsapp);
        youtube=findViewById(R.id.youtube);
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectTxt();
            }
        });
call.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        int count=0;
        String x = txtView.getText().toString();
        for(int i=0;i<x.length();++i)
        {
            if(Character.isDigit(x.charAt(i)))
            {
                count+=1;
            }
        }
        if(count==x.length()) {
            makePhoneCall();
        }
        else{
            Toast.makeText(MainActivity.this,"Invalid input",Toast.LENGTH_SHORT).show();
        }
    }
});
imports.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        chooseImage();
    }
});
search.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        String query = txtView.getText().toString();
        if(query!="No text detected" || query!="") {
            String escapedQuery = Uri.encode(query, "UTF-8");
            Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        else{
            Toast.makeText(MainActivity.this,"Invalid input",Toast.LENGTH_SHORT).show();
        }
    }
});
whatsapp.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        onClickWhatsApp();
    }
});
youtube.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        String query = txtView.getText().toString();
        if(query!="No text detected" || query!="") {
            String escapedQuery = Uri.encode(query, "UTF-8");
            Uri uri = Uri.parse("http://www.youtube.com/results?search_query=" + escapedQuery);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        else{
            Toast.makeText(MainActivity.this,"Invalid input",Toast.LENGTH_SHORT).show();
        }
    }
});
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
        if(requestCode == 100 && resultCode == RESULT_OK && data!=null)
        {
            Uri uri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void detectTxt() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void makePhoneCall() {
        String number = txtView.getText().toString();
        if (number.trim().length() > 0) {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + number;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        }
    }
    private void processTxt(FirebaseVisionText text) {
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No Text :(", Toast.LENGTH_LONG).show();
            return;
        }
        for (FirebaseVisionText.Block block : text.getBlocks()) {
            String txt = block.getText();
            txtView.setTextSize(24);
            txtView.setText(txt);
        }
    }
    public void onClickWhatsApp() {

        PackageManager pm=getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = txtView.getText().toString();
            if(text!="No text detected" || text!="") {
                PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                waIntent.setPackage("com.whatsapp");

                waIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(waIntent, "Share with"));
            }
            else{
                Toast.makeText(MainActivity.this,"Invalid input",Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }
    }
    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
    }
}
