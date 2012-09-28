package com.andrewquitmeyer.Subway;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager.OnActivityStopListener;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SubwayActivity extends Activity {
  private static final String TAG = "CameraDemo";
  Preview cameraView; // <1>
  
  Button clickButton; // <2>
  Button newImgButton;
  Button saveButton;
  Button userButton;
  FrameLayout cameraframe;
  Drawable currentOverlay;
  ImageView overlayview;
  
  boolean previewing;
  boolean freestyle;
  byte[] picdata;
  //entry point to sdcard
  public static File appDirectory;
  public static File pictureDirectory;
  public List<String> alreadyTherePictures;
  public List<String> alreadyThereProjects;

  
  public ArrayList<Integer> framesforUser;
  int initialUserID=-1;
  int userID=2;
  int totalUsers=100;
  
  int resourcefromframeorder[] = new int[7913];
  int FIRST_FRAME=6805;
  int currentPicIndex=0;
  int globalcurrentPicIndex=currentPicIndex+FIRST_FRAME;
  TextView framenumTextView;
  TextView modeTextView;
  TextView countdownView;
  DecimalFormat userIDformatter = new DecimalFormat(
	"00");
  
  /** INITIALLIZZE
   * set up files and folders
   * 
   * check which user we are
   * access the camera
   * 
   * . */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.main);
   cameraframe = (FrameLayout) findViewById(R.id.preview);
   overlayview = new ImageView(getApplicationContext());
	  overlayview.setScaleType(ImageView.ScaleType.FIT_XY);

   previewing=false;
   freestyle=false;
  
	// set up camera, start button
    cameraView = new Preview(this); // <3>

	  cameraframe.addView(cameraView,0); // <4>

	  
	
	
	    setupButtons();
    initCamera();
    
   
	
	
	
  
	loadFrames();
	  initFileSystem();
	  
    //retakeButton.setEnabled(false);
	 toggleMode();
   
    Log.d(TAG, "Fully initialized");
  }

  private void toggleMode() {
	// Switch Between Freestyle and Match Modes
	  if(freestyle){
		  Toast.makeText(getApplicationContext(), "Take a picture in any pose", Toast.LENGTH_LONG).show();
		  
modeTextView.setText("Freestyle");
newImgButton.setEnabled(false);
countdownView.setEnabled(false);
countdownView.setText("-");
framenumTextView.setEnabled(false);
framenumTextView.setText("-");
	  }
	  else{
		  Toast.makeText(getApplicationContext(), "Try to match the shape and take a picture", Toast.LENGTH_LONG).show();
		  modeTextView.setText("Match");
		  newImgButton.setEnabled(true);
		  countdownView.setEnabled(true);
		  framenumTextView.setEnabled(true);
	  }
	  
	
}

private void setupButtons() {
	
	countdownView=(TextView) findViewById(R.id.countdowntextView1);
	modeTextView =(TextView) findViewById(R.id.modeview);
	
	  framenumTextView = (TextView) findViewById(R.id.framenumberview);
	  
		framenumTextView.setText("#"+currentPicIndex+"");
		
		/**
		 * click button
		 */
	    clickButton = (Button) findViewById(R.id.buttonClick);
	    clickButton.setOnClickListener(new OnClickListener() {
	      public void onClick(View v) { // <5>
	    	  
	    	  if(!previewing){
	    	  
	        cameraView.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
	        previewing=true;
	        clickButton.setText("Redo");
		    //saveButton.setEnabled(true);

	    	  }
	    	  else{
	    		  //Let them retake the pic
	    		  cameraframe.removeView(cameraView);
	        	  cameraframe.addView(cameraView,0);
	    		  previewing=false;
	    		  clickButton.setText("Click");
	  		    saveButton.setEnabled(false);

	    	  }
	      }
	    });
	    
	    
		/**
		 * save button
		 */
	    saveButton = (Button) findViewById(R.id.savebutton);
	    saveButton.setOnClickListener(new OnClickListener() {
	      public void onClick(View v) { // <5>
	    	
	    	 
	  		if(freestyle){
	  			 //SAVE THE FILE!!!!!!//
		          FileOutputStream outStream = null;
		              try {
		            	  File outputFile = new File(pictureDirectory, String.format("freestyle_"+"userID_"+userIDformatter.format(userID)+"_t_"+"%d.jpg", System.currentTimeMillis()));
		            		

		            outStream = new FileOutputStream(outputFile);
//		    		  String.format("/sdcard/Subway_"+userID+"/"+"freestyle_"+"userID_"+userID+"_t_"+"%d.jpg", System.currentTimeMillis())); // <9>
////				    		  String.format("/sdcard/Subway_"+userID+"/"+"freestyle__user_"+userIDformatter.format(userID))); // <9>

		        	       	  outStream.write(picdata);
		            outStream.close();
		            Log.d("SY", "onPictureTaken - wrote freestyle bytes: " + picdata.length);
		          } catch (FileNotFoundException e) { // <10>
		            e.printStackTrace();
		          } catch (IOException e) {
		            e.printStackTrace();
		          } finally {
		          }
	  			
		        //RESET TO NEXT TARGET MODE
		          if(framesforUser.isEmpty())
		          {//don't toggle! stay empty, stay in freestyle mode
		          Toast.makeText(getApplicationContext(), "Finished!", Toast.LENGTH_LONG);
		          modeTextView.setText("Finished");
		          newImgButton.setEnabled(false);
		      	freestyle=true;
	  			toggleMode();
	        
  		//Take new FREESTYLE pic
    		  cameraframe.removeView(cameraView);
        	  cameraframe.addView(cameraView,0);
    		  previewing=false;
    		  clickButton.setText("Click");
  		    saveButton.setEnabled(false);

		          }
		          else{
		  			freestyle=false;
		  			toggleMode();
		          
	  			//This might not work cuz im delting something already gone
	  			changePic();
	  		//Take new pic
	    		  cameraframe.removeView(cameraView);
	        	  cameraframe.addView(cameraView,0);
	    		  previewing=false;
	    		  clickButton.setText("Click");
	  		    saveButton.setEnabled(false);
		          }
	  		}
	  		else{
	  			
	  			if(framesforUser.isEmpty())
		          {//don't toggle! stay empty, stay in freestyle mode
		          Toast.makeText(getApplicationContext(), "Finished!", Toast.LENGTH_LONG);
		          modeTextView.setText("Finished");
		          newImgButton.setEnabled(false);
		      	freestyle=true;
	  			toggleMode();
	        
		//Take new FREESTYLE pic
	  			 cameraframe.removeView(overlayview);
  		  cameraframe.removeView(cameraView);
      	  cameraframe.addView(cameraView,0);
  		  previewing=false;
  		  clickButton.setText("Click");
		    saveButton.setEnabled(false);

		          }
		          else{
	  			
	  			 //Match! Save the file
		          FileOutputStream outStream = null;
		              try {
		              	  File outputFile = new File(pictureDirectory, "match__userID_"+userIDformatter.format(userID)+"__frame_0"+currentPicIndex+".jpg");

				            outStream = new FileOutputStream(outputFile);
		            	  
		            	  
//		            outStream = new FileOutputStream(		  
//		    		  String.format("/sdcard/Subway_"+userID+"/"+"frame_0"+currentPicIndex+"__t_"+"%d.jpg", System.currentTimeMillis())); // <9>
//				    		  String.format("/sdcard/Subway_"+userID+"/"+"match__userID_"+userIDformatter.format(userID)+"__frame_0"+currentPicIndex)); // <9>

		        	       	  outStream.write(picdata);
		            outStream.close();
		            Log.d("SY", "onPictureTaken - wrote match bytes: " + picdata.length);
		          } catch (FileNotFoundException e) { // <10>
		            e.printStackTrace();
		          } catch (IOException e) {
		            e.printStackTrace();
		          } finally {
		          }
		    	  
		    	  //drop that frame
		  	/***
		  	 * 
		  	 * FIXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
		  	 * 
		  	 */
		          
		          framesforUser.remove(0);
				countdownView.setText("rem. "+framesforUser.size()+"");
				

		    	  
		    	  //RESET TO NEXT TARGET MODE
	  			
	  			freestyle=true;
	  			toggleMode();
	  		  cameraframe.removeView(overlayview);

	  			 //Take new pic
	    		  cameraframe.removeView(cameraView);
	        	  cameraframe.addView(cameraView,0);
	    		  previewing=false;
	    		  clickButton.setText("Click");
	  		    saveButton.setEnabled(false);
	  		}
	  		}
	    	
	      }
	    });
	    saveButton.setEnabled(false);

	    
	    /***
	     * New Image
	     */
	    newImgButton = (Button) findViewById(R.id.retakebutton);
	    
	    newImgButton.setOnClickListener(new OnClickListener() {
	      public void onClick(View v) { // <5>
	    	changePic();
	    	 
	    	  }
	    	  
	    	
	    	  
	      
	    });
	  
	    
		
		/**
		 * User button
		 */
	    userButton = (Button) findViewById(R.id.userChange);
	    userButton.setOnClickListener(new OnClickListener() {
	      public void onClick(View v) { // <5>
	    	
	    	  changeUser();
	
	    	
	      }
	    });
	    
	  
}
  
  protected void changePic() {
		// Drop the current pic from the array to take, and load up the next one
if(framesforUser.isEmpty())
{
Toast.makeText(getApplicationContext(), "Finished!", Toast.LENGTH_LONG);
modeTextView.setText("Finished");
newImgButton.setEnabled(false);

}
else{
	Collections.shuffle(framesforUser);

	currentPicIndex=framesforUser.get(0);

		  Log.e("SY", "Current PicIndex= "+currentPicIndex+"    "+framesforUser.get(0));
		  currentOverlay =  getResources().getDrawable(resourcefromframeorder[framesforUser.get(0)]);
		  
		  overlayview.setImageDrawable(currentOverlay);
		  cameraframe.removeView(overlayview);
		  cameraframe.addView(overlayview,1);

		  framenumTextView.setText(currentPicIndex+"");
			countdownView.setText(framesforUser.size()+"");

	}
  }
  
private void loadFrames() {


	

	

for (int i=6805; i<7912;i++){
resourcefromframeorder[i]=getResources().getIdentifier("drawable/subwaydance_0"+(i),null,getPackageName());
	//Log.d("MYT", "totalnum of frames= "+i+"  "+resourcefromframeorder[i]);
}



}
	  
	  


private void makeUser() {
	//Create a version of the app specific to one blind user
	framesforUser = new ArrayList<Integer>();
	//Go through all possible files and add every twentieth one
	for (int j=userID+6805; j<7912;j=j+totalUsers){
	//	Log.d("MYT", "newo totalnum of frames= "+j+"  "+resourcefromframeorder[j]);

		framesforUser.add(j);
	}
	
	
	Collections.shuffle(framesforUser);
	//Load in first Image
	currentPicIndex=framesforUser.get(0);
	currentOverlay =  getResources().getDrawable(resourcefromframeorder[framesforUser.get(0)]);
	overlayview.setImageDrawable(currentOverlay);
	//cameraframe.addView(overlayview,1);
	cameraframe.removeView(overlayview);
	cameraframe.addView(overlayview,1);
	framenumTextView.setText(currentPicIndex+"");
	countdownView.setText(framesforUser.size()+"");
	userButton.setText("USER: "+userIDformatter.format(userID));

}
@Override
protected void onPause() {
    super.onPause();

    // Because the Camera object is a shared resource, it's very
    // important to release it when the activity is paused.
    cameraView.camera.stopPreview();

    cameraView.camera.release();
    cameraView.camera = null;

   
    finish();
	System.exit(0);

}

void loadUpAllPics(){
	  pictureDirectory = new File("/sdcard/Subway/User_"+userIDformatter.format(userID)+"/");
	  pictureDirectory.mkdirs();
		// find Current Projects on SD card
		alreadyTherePictures = new ArrayList<String>();

		// Note that Arrays.asList returns a horrible fixed length list, Make
		// sure to cast to ArrayList

			alreadyTherePictures = new ArrayList<String>(
					Arrays.asList(pictureDirectory.list()));
//		
	  //Check to see if they have already taken some of the pics!
		//iterate through framesforUser and see if any of those frames have already been photographed, if so, delete from roster	
			//Loop through the pics on file
			for(int z=0;z<alreadyTherePictures.size();z++){
				Log.d("SY", "file already there "+alreadyTherePictures.get(z));
				//Loop through user files
				for(int x=0;x<framesforUser.size();x++){
					if(
							((alreadyTherePictures.get(z)).indexOf(Integer.toString(framesforUser.get(x))))>5) 
							{
						Log.e("SY", "already took this pic, remove please! "+alreadyTherePictures.get(z)+"  frame numba "+framesforUser.get(x)+"   framesize "+framesforUser.size());
framesforUser.remove(x);
							}
				}
				
				
			}
			countdownView.setText(framesforUser.size()+"");
	
}
private void initFileSystem() {
	//  make sure our directories are in order
	chkSD();
	  appDirectory = new File("/sdcard/Subway/");

		if(appDirectory.mkdirs()){
			//BRAND NEW PERSON PROJECT FIRST TIME THEY HAVE THE PHONE
			Log.e("SY", "First time with app!");
			changeUser();
		
		}
		else{
			appDirectory.mkdirs();
			Log.e("MYTT", "Used the App before");
			
			alreadyThereProjects = new ArrayList<String>();

			// Note that Arrays.asList returns a horrible fixed length list, Make
			// sure to cast to ArrayList

				alreadyThereProjects = new ArrayList<String>(Arrays.asList(appDirectory.list()));
				if(alreadyThereProjects.size()>0)
				{
				userID=Integer.parseInt( alreadyThereProjects.get(0).substring(5));
				makeUser();
				loadUpAllPics();
				Log.e("SY", "THERE WAS ALREADY ONE:"+alreadyThereProjects.get(0).substring(5));
				}
				else{
					changeUser();
				}
			
		}


			
}

private void initCamera() {

}

  
  /*Camera functions
   * 
   */
  
// Called when shutter is opened
  ShutterCallback shutterCallback = new ShutterCallback() { // <6>
    public void onShutter() {
      Log.d(TAG, "onShutter'd");
    }
  };

  // Handles data for raw picture
  PictureCallback rawCallback = new PictureCallback() { // <7>
    public void onPictureTaken(byte[] data, Camera camera) {
      Log.d("SY", "onPictureTaken - raw");
    }
  };

  // Handles data for jpeg picture
  PictureCallback jpegCallback = new PictureCallback() { // <8>
    public void onPictureTaken(byte[] data, Camera camera) {
//      FileOutputStream outStream = null;
      picdata=data;
//      try {
//        // Write to SD Card
//       // outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis())); // <9>
////    	  outStream = new FileOutputStream(		  
////    			  String.format("/sdcard/Subway/%d.jpg", System.currentTimeMillis())); // <9>
//             	  outStream = new FileOutputStream(		  
//		  String.format("/sdcard/Subway_"+userID+"/"+"frame_0"+currentPicIndex+"__t_"+"%d.jpg", System.currentTimeMillis())); // <9>
////    	  pictureDirectory.mkdirs();
////    	  File newpic= new File(pictureDirectory.toURI()+"frame_0"+(FIRST_FRAME+currentPicIndex)+"__t_"+String.valueOf(System.currentTimeMillis())+ ".jpg");
////    	  newpic.createNewFile();
////    	  outStream = new FileOutputStream(newpic);
//    	  outStream.write(data);
//        outStream.close();
//        Log.d("SY", "onPictureTaken - wrote bytes: " + data.length);
//      } catch (FileNotFoundException e) { // <10>
//        e.printStackTrace();
//      } catch (IOException e) {
//        e.printStackTrace();
//      } finally {
//      }
      Log.d("SY", "onPictureTaken - jpeg");
      saveButton.setEnabled(true);
     
   // cameraView.camera.startPreview();
  
    }
  };
//END Camera functions
  
  

	
	private void chkSD() {
		//Check the SD card, make sure available
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;

		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		if (mExternalStorageAvailable) {
//			Toast.makeText(getApplicationContext(),
//					"External SD CARD storage available!", 1).show();

		} else {
			Toast.makeText(
					getApplicationContext(),
					"External SD CARD storage is NOT available - Please Insert an SD card",
					1).show();
		}
		if (mExternalStorageWriteable) {
//			Toast.makeText(getApplicationContext(),
//					"External SD CARD storage is writable!", 1).show();

		} else {
			Toast.makeText(
					getApplicationContext(),
					"External Storage is NOT writable - There is something wrong with your SD card",
					1).show();

		}
	}

	
	void changeUser(){
		// TODO Pop up a dialog to type in a new USER ID
		final FrameLayout fl = new FrameLayout(this);

		final EditText newUID = new EditText(this);
		final LinearLayout nameHolder = new LinearLayout(this);
		nameHolder.setOrientation(1);//Vertical
//		inputName.setGravity(Gravity.CENTER);
//		inputTitle.setGravity(Gravity.CENTER);
		//MAX 2 digits! HARDCODED
		//Limit them to a certain amount of digits
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(2);
		newUID.setFilters(FilterArray);
		
		nameHolder.addView(newUID, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT));
		
		fl.addView(nameHolder, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams. FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT));

		// input.setText("Preset Text");
		newUID.setHint("NUMBER: 00-99");
		newUID.setInputType(InputType.TYPE_CLASS_NUMBER);

		AlertDialog newprojPopUp = new AlertDialog.Builder(this).create();

		//Show the keyboard automatically
		newprojPopUp.setOnShowListener(new OnShowListener() {

		    @Override
		    public void onShow(DialogInterface dialog) {
		        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		        imm.showSoftInput(newUID, InputMethodManager.SHOW_FORCED);
		    }
		});
		
		newprojPopUp.setView(fl);

		newprojPopUp.setTitle("Enter your USER ID (00-99)");

		// Create Button
		newprojPopUp.setButton("Enter",
				new DialogInterface.OnClickListener() {

					public void onClick(
							final DialogInterface dMAIN,
							int which) {

						userID=Integer.parseInt(newUID.getText().toString().trim());
						userButton.setText("USER: "+userIDformatter.format(userID));
						Toast.makeText(
								getApplicationContext(),
								"New User ID = "+userID,
								1).show();
						makeUser();
						loadUpAllPics();


					}

				});
		// Cancel Button
		newprojPopUp.setButton2("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface d,
							int which) {

						 d.dismiss();

					}

				});

		newprojPopUp.show();
		newUID.requestFocus();
		newprojPopUp
				.getWindow()
				.setSoftInputMode(
						WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);


		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Change User");
		menu.add("Quit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle() == "Change User") {
			
			changeUser();
			
			
		} else if (item.getTitle() == "Quit") {
			finish();
			System.exit(0);
		}
		return true;
	}
}

