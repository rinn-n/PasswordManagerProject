package com.cookandroid.passwordmanagerproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {

    // DB 파일 이름, 버전 설정
    public DBHelper(Context context) {
        super(context, "passwordDB", null, 1);
    }

    // 앱 처음 실행할 때 테이블 2개 자동 생성
    @Override
    public void onCreate(SQLiteDatabase db) {

        // 테이블1: 검사 기록
        db.execSQL("CREATE TABLE scan_logs (" +
                "id          INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pw_masked   TEXT, " +
                "risk_score  INTEGER, " +
                "result_text TEXT, " +
                "created_at  TEXT)");

        // 테이블2: 저장된 비밀번호 목록
        db.execSQL("CREATE TABLE saved_passwords (" +
                "id           INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "service_name TEXT, " +
                "username     TEXT, " +
                "password     TEXT, " +
                "created_at   TEXT)");
    }

    // DB 버전 올라갈 때 (초기화할 때 사용)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS scan_logs");
        db.execSQL("DROP TABLE IF EXISTS saved_passwords");
        onCreate(db);
    }
}