package kr.co.tacademy.mongsil.mongsil;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by ccei on 2016-08-04.
 */
public class EditProfileActivity extends AppCompatActivity {

    // 툴바
    TextView tbCancel, tbDone;

    // 프로필 사진
    LinearLayout imgProfileContainer;

    // 기본정보
    LinearLayout editNameContainer, editLocationContainer;
    EditText editName, editLocation;

    // 계정삭제
    RelativeLayout leaveProfileContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 툴바 추가
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        tbCancel = (TextView) toolbar.findViewById(R.id.toolbar_cancel);
        tbCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tbDone = (TextView) toolbar.findViewById(R.id.toolbar_done);
        tbDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 서버에 프로필 편집한 내용을 저장하고 액티비티를 종료한다.
                finish();
            }
        });

        imgProfileContainer =
                (LinearLayout) findViewById(R.id.img_profile_container);
        imgProfileContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .add(BottomDialogFragment.newInstance(0), "bottom")
                        .addToBackStack("bottom").commit();
            }
        });

        editNameContainer =
                (LinearLayout) findViewById(R.id.edit_name_container);
        editName = (EditText) findViewById(R.id.edit_name);
        editNameContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editName.requestFocus();
            }
        });
        editLocationContainer =
                (LinearLayout) findViewById(R.id.edit_location_container);
        editLocation = (EditText) findViewById(R.id.edit_location);
        editLocationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editLocation.requestFocus();
            }
        });

        leaveProfileContainer =
                (RelativeLayout) findViewById(R.id.leave_profile_container);
        leaveProfileContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO : 계정을 삭제하시겠습니까?~~ 다이어로그 떠야함
                getSupportFragmentManager().beginTransaction()
                        .add(BottomDialogFragment.newInstance(0), "bottom")
                        .addToBackStack("bottom").commit();
            }
        });
    }
}