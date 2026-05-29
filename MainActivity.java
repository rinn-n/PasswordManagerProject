package com.cookandroid.passwordmanagerproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText edtPassword;
    Button btnAnalyze, btnHistory, btnSavedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("비밀번호 보안 매니저");

        edtPassword   = (EditText) findViewById(R.id.edtPassword);
        btnAnalyze    = (Button)   findViewById(R.id.btnAnalyze);
        btnHistory    = (Button)   findViewById(R.id.btnHistory);
        btnSavedList  = (Button)   findViewById(R.id.btnSavedList);

        // 강도 분석 버튼 클릭
        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pw = edtPassword.getText().toString();

                // 빈칸 체크
                if (pw.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 강도 분석 계산
                int score = analyzePassword(pw);
                String result = getResultText(score);

                // ResultActivity로 이동 + 데이터 전달
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("password", pw);
                intent.putExtra("score", score);
                intent.putExtra("result", result);
                startActivity(intent);
            }
        });

        // 검사 기록 보기 버튼
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        // 저장된 비밀번호 목록 버튼
        btnSavedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SavedListActivity.class);
                startActivity(intent);
            }
        });
    }

    int analyzePassword(String pw) {
        int score = 0;

        //길이 점수
        if (pw.length() >= 12) score += 30;
        else if (pw.length() >= 8) score += 15;
        else score += 5;

        //대문자 포함
        if (pw.matches(".*[A-Z].*")) score += 15;

        //소문자 포함
        if (pw.matches(".*[a-z].*")) score += 15;

        //숫자 포함
        if (pw.matches(".*[0-9].*")) score += 20;

        // 특수문자 포함
        if (pw.matches(".*[!@#$%^&*()].*")) score += 20;

        // 반복문자 패턴 감점 (예: aaaa, 1111)
        if (pw.matches(".*(.)\\1{2,}.*")) score -= 10;

        // 0~100 범위 고정
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        return score;
    }

    String getResultText(int score) {
        if (score >= 80) return "매우 강함";
        else if (score >= 60) return "강함";
        else if (score >= 40) return "보통";
        else if (score >= 20) return "약함";
        else return "매우 약함";
    }
}