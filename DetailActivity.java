package com.cookandroid.passwordmanagerproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    TextView tvDetailMasked, tvDetailScore, tvDetailResult, tvDetailTime;
    Button btnDelete, btnReturn;
    DBHelper dbHelper;
    int logId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setTitle("검사 기록 상세");

        tvDetailMasked = (TextView) findViewById(R.id.tvDetailMasked);
        tvDetailScore  = (TextView) findViewById(R.id.tvDetailScore);
        tvDetailResult = (TextView) findViewById(R.id.tvDetailResult);
        tvDetailTime   = (TextView) findViewById(R.id.tvDetailTime);
        btnDelete      = (Button)   findViewById(R.id.btnDelete);
        btnReturn      = (Button)   findViewById(R.id.btnReturn);
        dbHelper       = new DBHelper(this);

        // HistoryActivity에서 전달받은 id (코드11번 패턴)
        logId = getIntent().getIntExtra("logId", -1);

        // DB에서 해당 기록 불러오기
        loadDetail();

        // 삭제 버튼 - AlertDialog 확인 후 삭제 (코드3번 패턴)
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(DetailActivity.this);
                dlg.setTitle("기록 삭제");
                dlg.setMessage("이 검사 기록을 삭제할까요?");
                dlg.setIcon(android.R.drawable.ic_dialog_alert);

                dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
                        sqlDB.execSQL("DELETE FROM scan_logs WHERE id=" + logId);
                        sqlDB.close();

                        Toast.makeText(DetailActivity.this,
                                "삭제되었습니다", Toast.LENGTH_SHORT).show();
                        finish(); // 삭제 후 돌아가기 (코드9번 패턴)
                    }
                });

                dlg.setNegativeButton("취소", null);
                dlg.show();
            }
        });

        // 돌아가기 버튼
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // =============================================
    // DB에서 해당 id의 기록 불러오기 (코드12번 Cursor 패턴)
    // =============================================
    void loadDetail() {
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(
                "SELECT * FROM scan_logs WHERE id=" + logId, null);

        if (cursor.moveToNext()) {
            String masked = cursor.getString(1);
            int    score  = cursor.getInt(2);
            String result = cursor.getString(3);
            String time   = cursor.getString(4);

            tvDetailMasked.setText(masked);
            tvDetailScore.setText(score + "점");
            tvDetailResult.setText(result);
            tvDetailTime.setText(time);
        }

        cursor.close();
        sqlDB.close();
    }
}