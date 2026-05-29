package com.cookandroid.passwordmanagerproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

public class ResultActivity extends AppCompatActivity {

    TextView tvScore, tvResult, tvSuggested, tvSuggestMsg, tvInputPassword;
    RatingBar ratingBar;
    Button btnSave, btnReturn;

    String password, result, suggestedPw;
    int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        setTitle("분석 결과");

        tvInputPassword = (TextView)  findViewById(R.id.tvInputPassword);
        tvScore      = (TextView)  findViewById(R.id.tvScore);
        tvResult     = (TextView)  findViewById(R.id.tvResult);
        tvSuggested  = (TextView)  findViewById(R.id.tvSuggested);
        tvSuggestMsg = (TextView)  findViewById(R.id.tvSuggestMsg);
        ratingBar    = (RatingBar) findViewById(R.id.ratingBar);
        btnSave      = (Button)    findViewById(R.id.btnSave);
        btnReturn    = (Button)    findViewById(R.id.btnReturn);

        // MainActivity에서 전달받은 데이터
        Intent intent = getIntent();
        password = intent.getStringExtra("password");
        score    = intent.getIntExtra("score", 0);
        result   = intent.getStringExtra("result");

        tvInputPassword.setText(password); //입력비밀번호 표시

        // 점수
        tvScore.setText(score + "점");
        tvScore.setTextColor(getScoreColor(score));

        // 판정 결과
        tvResult.setText(result);
        tvResult.setTextColor(getScoreColor(score));


        // 별점 설정 
        ratingBar.setRating(getRatingFromScore(score));

        // 강화 제안 비밀번호 생성
        suggestedPw = suggestPassword(password);

        if (suggestedPw.equals(password)) {             // 이미 강한 경우
            tvSuggested.setText("✅ 이미 매우 안전합니다!");
            tvSuggestMsg.setText("그대로 사용하셔도 됩니다.");
        } else {
            tvSuggested.setText(suggestedPw);
            tvSuggestMsg.setText("※ 위 비밀번호로 변경하면 더 안전해요!");
        }

        // 검사 기록 저장 버튼
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToDB();
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

    int getScoreColor(int score) {
        if (score >= 60) return Color.parseColor("#00AA00"); // 초록
        else if (score >= 40) return Color.parseColor("#FFA500"); // 노랑
        else return Color.parseColor("#FF0000");       // 빨강
    }


    // 점수 → 별점
    float getRatingFromScore(int score) {
        if (score >= 80) return 5;
        else if (score >= 60) return 4;
        else if (score >= 40) return 3;
        else if (score >= 20) return 2;
        else return 1;
    }



    String suggestPassword(String pw) {   // 강화 제안 비밀번호 생성
        String suggested = pw;

        //  대문자 없으면 첫 글자 대문자로
        if (!suggested.matches(".*[A-Z].*")) {
            suggested = Character.toUpperCase(suggested.charAt(0))
                    + suggested.substring(1);
        }

        //숫자 없으면 끝에 랜덤 숫자 2개 추가
        if (!suggested.matches(".*[0-9].*")) {
            int rand = (int)(Math.random() * 90) + 10; // 10~99
            suggested = suggested + rand;
        }

        // 특수문자 없으면 중간에 삽입
        if (!suggested.matches(".*[!@#?~].*")) {
            String[] specials = {"!", "@", "#", "?", "~"};
            String special = specials[(int)(Math.random() * specials.length)];
            int mid = suggested.length() / 2;
            suggested = suggested.substring(0, mid) + special + suggested.substring(mid);
        }

        return suggested;
    }


    void saveToDB() {     // SQLite에 검사 기록 저장

        DBHelper dbHelper = new DBHelper(this);
        android.database.sqlite.SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();

        // 비밀번호 마스킹 처리 (앞 3자리 + ***)
        String masked = password.length() > 3
                ? password.substring(0, 3) + "***"
                : "***";

        // 현재 시간
        String time = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
        ).format(new java.util.Date());

        sqlDB.execSQL("INSERT INTO scan_logs (pw_masked, risk_score, result_text, created_at) VALUES ('"
                + masked + "', "
                + score + ", '"
                + result + "', '"
                + time + "')");

        sqlDB.close();
        Toast.makeText(this, "검사 기록이 저장되었습니다!", Toast.LENGTH_SHORT).show();

    }



}
