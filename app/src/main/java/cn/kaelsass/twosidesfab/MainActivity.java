package cn.kaelsass.twosidesfab;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import cn.kaelsass.twosidesfab.utils.AnimateUtil;

public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private FloatingActionsMenu captureButton;
    private FloatingActionButton camTrash;
    private FloatingActionButton camAccept;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camTrash = (FloatingActionButton) findViewById(R.id.btn_trash);

        camAccept = (FloatingActionButton) findViewById(R.id.btn_accept);

        captureButton = (FloatingActionsMenu) findViewById(R.id.btn_capture);

        setUpListenerForCamButtons();
    }

    private void setUpListenerForCamButtons() {

        camAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimateUtil.showFab(captureButton.getAddButton());
                captureButton.collapse();
            }
        });

        camTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimateUtil.showFab(captureButton.getAddButton());
                captureButton.collapse();
            }
        });

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AnimateUtil.hideFab(captureButton.getAddButton());
                        captureButton.expand();

                    }
                }
        );

    }
}
