package com.mingchuan.photoeffects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

public class MainActivity extends Activity {
	private static int LOAD_IMAGE_RESULT = 1;
	private Bitmap originImage;
	private String appImagesFolder = "";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Create application folder to save images
        appImagesFolder = Environment.getExternalStorageDirectory().toString() + "/PhotoEffects";
        File outputDirectory = new File(appImagesFolder);
        outputDirectory.mkdir();
        
    	String[] EFFECT_LIST = {
    			getString(R.string.none),
    			getString(R.string.grayscale), 
    			getString(R.string.negative)};
        
        // Find element
        Button btnLoadImage = (Button) findViewById(R.id.btnLoadPicture);
        Button btnSaveImage = (Button) findViewById(R.id.btnSavePicture);
        Spinner spiEffect = (Spinner) findViewById(R.id.spiEffect);
        
        // Create adapter to store dropdown list
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, EFFECT_LIST
        );
        // Setting dropdown style
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiEffect.setAdapter(adapter);
        selectEffect(spiEffect.getSelectedItemPosition());
        
        // Event Listener : btnLoadImage
        btnLoadImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				
				startActivityForResult(i, LOAD_IMAGE_RESULT);
			}
		});
        
        // Event Listener : btnSaveImage
        btnSaveImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ImageView imageView = (ImageView) findViewById(R.id.imgView);
				BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
				Bitmap bitmap = drawable.getBitmap();
				if(bitmap != null)
				{
					boolean success = false;
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
					String outputFileName = "Images_" + dateFormat.format(new Date()) + ".jpg";
					
					 File image = new File(appImagesFolder, outputFileName);
					 
					// Encode the file as a JPEG image.
					FileOutputStream outStream;
					try {
					    outStream = new FileOutputStream(image); 
					    // keep full quality of the image
					    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
					    outStream.flush();
					    outStream.close();
					    success = true;
					} catch (FileNotFoundException e) {
					    e.printStackTrace();
					} catch (IOException e) {
			            e.printStackTrace();
					}
					
					if (success) {
				        Toast.makeText(getApplicationContext(), getString(R.string.save_success), Toast.LENGTH_LONG).show();
				    } else {
				        Toast.makeText(getApplicationContext(), getString(R.string.save_fail), Toast.LENGTH_LONG).show();
				    }
				}
			}
		});
        
        // Event Listener : spiEffect
        spiEffect.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            	selectEffect(adapterView.getSelectedItemPosition());
            }

        	public void onNothingSelected(AdapterView<?> arg0) {
                Toast.makeText(MainActivity.this, "您沒有選擇任何項目", Toast.LENGTH_LONG).show();
        	}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    // Triggered when menu item click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_exit:
            finish();
            break;
        }
        return true;
    }
    
    // Triggered when pickup a image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
		if (requestCode == LOAD_IMAGE_RESULT && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			
			originImage = BitmapFactory.decodeFile(picturePath);
			Spinner spinner = (Spinner) findViewById(R.id.spiEffect);
			selectEffect(spinner.getSelectedItemPosition());
		}
    }
    
    // Triggered when select a item from spinner
    private void selectEffect(int itemPosition){
    	ImageView imageView = (ImageView) findViewById(R.id.imgView);
        switch(itemPosition){
            case 0:
        	    imageView.setImageBitmap(originImage);
        	    break;
            case 1:
            	Toast.makeText(MainActivity.this, getString(R.string.grayscale), Toast.LENGTH_LONG).show();
            	imageView.setImageBitmap(ToGray(originImage));
        	    break;
            case 2:
            	Toast.makeText(MainActivity.this, getString(R.string.negative), Toast.LENGTH_LONG).show();
            	imageView.setImageBitmap(ToNegative(originImage));
        	    break;
        }
    }
    
    private Bitmap ToNegative(Bitmap sourceImg) {
    	Bitmap outputImg = Bitmap.createBitmap(sourceImg.getWidth(), sourceImg.getHeight(), Bitmap.Config.ARGB_8888);
    	Canvas c = new Canvas(outputImg);
    	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);    
    	ColorMatrix cm = new ColorMatrix(new float[] {  
    	    -1f,  0f,  0f, 0f, 255f,
    	     0f, -1f,  0f, 0f, 255f,
    	     0f,  0f, -1f, 0f, 255f,
    	     0f,  0f,  0f, 1f,   0f});
    	paint.setColorFilter(new ColorMatrixColorFilter(cm));
    	c.drawBitmap(sourceImg, 0, 0, paint);
    	
    	return outputImg;
    }
    
    private Bitmap ToGray(Bitmap sourceImg) {
    	Bitmap outputImg = Bitmap.createBitmap(sourceImg.getWidth(), sourceImg.getHeight(), Bitmap.Config.ARGB_8888);
    	Canvas c = new Canvas(outputImg);
    	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);    
    	ColorMatrix cm = new ColorMatrix();
    	cm.setSaturation(0);
    	paint.setColorFilter(new ColorMatrixColorFilter(cm));
    	c.drawBitmap(sourceImg, 0, 0, paint);
    	
    	return outputImg;
    }
}
