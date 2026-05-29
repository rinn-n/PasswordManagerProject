package com.cookandroid.passwordmanagerproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SavedListActivity extends AppCompatActivity {

    ListView listSaved;
    Button btnAdd;
    DBHelper dbHelper;
    ArrayList<String> listData;
    ArrayList<Integer> listIds;
    ArrayAdapter<String> adapter;
    int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_list);
        setTitle("저장된 비밀번호 목록");

        listSaved = (ListView) findViewById(R.id.listSaved);
        btnAdd    = (Button)   findViewById(R.id.btnAdd);
        dbHelper  = new DBHelper(this);

        listData = new ArrayList<>();
        listIds  = new ArrayList<>();

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, listData);
        listSaved.setAdapter(adapter);

        registerForContextMenu(listSaved);
        loadSavedList();

        // 항목 클릭 → AlertDialog로 비밀번호 확인
        listSaved.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPasswordDialog(position);
            }
        });

        // 추가 버튼
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedList();
    }


    @Override     // 컨텍스트 메뉴
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedPosition = info.position;
        menu.setHeaderTitle(listData.get(selectedPosition));
        menu.add(0, 0, 0, "수정");
        menu.add(0, 1, 0, "삭제");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            showEditDialog(selectedPosition);
            return true;
        } else if (item.getItemId() == 1) {
            showDeleteDialog(selectedPosition);
            return true;
        }
        return false;
    }

    void loadSavedList() {      // DB 목록 불러오기
        listData.clear();
        listIds.clear();

        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(
                "SELECT * FROM saved_passwords ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            int    id          = cursor.getInt(0);
            String serviceName = cursor.getString(1);
            listData.add(serviceName); // 사용처 이름만 표시
            listIds.add(id);
        }

        cursor.close();
        sqlDB.close();
        adapter.notifyDataSetChanged();
    }


    String encrypt(String text) {     // 암호화 / 복호화 메서드
        return Base64.encodeToString(text.getBytes(), Base64.DEFAULT).trim();
    }

    String decrypt(String encoded) {
        return new String(Base64.decode(encoded, Base64.DEFAULT));
    }

    // =============================================
    // 항목 클릭 → 비밀번호 확인 AlertDialog (코드3번 패턴)
    // =============================================
    void showPasswordDialog(int position) {
        int id = listIds.get(position);
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(
                "SELECT * FROM saved_passwords WHERE id=" + id, null);

        if (cursor.moveToNext()) {
            String service  = cursor.getString(1);
            String username = cursor.getString(2);
            String encryptedPw = cursor.getString(3);

            // 복호화해서 보여주기
            String password = decrypt(encryptedPw);

            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle(service);
            dlg.setMessage("아이디: " + username + "\n비밀번호: " + password);
            dlg.setPositiveButton("확인", null);
            dlg.show();
        }
        cursor.close();
        sqlDB.close();
    }


    void showAddDialog() {      // 추가 AlertDialog
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("비밀번호 추가");

        final EditText edtService  = new EditText(this);
        final EditText edtUsername = new EditText(this);
        final EditText edtPassword = new EditText(this);

        edtService.setHint("사용처 (예: 네이버)");
        edtUsername.setHint("아이디");
        edtPassword.setHint("비밀번호");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(edtService);
        layout.addView(edtUsername);
        layout.addView(edtPassword);
        dlg.setView(layout);

        dlg.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String service  = edtService.getText().toString();
                String username = edtUsername.getText().toString();
                String password = edtPassword.getText().toString();

                if (service.isEmpty()) {
                    Toast.makeText(SavedListActivity.this,
                            "사용처를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 암호화 후 저장
                String encryptedPw = encrypt(password);

                String time = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        java.util.Locale.getDefault()
                ).format(new java.util.Date());

                SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
                sqlDB.execSQL("INSERT INTO saved_passwords " +
                        "(service_name, username, password, created_at) VALUES ('"
                        + service + "', '"
                        + username + "', '"
                        + encryptedPw + "', '"
                        + time + "')");
                sqlDB.close();

                loadSavedList();
                Toast.makeText(SavedListActivity.this,
                        "저장되었습니다!", Toast.LENGTH_SHORT).show();
            }
        });

        dlg.setNegativeButton("취소", null);
        dlg.show();
    }


    void showEditDialog(int position) {      // AlertDialog
        int id = listIds.get(position);
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(
                "SELECT * FROM saved_passwords WHERE id=" + id, null);

        if (cursor.moveToNext()) {
            String oldService  = cursor.getString(1);
            String oldUsername = cursor.getString(2);
            String oldEncrypted = cursor.getString(3);

            // 복호화해서 입력창에 표시
            String oldPassword = decrypt(oldEncrypted);
            cursor.close();
            sqlDB.close();

            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("수정");

            final EditText edtService  = new EditText(this);
            final EditText edtUsername = new EditText(this);
            final EditText edtPassword = new EditText(this);

            edtService.setText(oldService);
            edtUsername.setText(oldUsername);
            edtPassword.setText(oldPassword);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 20, 40, 20);
            layout.addView(edtService);
            layout.addView(edtUsername);
            layout.addView(edtPassword);
            dlg.setView(layout);

            dlg.setPositiveButton("수정 완료", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newService  = edtService.getText().toString();
                    String newUsername = edtUsername.getText().toString();
                    String newPassword = edtPassword.getText().toString();

                    // 수정된 비밀번호도 암호화 저장
                    String newEncrypted = encrypt(newPassword);

                    SQLiteDatabase sqlDB2 = dbHelper.getWritableDatabase();
                    sqlDB2.execSQL("UPDATE saved_passwords SET " +
                            "service_name='" + newService + "', " +
                            "username='" + newUsername + "', " +
                            "password='" + newEncrypted + "' " +
                            "WHERE id=" + id);
                    sqlDB2.close();

                    loadSavedList();
                    Toast.makeText(SavedListActivity.this,
                            "수정되었습니다!", Toast.LENGTH_SHORT).show();
                }
            });

            dlg.setNegativeButton("취소", null);
            dlg.show();
        }
    }

    // 삭제 확인 AlertDialog
    void showDeleteDialog(int position) {
        int id = listIds.get(position);
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("삭제");
        dlg.setMessage("[" + listData.get(position) + "] 를 삭제할까요?");
        dlg.setIcon(android.R.drawable.ic_dialog_alert);

        dlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
                sqlDB.execSQL("DELETE FROM saved_passwords WHERE id=" + id);
                sqlDB.close();
                loadSavedList();
                Toast.makeText(SavedListActivity.this,
                        "삭제되었습니다", Toast.LENGTH_SHORT).show();
            }
        });

        dlg.setNegativeButton("취소", null);
        dlg.show();
    }
}