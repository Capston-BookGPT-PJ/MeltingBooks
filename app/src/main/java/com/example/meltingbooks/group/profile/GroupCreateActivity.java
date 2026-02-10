package com.example.meltingbooks.group.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.meltingbooks.group.GroupActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.group.Group;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.group.feed.GroupPostResponse;
import com.example.meltingbooks.network.group.GroupProfileResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;


public class GroupCreateActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private EditText groupNameEditText, groupIntroEditText, etCustomTopic;
    private Spinner spinnerMainCategory, spinnerSubCategory;
    private Button groupProfileImage;
    private ImageView imageView;
    private ImageButton createButton;

    private Uri selectedImageUri = null;
    private boolean isSpinnerInitialized = false;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_new_create);

        groupNameEditText = findViewById(R.id.groupNameEditText);
        groupIntroEditText = findViewById(R.id.groupIntroEditText);
        etCustomTopic = findViewById(R.id.etCustomTopic);
        spinnerMainCategory = findViewById(R.id.spinnerMainCategory);
        spinnerSubCategory = findViewById(R.id.spinnerSubCategory);
        groupProfileImage = findViewById(R.id.groupProfileImage);
        imageView = findViewById(R.id.imageView);
        createButton = findViewById(R.id.createButton);

        setupSpinners();

        createButton.setOnClickListener(v -> validateAndCreateGroup());
        groupProfileImage.setOnClickListener(v -> openImagePicker());

        //그룹 대표 이미지 불러오기
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                }
        );

        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> mainAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.group_category_main,
                R.layout.spinner_item
        );
        mainAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMainCategory.setAdapter(mainAdapter);

        spinnerMainCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true;
                    spinnerSubCategory.setVisibility(View.GONE);
                    etCustomTopic.setVisibility(View.VISIBLE);
                    return;
                }

                switch (position) {
                    case 0: // 선택 안 함
                        spinnerSubCategory.setVisibility(View.GONE);
                        etCustomTopic.setVisibility(View.GONE);
                        break;
                    case 1: // 장르
                        showSubCategory(R.array.group_category_genre);
                        break;
                    case 2: // 목적
                        showSubCategory(R.array.group_category_purpose);
                        break;
                    case 3: // 관심사
                        showSubCategory(R.array.group_category_interest);
                        break;
                    default:
                        spinnerSubCategory.setVisibility(View.GONE);
                        etCustomTopic.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {
                spinnerSubCategory.setVisibility(View.GONE);
                etCustomTopic.setVisibility(View.GONE);
            }
        });
    }

    private void showSubCategory(int arrayResId) {
        ArrayAdapter<CharSequence> subAdapter = ArrayAdapter.createFromResource(
                this,
                arrayResId,
                R.layout.spinner_item
        );
        subAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSubCategory.setAdapter(subAdapter);
        spinnerSubCategory.setVisibility(View.VISIBLE);
        etCustomTopic.setVisibility(View.GONE);
    }



    /*private void validateAndCreateGroup() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = groupNameEditText.getText().toString().trim();
        String description = groupIntroEditText.getText().toString().trim();

        if (name.length() < 3) {
            Toast.makeText(this, "그룹 이름은 3자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.length() > 500) {
            Toast.makeText(this, "그룹 소개글은 500자 이하로 작성해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category;
        if (etCustomTopic.getVisibility() == View.VISIBLE) {
            category = etCustomTopic.getText().toString().trim();
            if (TextUtils.isEmpty(category)) {
                Toast.makeText(this, "카테고리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            String mainCat = (String) spinnerMainCategory.getSelectedItem();
            String subCat = spinnerSubCategory.getVisibility() == View.VISIBLE
                    ? (String) spinnerSubCategory.getSelectedItem()
                    : "";
            category = mainCat + (subCat.isEmpty() ? "" : " > " + subCat);
        }

        // 선택된 이미지가 없으면 기본 이미지 URL 사용
        String groupImageUrl = (selectedImageUri != null) ? selectedImageUri.toString() : "https://i.imgur.com/iWf9Yuh.jpeg";

        Group group = new Group(name, description, groupImageUrl, category);


        GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

        // 그룹 단일 타입-> GroupSingleList 사용
        groupApi.createGroup(group).enqueue(new retrofit2.Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GroupProfileResponse groupResponse = response.body().getData(); // 🔹 바로 객체 접근

                    Toast.makeText(GroupCreateActivity.this,
                            "그룹 생성 성공: " + groupResponse.getName(),
                            Toast.LENGTH_SHORT).show();

                    // 그룹 홈으로 이동
                    Intent intent = new Intent(GroupCreateActivity.this, GroupActivity.class);
                    intent.putExtra("newGroup", groupResponse); // Serializable 전달
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(GroupCreateActivity.this, "그룹 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                Toast.makeText(GroupCreateActivity.this, "에러: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }*/

    private void validateAndCreateGroup() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = groupNameEditText.getText().toString().trim();
        String description = groupIntroEditText.getText().toString().trim();

        if (name.length() < 3) {
            Toast.makeText(this, "그룹 이름은 3자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.length() > 500) {
            Toast.makeText(this, "그룹 소개글은 500자 이하로 작성해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String category;
        if (etCustomTopic.getVisibility() == View.VISIBLE) {
            category = etCustomTopic.getText().toString().trim();
            if (TextUtils.isEmpty(category)) {
                Toast.makeText(this, "카테고리를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            String mainCat = (String) spinnerMainCategory.getSelectedItem();
            String subCat = spinnerSubCategory.getVisibility() == View.VISIBLE
                    ? (String) spinnerSubCategory.getSelectedItem()
                    : "";
            category = mainCat + (subCat.isEmpty() ? "" : " > " + subCat);
        }

        GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

        // 1️⃣ 그룹 생성
        Group group = new Group(name, description, "", category);
        groupApi.createGroup(group).enqueue(new retrofit2.Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GroupProfileResponse groupResponse = response.body().getData();
                    int groupId = groupResponse.getId();

                    if (selectedImageUri != null) {
                        try {
                            File file = createTempFileFromUri(selectedImageUri);
                            String mimeType = getContentResolver().getType(selectedImageUri);
                            RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));
                            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                            // 2️⃣ 이미지 업로드
                            groupApi.uploadGroupImage("Bearer " + token, groupId, body)
                                    .enqueue(new retrofit2.Callback<ApiResponse<String>>() {
                                        @Override
                                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                                            goToGroupHome(groupResponse);
                                        }

                                        @Override
                                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                                            Toast.makeText(GroupCreateActivity.this, "이미지 업로드 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                            goToGroupHome(groupResponse);
                                        }
                                    });

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(GroupCreateActivity.this, "이미지 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            goToGroupHome(groupResponse);
                        }
                    } else {
                        goToGroupHome(groupResponse);
                    }

                } else {
                    Toast.makeText(GroupCreateActivity.this, "그룹 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                Toast.makeText(GroupCreateActivity.this, "서버 연결 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // 그룹 홈 이동
    private void goToGroupHome(GroupProfileResponse groupResponse) {
        Intent intent = new Intent(GroupCreateActivity.this, GroupActivity.class);
        intent.putExtra("newGroup", groupResponse);
        startActivity(intent);
        finish();
    }

    // Uri → 임시 File 변환
    private File createTempFileFromUri(Uri uri) throws Exception {
        String fileName = getFileName(uri);
        File tempFile = new File(getCacheDir(), fileName);
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        }
        return tempFile;
    }

    // Uri에서 파일명 추출
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) result = cursor.getString(idx);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


}
