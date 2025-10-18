package com.example.meltingbooks.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide; // 이미지 로딩용 (Glide 추가 필요)
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.profile.UpdateUserRequest;
import com.example.meltingbooks.network.profile.UserResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingProfile extends AppCompatActivity {

    private EditText etNickname, etUserId, etBio;
    private ImageView ivProfile;
    private TextView btnSave;
    private ImageButton btnProfileImage;
    private Uri selectedImageUri;  // 갤러리에서 선택한 이미지 Uri

    private UserResponse currentUser; // 기존 프로필 정보 저장용

    private ApiService apiService;
    private String token;
    private int userId;

    // 프로필 이미지 선택 버튼
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);

        // 상태바 색상 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // SharedPreferences에서 token, userId 가져오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("ProfileEdit", "토큰 또는 사용자 ID 없음");
            return;
        }

        // 뷰 초기화
        etNickname = findViewById(R.id.NicknameEditText);
        etUserId = findViewById(R.id.UserIDEditText);
        etBio = findViewById(R.id.BioEditText);
        ivProfile = findViewById(R.id.profile_imageView);
        btnSave = findViewById(R.id.bnt_setting_save);
        btnProfileImage = findViewById(R.id.bnt_ProfileImage);

        apiService = ApiClient.getClient(token).create(ApiService.class);

        // 기존 프로필 불러오기
        loadUserProfile();


        // onCreate 안에 초기화
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        // 여기서 imageUri를 서버 업로드 메서드에 넘기면 돼요
                        uploadProfileImage(imageUri);
                    }
                }
        );

        // 버튼 클릭 이벤트
        btnProfileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });


        // 저장 버튼 클릭 이벤트
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadUserProfile() {
        Call<UserResponse> getCall = apiService.getUser("Bearer " + token, userId);
        getCall.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();

                    // UI에 기존 값 반영
                    etNickname.setText(currentUser.getNickname());
                    etUserId.setText(String.valueOf(currentUser.getTagId()));
                    etBio.setText(currentUser.getBio());

                    // Glide로 프로필 이미지 표시
                    if (currentUser.getProfileImageUrl() != null) {
                        Glide.with(SettingProfile.this)
                                .load(currentUser.getProfileImageUrl())
                                .placeholder(R.drawable.sample_profile) // 기본 이미지
                                .into(ivProfile);
                    }
                } else {
                    Toast.makeText(SettingProfile.this, "프로필 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("ProfileEdit", "프로필 불러오기 실패: " + t.getMessage());
            }
        });
    }
    private void saveProfileChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "기존 프로필 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nickname = etNickname.getText().toString().trim();
        String tagIdStr = etUserId.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (nickname.isEmpty()) nickname = currentUser.getNickname();
        if (tagIdStr.isEmpty()) tagIdStr = String.valueOf(currentUser.getTagId());
        if (bio.isEmpty()) bio = currentUser.getBio();

        String email = currentUser.getEmail();
        String username = currentUser.getUsername();
        // 여기서 profileImage는 굳이 안 바꿔도 됨
        String profileImage = currentUser.getProfileImageUrl();

        UpdateUserRequest request = new UpdateUserRequest(email, nickname, username, bio, tagIdStr, profileImage);

        Call<UserResponse> call = apiService.updateUserProfile("Bearer " + token, userId, request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SettingProfile.this, "프로필 수정 성공!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SettingProfile.this, "수정 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(SettingProfile.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadProfileImage(Uri imageUri) {
        try {
            File file = new File(getCacheDir(), "temp_image.jpg");
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Call<ApiResponse<String>> call = apiService.uploadProfileImage("Bearer " + token, userId, body);
            call.enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String uploadedUrl = response.body().getData();

                        // 임시로 currentUser에만 저장
                        currentUser.setProfileImageUrl(uploadedUrl);

                        // 미리보기만 바꿔줌
                        Glide.with(SettingProfile.this)
                                .load(uploadedUrl)
                                .placeholder(R.drawable.sample_profile)
                                .into(ivProfile);

                        Toast.makeText(SettingProfile.this, "이미지가 선택되었습니다. 저장을 눌러 반영하세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingProfile.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Toast.makeText(SettingProfile.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}