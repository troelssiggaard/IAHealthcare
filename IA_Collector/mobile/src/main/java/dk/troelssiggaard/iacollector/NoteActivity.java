package dk.troelssiggaard.iacollector;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import java.io.IOException;

import dk.troelssiggaard.iacollector.R;

public class NoteActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private ToggleButton toggleButton;

    private RadioGroup radioGroup1;
    private RadioGroup radioGroup2;
    private RadioGroup radioGroup3;


    private boolean isRunning = false;
    private DataLogger dataLogger;
    public String labelString;

    public EditText textInput1;
    public EditText textInput2;

    public String physical;
    public String work;
    public String interruptibility;
    public String timestampMillis;

    private static final long DELAY = 19; // 19 = (~50Hz)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup1.setOnCheckedChangeListener(this);
        textInput1 = (EditText) findViewById(R.id.editText1);

        radioGroup2 = (RadioGroup) findViewById(R.id.radioGroup2);
        radioGroup2.setOnCheckedChangeListener(this);
        textInput2 = (EditText) findViewById(R.id.editText2);

        radioGroup3 = (RadioGroup) findViewById(R.id.radioGroup3);
        radioGroup3.setOnCheckedChangeListener(this);

        toggleButton = (ToggleButton) findViewById(R.id.labelToggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    startLabelCollection();
                } else {
                    stopLabelCollection();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        timestampMillis = System.currentTimeMillis() + "";

        switch (checkedId) {

            case R.id.rBtn_stand:
                physical = "Standing";
                break;

            case R.id.rBtn_walk:
                physical = "Walking";
                break;

            case R.id.rBtn_run:
                physical = "Running";
                break;

            case R.id.rBtn_climb:
                physical = "Climbing stairs";
                break;

            case R.id.rBtn_sit:
                physical = "Sitting down";
                break;

            case R.id.rBtn_none1:
                physical = "None";
                break;

            case R.id.rBtn_txtInput1:
                String altLabel = textInput1.getText().toString();
                physical = altLabel.trim();
                break;

            case R.id.rBtn_meet:
                work = "Meeting";
                break;

            case R.id.rBtn_diag:
                work = "Diagnosing";
                break;

            case R.id.rBtn_report:
                work = "Reporting";
                break;

            case R.id.rBtn_oper:
                work = "Surgery";
                break;

            case R.id.rBtn_collab:
                work = "Collaborating";
                break;

            case R.id.rBtn_none2:
                work = "None";
                break;

            case R.id.rBtn_txtInput2:
                String altLabel2 = textInput2.getText().toString();
                work = altLabel2.trim();
                break;

            case R.id.rBtn_veryinterrupt:
                interruptibility = "V_Interruptible";   // Very Interruptible, i.e. not doing anything work related
                break;

            case R.id.rBtn_interrupt:
                interruptibility = "Interruptible";     // Interruptible, i.e. sitting at a desk, reporting
                break;

            case R.id.rBtn_unknown:
                interruptibility = "Unknown";           // Unknown, maybe interruptible, maybe not - but possibly not doing anything serious
                break;

            case R.id.rBtn_uninterrupt:
                interruptibility = "UnInterruptible";   // UnInterruptible, sitting in a meeting
                break;

            case R.id.rBtn_veryuninterrupt:
                interruptibility = "V_UnInterruptible"; // Very UnIterruptible, doing surgery or something else work-related with high concentration
                break;

            case R.id.rBtn_none3:
                interruptibility = "None";              // Skip these sensor values.
                break;
        }
    }

    public String conCat() {
        labelString = timestampMillis + "," + interruptibility + "," + work + "," + physical;
        return labelString;
    }

    public void startLabelCollection() {
        new Thread(new Runnable() {

            public void run() {

                timestampMillis = System.currentTimeMillis() + "";
                physical = "None";
                work = "None";
                interruptibility = "None";
                isRunning = true;

                try {
                    dataLogger = new DataLogger("LABEL.csv");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                while (isRunning) {
                    try {
                        String concat = conCat();
                        dataLogger.saveString(concat);
                        Thread.sleep(DELAY);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        }

    public void stopLabelCollection() {
        this.isRunning = false;
        String filePath = dataLogger.getFilePath();

        try {
            dataLogger.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(MainActivity.uploadData){
            FileUploader fileUploader = new FileUploader();
            fileUploader.execute(filePath);
        }
    }


}
