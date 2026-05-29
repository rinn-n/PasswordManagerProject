package com.cookandroid.passwordmanagerproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    ListView listHistory;
    Button btnDeleteAll;
    DBHelper dbHelper;
    ArrayList<String> listData;   // 화면에 보여줄 텍스트 목록
    ArrayList<Integer> listIds;   // DB id 목록 (삭제할 때 필요)
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle("검사 기록");

        listHistory  = (ListView) findViewById(R.id.listHistory);
        btnDeleteAll = (Button)   findViewById(R.id.btnDeleteAll);
        dbHelper     = new DBHelper(this);

        listData = new ArrayList<>();
        listIds  = new ArrayList<>();

        // ArrayAdapter로 ListView에 연결
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listData);
        listHistory.setAdapter(adapter);

        // DB에서 기록 불러오기
        loadHistory();

        // 항목 클릭 → DetailActivity로 이동 (코드9,11번 패턴)
        listHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("logId", listIds.get(position));
                startActivity(intent);
            }
        });

        // 전체 삭제 버튼 - AlertDialog 확인 후 삭제 (코드3번 패턴)
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(HistoryActivity.this);
                dlg.setTitle("전체 삭제");
                dlg.setMessage("검사 기록을 전부 삭제할까요?");
                dlg.setIcon(android.R.drawable.ic_dialog_alert);

                dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
                        sqlDB.execSQL("DELETE FROM scan_logs");
                        sqlDB.close();
                        loadHistory(); // 목록 새로고침
                        Toast.makeText(HistoryActivity.this,
                                "전체 삭제되었습니다", Toast.LENGTH_SHORT).show();
                    }
                });

                dlg.setNegativeButton("취소", null);
                dlg.show();
            }
        });
    }

    // onResume: DetailActivity에서 삭제 후 돌아왔을 때 목록 갱신
    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    // =============================================
    // DB에서 검사 기록 불러오기 (코드12번 Cursor 패턴)
    // =============================================
    void loadHistory() {
        listData.clear();
        listIds.clear();

        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(
                "SELECT * FROM scan_logs ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            int    id     = cursor.getInt(0);
            String masked = cursor.getString(1);
            int    score  = cursor.getInt(2);
            String result = cursor.getString(3);
            String time   = cursor.getString(4);

            listData.add(masked + " | " + score + "점 | " + result + "\n" + time);
            listIds.add(id);
        }

        cursor.close();
        sqlDB.close();

        adapter.notifyDataSetChanged(); // ListView 갱신
    }
}