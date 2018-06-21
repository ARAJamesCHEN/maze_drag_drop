package nz.ara.game.view.activity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;

import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.yac0105.game.R;
import com.example.yac0105.game.databinding.ActivityMainBinding;

import java.io.File;

import nz.ara.game.model.em.constvalue.Const;
import nz.ara.game.util.DisplayParams;
import nz.ara.game.util.DisplayUtil;
import nz.ara.game.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Spinner level_spinner;

    private ImageView mapView1;

    private ImageView theView;

    private ImageView minView;

    private int stepWidthX = 100;

    private int stepWidthY = 100;

    private int startPointX = 100;

    private int startPointY = 200;

    private String itemsWallLeftStr;

    private String itemsWallAboveStr;

    private String thePointStr;

    private String minPointStr;

    private Paint drawPaint;

    Canvas canvas;

    private int mHeight = 100;
    private int mWidth = 100;

    private String wallSquareStr;

    private Button reset;

    private Button pause;

    private Button save;

    private Button loadByFile;

    private Button help;

    private String level_string = "Level-1";

    private MainViewModel mainViewModel;

    private Context context;

    private ActivityMainBinding binding;

    private int rolePointXShort = 100;

    private int rolePointXLong = 100;

    private int rolePointYShort = 200;

    private int rolePointYLong = 200;

    private float startX;

    private float startY;

    private File directory;

    private String fileP;

    private boolean isSaveSuccessful = false;

    private boolean isLoadSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mapView1 = findViewById(R.id.mapview);

        if(mapView1 == null){
            FrameLayout f = findViewById(R.id.frameLayout);

            mapView1 = (ImageView) f.getChildAt(0);

            theView = (ImageView) f.getChildAt(1);

            minView = (ImageView)f.getChildAt(2);

            theView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return roleViewOnTouched(event);
                }
            });
        }



        level_spinner = findViewById(R.id.level_spinner);

        level_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                spinnerItemSelected();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });


        reset = findViewById(R.id.button_reset);

        reset.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetButtonClicked();
                }
            }
        );

        pause = findViewById(R.id.button_pause);

        pause.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pauseButtonClicked();
                    }
                }
        );

        save = findViewById(R.id.button_save);

        save.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveButtonClicked();
                    }
                }
        );


        loadByFile = findViewById(R.id.button_new);

        loadByFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadByFileButtonClicked();
            }}
        );

        help = findViewById(R.id.button_help);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpButtonClicked();
            }}
        );

        if(mainViewModel == null){
            mainViewModel = new MainViewModel(this,level_string);
        }

        this.drawMapByImageView();

        this.drawRoleByImageView(theView,   getResources().getString(R.string.ROLE_TYPE_THESEUS));
        this.drawRoleByImageView(minView,   getResources().getString(R.string.ROLE_TYPE_MINOTAUR));
        binding.setMainViewModel(mainViewModel);

    }

    private void setParas(){
        this.thePointStr = mainViewModel.thePointStr.get();
        this.itemsWallAboveStr = mainViewModel.wallAbovePointListStr.get();
        this.itemsWallLeftStr = mainViewModel.wallLeftPointListStr.get();
        this.minPointStr = mainViewModel.minPointStr.get();
        this.wallSquareStr =  mainViewModel.wallSquareStr.get();
    }

    private void drawMapByImageView(){
        this.drawMapByAttrs();
        this.setParas();
        this.calParas();
        Bitmap bitmap = Bitmap.createBitmap(mWidth, (int) mWidth,
                Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);

        this.drawMap(canvas, this.itemsWallAboveStr, getResources().getString(R.string.WALL_TYPE_ABOVE));
        this.drawMap(canvas,this.itemsWallLeftStr, getResources().getString(R.string.WALL_TYPE_LEFT));

        mapView1.setImageBitmap(bitmap);

        mapView1.invalidate();
    }

    private void drawRoleByImageView(ImageView imageView, String roleStr){
        this.setParas();
        //this.calParas();

        Bitmap bitmap = Bitmap.createBitmap(mWidth,  mWidth,
                Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);

        if(roleStr!=null && roleStr.equals(getResources().getString(R.string.ROLE_TYPE_THESEUS))){
            this.drapRole(canvas, this.thePointStr, roleStr );
        }else if(roleStr!=null && roleStr.equals(getResources().getString(R.string.ROLE_TYPE_MINOTAUR))){
            this.drapRole(canvas, this.minPointStr, roleStr );
        }else{
            Log.e(TAG, "Error Type:" + roleStr);
        }

        imageView.setImageBitmap(bitmap);
        imageView.invalidate();
    }

    private void drawMapByAttrs(){
        drawPaint = new Paint(Paint.DITHER_FLAG);
        drawPaint.setAntiAlias(true);
        drawPaint.setColor(Color.BLACK);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    private void    calParas(){

        int countX = 4;
        int countY = 4;

        if(wallSquareStr!=null && wallSquareStr.trim().length()>0){
            Log.d(TAG, "Wall suare:" + wallSquareStr);

            String[] wallSqurArray = wallSquareStr.split(",");

            countX = Integer.parseInt(wallSqurArray[0]);
            countY = Integer.parseInt(wallSqurArray[1]);

        }

        Display currentDisplay = getWindowManager().getDefaultDisplay();


        DisplayParams  displayParams = DisplayParams.getInstance(context);
        mWidth =  DisplayUtil.dip2px(342, displayParams.scale);//currentDisplay.getWidth();
        mHeight = DisplayUtil.dip2px(342, displayParams.scale);//currentDisplay.getHeight();

        this.stepWidthX = mWidth/(countX);
        this.stepWidthY = mHeight/(countY);

        this.startPointX = this.stepWidthX;

        TextView v = findViewById(R.id.textView_move_name);

        int hTx = v.getMeasuredHeight();
        this.startPointY = hTx + 12 + this.stepWidthY/2;
    }

    private void drawMap(Canvas canvas, String wallStr, String type){
        if(wallStr!=null && wallStr.trim().length()>0){
            String[] wallStrArray = wallStr.split("\\|");

            for(String pointStr : wallStrArray){

                String[] pointStrArray = pointStr.split(",");

                int pointX = Integer.parseInt(pointStrArray[0]);
                int pointY = Integer.parseInt(pointStrArray[1]);
                Log.d(TAG, "Point: " + pointX + "," + pointY);

                int drawPointX = startPointX + pointX*this.stepWidthX - this.stepWidthX/2;
                int drawPointY = startPointY + pointY*this.stepWidthX - this.stepWidthY/2;

                if(type!=null && type.equals(getResources().getString(R.string.WALL_TYPE_ABOVE))){
                    canvas.drawLine(drawPointX, drawPointY, drawPointX + this.stepWidthX, drawPointY, drawPaint);
                }else if(type!=null && type.equals(getResources().getString(R.string.WALL_TYPE_LEFT))){
                    canvas.drawLine(drawPointX, drawPointY, drawPointX, drawPointY + this.stepWidthY, drawPaint);
                }else{
                    Log.e(TAG, "Error Type:" + type);
                }

            }

        }
    }

    private void drapRole(Canvas canvas, String pointStr, String type){
        if(pointStr!=null && pointStr.trim().length()>0){

            String[] pointStrArray = pointStr.split(",");

            int pointX = Integer.parseInt(pointStrArray[0]);
            int pointY = Integer.parseInt(pointStrArray[1]);

            int left = startPointX + pointX*this.stepWidthX - this.stepWidthX/2 + 5;
            int top =  startPointY + pointY*this.stepWidthY - this.stepWidthY/2 + 5;
            int right = startPointX + pointX*this.stepWidthX + this.stepWidthX/2 - 5;
            int bottom = startPointY + pointY*this.stepWidthY + this.stepWidthY/2 - 5;


            Rect rectangle = new Rect(left,top,right,bottom);


            Bitmap bitmap = null;


            if(type!=null && type.equals(getResources().getString(R.string.ROLE_TYPE_THESEUS))){

                rolePointXShort = left;
                rolePointYShort = top;
                rolePointXLong = right;
                rolePointYLong = bottom;

                bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.theseus);

            }else if(type!=null && type.equals(getResources().getString(R.string.ROLE_TYPE_MINOTAUR))){
                bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.minotaur);
            }else{
                Log.e(TAG, "Error Type:" + type);
                return;
            }

            canvas.drawBitmap(bitmap, null, rectangle, null);


        }



    }

    private boolean roleViewOnTouched(MotionEvent event){

        switch (event.getAction()) {
            case MotionEvent.ACTION_SCROLL:
                 break;
            case MotionEvent.ACTION_DOWN:
                startX=event.getX();
                startY=event.getY();

                if(mainViewModel.moveThe(rolePointXShort,rolePointXLong,rolePointYShort,rolePointYLong,startX,startY)){

                    this.drawRoleByImageView(theView,   getResources().getString(R.string.ROLE_TYPE_THESEUS));

                    if(mainViewModel.getGameModel().getTheseus().isHasWon()){
                        playWin();
                        theWinDialog();
                    }
                }

                if(mainViewModel.moveMin()){
                    if(mainViewModel.getGameModel().getMinotaur().isHasEaten()){
                        minView.bringToFront();
                        playLost();
                        minKillTheDialog();
                    }

                    this.drawRoleByImageView(minView,   getResources().getString(R.string.ROLE_TYPE_MINOTAUR));
                }

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if(mainViewModel.moveMin()){

                    if(mainViewModel.getGameModel().getMinotaur().isHasEaten()){

                        minView.bringToFront();
                        playLost();
                        minKillTheDialog();
                    }

                    this.drawRoleByImageView(minView,   getResources().getString(R.string.ROLE_TYPE_MINOTAUR));

                }
                break;
            default:
                return false;
        }

        Log.d(TAG, "Touch Event::" + event.getAction());
        return true;
    }


    /**
     * spinnerItemSelected
     */
    private void spinnerItemSelected(){
        String aNewlevel_string = (String) level_spinner.getSelectedItem();

        if(!level_string.equals(aNewlevel_string)){
            level_string = aNewlevel_string;
            if(mainViewModel == null){
                mainViewModel = new MainViewModel(context,aNewlevel_string);
            }else{
                mainViewModel.initGameImpl(aNewlevel_string);
                theView.bringToFront();
                this.drawMapByImageView();
                this.drawRoleByImageView(theView,   getResources().getString(R.string.ROLE_TYPE_THESEUS));
                this.drawRoleByImageView(minView,   getResources().getString(R.string.ROLE_TYPE_MINOTAUR));
            }
        }
    }

    private void resetButtonClicked(){
        mainViewModel.initGameImpl(level_string);
        theView.bringToFront();
        this.drawMapByImageView();
        this.drawRoleByImageView(theView,   getResources().getString(R.string.ROLE_TYPE_THESEUS));
        this.drawRoleByImageView(minView,   getResources().getString(R.string.ROLE_TYPE_MINOTAUR));
    }

    private void pauseButtonClicked(){

        mainViewModel.moveMin();
        mainViewModel.moveMin();

        if(mainViewModel.getGameModel().getMinotaur().isHasEaten()){

            minView.bringToFront();
            playLost();
            minKillTheDialog();
        }

        this.drawRoleByImageView(minView,   getResources().getString(R.string.ROLE_TYPE_MINOTAUR));

    }

    private void saveButtonClicked(){
        mainViewModel.initGameImpl(level_string);

        directory = context.getFilesDir();

        fileP = directory.getAbsolutePath() + File.separator + Const.LEVEL_FILE_NAME.getValue();

        new Thread(new Runnable() {
            @Override
            public void run() {
                isLoadSuccessful =  mainViewModel.save(directory);
                Log.d(TAG,"Save to " + fileP + " successfully!" );
            }
        }).start();

        showSaveFileProgressDialog();
    }

    private void showSaveFileProgressDialog() {
        final int MAX_PROGRESS = 100;
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgress(0);
        progressDialog.setTitle("Saving");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(MAX_PROGRESS);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int progress= 0;

                while (progress < MAX_PROGRESS){
                    try {
                        Thread.sleep(100);
                        if(!isSaveSuccessful){
                            progress++;
                            progressDialog.setProgress(progress);
                        }else{
                            progressDialog.setProgress(MAX_PROGRESS);
                            progressDialog.cancel();
                            if(isSaveSuccessful){
                                isSaveSuccessful = false;
                            }
                        }

                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                progressDialog.cancel();
            }
        }).start();

    }

    private void loadByFileButtonClicked(){
        directory = context.getFilesDir();

        fileP = directory.getAbsolutePath() + File.separator + Const.LEVEL_FILE_NAME.getValue();

        new Thread(new Runnable() {
            @Override
            public void run() {
                isLoadSuccessful = mainViewModel.initGameImplByFile(level_string);
                Log.d(TAG,  "Load " + level_string + " from " + fileP + " successfully!" );
            }
        }).start();

        showLaodFileProgressDialog();


        fileP = directory.getAbsolutePath() + File.separator + Const.LEVEL_FILE_NAME.getValue();

    }

    private void  helpButtonClicked(){
        helpButtonDialog();
    }

    private void  helpButtonDialog(){

        final AlertDialog.Builder minKillTheDialog = new AlertDialog.Builder(this);

        minKillTheDialog.setTitle("HELP");

        minKillTheDialog.setMessage("As Theseus, you must escape the Minotaur's maze!\n" +
                "\n" +
                "For every move you make, the Minotaur makes two moves. Luckily, he isn't terribly bright. He will move toward Theseus, favoring horizontal over vertical moves, without knowing how to get around a wall in his way. Escape by luring the Minotaur into a place where he gets stuck!\n" +
                "\n" +
                "Code: Yang CHEN 99168512");

        minKillTheDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        minKillTheDialog.show();
    }

    private void showLaodFileProgressDialog() {
        final int MAX_PROGRESS = 100;
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgress(0);
        progressDialog.setTitle("Loading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(MAX_PROGRESS);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int progress= 0;

                while (progress < MAX_PROGRESS){
                    try {
                        Thread.sleep(100);
                        if(!isLoadSuccessful){
                            progress++;
                            progressDialog.setProgress(progress);
                        }else{
                            progressDialog.setProgress(MAX_PROGRESS);
                            progressDialog.cancel();
                            if(isLoadSuccessful){
                                isLoadSuccessful = false;
                            }

                            drawRoleByImageView(theView,   getResources().getString(R.string.ROLE_TYPE_THESEUS));
                            drawRoleByImageView(minView,   getResources().getString(R.string.ROLE_TYPE_MINOTAUR));
                        }

                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                progressDialog.cancel();
            }
        }).start();

    }



    private void minKillTheDialog(){

        final AlertDialog.Builder minKillTheDialog = new AlertDialog.Builder(this);

        minKillTheDialog.setTitle("Minotaur killed Theseus!");

        minKillTheDialog.setMessage("Game Over");

        minKillTheDialog.setPositiveButton("OK",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    minKillTheOptionDialog();
                }
         });
        minKillTheDialog.show();
    }

    private void minKillTheOptionDialog(){
        final AlertDialog.Builder theDialog = new AlertDialog.Builder(this);

        theDialog.setTitle("Do you like to play these level again?");

        theDialog.setMessage("");

        theDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                resetButtonClicked();
            }
        });

        theDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                level_string = "Level-1";
                resetButtonClicked();
                level_spinner.setSelection(0);
            }
        });
        theDialog.show();
    }

    private void theWinDialog(){

        final AlertDialog.Builder minKillTheDialog = new AlertDialog.Builder(this);

        minKillTheDialog.setTitle("Theseus win!");

        minKillTheDialog.setMessage("Congratulations~");

        minKillTheDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        theWinOptionDialog();
                    }
                });
        minKillTheDialog.show();
    }

    private void theWinOptionDialog(){
        final AlertDialog.Builder theDialog = new AlertDialog.Builder(this);

        theDialog.setTitle("Do you like to play these level again?");

        theDialog.setMessage("");

        theDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                resetButtonClicked();
            }
        });

        theDialog.setNegativeButton("Next Level", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int theNum = mainViewModel.getGameModel().getLevelByLevelStr(level_string);
                level_string = mainViewModel.getGameModel().getLevels()[theNum];
                resetButtonClicked();
                level_spinner.setSelection(theNum);
            }
        });
        theDialog.show();
    }


    public void playWin() {

        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.you_win_sound_effect);
        mediaPlayer.start();
    }

    public void playLost() {

        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.game_over_sound_effect);
        mediaPlayer.start();
    }

}
