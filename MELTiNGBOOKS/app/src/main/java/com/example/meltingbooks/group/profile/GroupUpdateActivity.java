package com.example.meltingbooks.group.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class GroupUpdateActivity extends AppCompatActivity {

    private EditText groupNameEditText, groupIntroEditText, etCustomTopic;
    private Spinner spinnerMainCategory, spinnerSubCategory;
    private ImageButton groupProfileImage;
    private ImageView imageView;
    private ImageButton updateButton;

    private Uri selectedImageUri = null;
    private boolean isSpinnerInitialized = false;
    private GroupProfileResponse groupInfo;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_update); // 레이아웃 파일 이름

        groupNameEditText = findViewById(R.id.groupNameEditText);
        groupIntroEditText = findViewById(R.id.groupIntroEditText);
        etCustomTopic = findViewById(R.id.etCustomTopic);
        spinnerMainCategory = findViewById(R.id.spinnerMainCategory);
        spinnerSubCategory = findViewById(R.id.spinnerSubCategory);
        groupProfileImage = findViewById(R.id.groupProfileImage);
        imageView = findViewById(R.id.imageView);
        updateButton = findViewById(R.id.updateButton);

        // 상태바 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 그룹 정보 가져오기
        groupInfo = (GroupProfileResponse) getIntent().getSerializableExtra("groupInfo");
        if (groupInfo == null) {
            Toast.makeText(this, "그룹 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 기존 정보 채우기
        fillGroupData();

        // 스피너 초기화
        setupSpinners();

        // 이미지 선택
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

        // 수정 완료 버튼 클릭
        updateButton.setOnClickListener(v -> validateAndUpdateGroup());
    }

    private void fillGroupData() {
        groupNameEditText.setText(groupInfo.getName());
        groupIntroEditText.setText(groupInfo.getDescription());

        String imageUrl = groupInfo.getGroupImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }

        // 카테고리 반영 (대분류 > 세부분류) 예시
        String category = groupInfo.getCategory();
        if (category.contains(">")) {
            String[] parts = category.split(">");
            String main = parts[0].trim();
            String sub = parts[1].trim();

            spinnerMainCategory.setSelection(getSpinnerIndex(spinnerMainCategory, main));
            spinnerSubCategory.setVisibility(View.VISIBLE);
            spinnerSubCategory.setSelection(getSpinnerIndex(spinnerSubCategory, sub));
            etCustomTopic.setVisibility(View.GONE);
        } else {
            spinnerSubCategory.setVisibility(View.GONE);
            etCustomTopic.setVisibility(View.VISIBLE);
            etCustomTopic.setText(category);
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

        spinnerMainCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true;
                    return;
                }

                switch (position) {
                    case 1: showSubCategory(R.array.group_category_genre); break;
                    case 2: showSubCategory(R.array.group_category_purpose); break;
                    case 3: showSubCategory(R.array.group_category_interest); break;
                    default:
                        spinnerSubCategory.setVisibility(View.GONE);
                        etCustomTopic.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
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

        String groupImageUrl = (selectedImageUri != null) ? selectedImageUri.toString() : groupInfo.getGroupImageUrl();

        Group updatedGroup = new Group(name, description, groupImageUrl, category);

        new GroupController(this).updateGroup(groupInfo.getId(), updatedGroup, new GroupController.OnGroupActionCallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(GroupUpdateActivity.this, "그룹 수정 완료", Toast.LENGTH_SHORT).show();
                finish();
                // 그룹 홈으로 이동
                Intent intent = new Intent(GroupUpdateActivity.this, GroupActivity.class);
                startActivity(intent); // 이전 화면으로 돌아감
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(GroupUpdateActivity.this, "수정 실패: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) return i;
        }
        return 0;
    }
}
