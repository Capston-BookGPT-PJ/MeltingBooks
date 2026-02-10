package com.example.meltingbooks.group.profile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.group.GroupActivity;
import com.example.meltingbooks.network.group.Group;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.GroupProfileResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class GroupUpdateActivity extends AppCompatActivity {

    private EditText groupNameEditText, groupIntroEditText, etCustomTopic;
    private Spinner spinnerMainCategory, spinnerSubCategory;
    private ImageButton groupProfileImage;
    private ImageView imageView;
    private ImageButton updateButton;

    private Uri selectedImageUri = null;
    private GroupProfileResponse groupInfo;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_update);

        groupNameEditText = findViewById(R.id.groupNameEditText);
        groupIntroEditText = findViewById(R.id.groupIntroEditText);
        etCustomTopic = findViewById(R.id.etCustomTopic);
        spinnerMainCategory = findViewById(R.id.spinnerMainCategory);
        spinnerSubCategory = findViewById(R.id.spinnerSubCategory);
        groupProfileImage = findViewById(R.id.groupProfileImage);
        imageView = findViewById(R.id.imageView);
        updateButton = findViewById(R.id.updateButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        groupInfo = (GroupProfileResponse) getIntent().getSerializableExtra("groupInfo");
        if (groupInfo == null) {
            Toast.makeText(this, "그룹 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupSpinners();
        fillGroupData();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                }
        );
        groupProfileImage.setOnClickListener(v -> openImagePicker());

        updateButton.setOnClickListener(v -> validateAndUpdateGroup());
    }

    private void fillGroupData() {

        groupNameEditText.setText(groupInfo.getName());
        groupIntroEditText.setText(groupInfo.getDescription());

        String category = groupInfo.getCategory();

        if (category.contains(">")) {

            String[] parts = category.split(">");
            String main = parts[0].trim();
            String sub = parts[1].trim();

            // ✅ 리스너 제거
            spinnerMainCategory.setOnItemSelectedListener(null);

            // ✅ main 선택
            spinnerMainCategory.setSelection(getSpinnerIndex(spinnerMainCategory, main));

            // ✅ main에 맞는 sub adapter 로드
            int arrayId = getArrayIdByMain(main);
            showSubCategory(arrayId);

            // ✅ sub 선택 (반드시 post로!)
            spinnerSubCategory.post(() ->
                    spinnerSubCategory.setSelection(getSpinnerIndex(spinnerSubCategory, sub))
            );

            etCustomTopic.setVisibility(View.GONE);

            // ✅ 리스너 다시 부착
            setMainSpinnerListener();

        } else {
            spinnerSubCategory.setVisibility(View.GONE);
            etCustomTopic.setVisibility(View.VISIBLE);
            etCustomTopic.setText(category);

            setMainSpinnerListener();
        }
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> mainAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.group_category_main,
                R.layout.spinner_item
        );
        mainAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMainCategory.setAdapter(mainAdapter);
    }

    private void setMainSpinnerListener() {
        spinnerMainCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 1: showSubCategory(R.array.group_category_genre); break;
                    case 2: showSubCategory(R.array.group_category_purpose); break;
                    case 3: showSubCategory(R.array.group_category_interest); break;
                    default:
                        spinnerSubCategory.setVisibility(View.GONE);
                        etCustomTopic.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int getArrayIdByMain(String main) {
        switch (main) {
            case "장르": return R.array.group_category_genre;
            case "목적": return R.array.group_category_purpose;
            case "관심사": return R.array.group_category_interest;
            default: return -1;
        }
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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void validateAndUpdateGroup() {
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
            String main = (String) spinnerMainCategory.getSelectedItem();
            String sub = spinnerSubCategory.getVisibility() == View.VISIBLE
                    ? (String) spinnerSubCategory.getSelectedItem()
                    : "";
            category = main + (sub.isEmpty() ? "" : " > " + sub);
        }

        GroupController controller = new GroupController(this);
        controller.updateGroup(groupInfo.getId(),
                new Group(name, description, "", category),
                new GroupController.OnGroupActionCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        if (selectedImageUri != null) {
                            try {
                                File file = createTempFileFromUri(selectedImageUri);
                                String mimeType = getContentResolver().getType(selectedImageUri);
                                RequestBody requestFile = RequestBody.create(file, MediaType.parse(mimeType));
                                MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                                controller.uploadGroupImage(groupInfo.getId(), body, new GroupController.OnGroupActionCallback() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        goToGroupHome();
                                    }

                                    @Override
                                    public void onFailure(String message) {
                                        Toast.makeText(GroupUpdateActivity.this, "이미지 업로드 실패: " + message, Toast.LENGTH_SHORT).show();
                                        goToGroupHome();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(GroupUpdateActivity.this, "이미지 처리 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                goToGroupHome();
                            }
                        } else {
                            goToGroupHome();
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(GroupUpdateActivity.this, "수정 실패: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToGroupHome() {
        Intent intent = new Intent(GroupUpdateActivity.this, GroupActivity.class);
        startActivity(intent);
        finish();
    }

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

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) result = cursor.getString(idx);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) return i;
        }
        return 0;
    }
}
