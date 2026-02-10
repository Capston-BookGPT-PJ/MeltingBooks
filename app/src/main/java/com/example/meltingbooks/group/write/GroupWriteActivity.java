package com.example.meltingbooks.group.write;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.airbnb.lottie.LottieAnimationView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.group.GroupFeedActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.BuildConfig;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.group.feed.CreatePostRequest;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.feed.GroupFeedResponse;
import com.example.meltingbooks.network.group.feed.GroupReviewResponse;
import com.example.meltingbooks.network.group.feed.UpdatePostRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Retrofit은 Retrofit 요청용
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// OkHttp는 OkHttp 요청용
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
//import okhttp3.ResponseBody;
//import okhttp3.Call;       // 여기서 okhttp3.Call
//import okhttp3.Callback;   // 여기서 okhttp3.Callback

public class GroupWriteActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1001;

    private EditText etInput, etInputTitle;
    private LinearLayout micImageView;
    private LottieAnimationView lottieMicBars;
    private ImageView imageView;
    private ImageButton btnRecord, btnAddFile, btnSummarize, btnUpload;

    private LinearLayout discussionLayout;
    private TextView textHint;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private StorageReference storageReference;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private OkHttpClient client;
    private Request request;
    private String apiKey;
    private Intent intent;

    private boolean isKeyboardVisible = false;

    private Uri selectedImageUri;

    //게시글 수정용 변수
    private boolean isEdit = false;
    private int postId = -1;
    private int groupId;
    private String token;
    //private GroupFeedResponse currentFeed; // 수정할 게시글 데이터
    // 게시글 수정용 변수
    private GroupFeedResponse.Post currentPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_write);

        //위 상단바색상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);  // 아이콘 색상 어둡게!
        }

        // API 키와 네트워크 클라이언트 초기화
        apiKey = BuildConfig.OPENAI_API_KEY;
        client = new OkHttpClient();



        // Firebase Storage 참조 초기화 (필요시 사용)
        storageReference = FirebaseStorage.getInstance().getReference("audio");

        // 뷰 초기화
        micImageView = findViewById(R.id.micON);
        imageView = findViewById(R.id.imageView);
        btnRecord = findViewById(R.id.btnRecord);
        btnAddFile = findViewById(R.id.btnAddFile);
        btnUpload = findViewById(R.id.btnUpload);

        //애니메이션
        lottieMicBars = findViewById(R.id.lottieMicBars);


        discussionLayout = findViewById(R.id.discussionLayout);
        etInputTitle = findViewById(R.id.discussionTitle);
        etInput = findViewById(R.id.discussionContent);



        //게시글 수정 모드
        Intent editIntent = getIntent();
        isEdit = editIntent.getBooleanExtra("isEdit", false);
        postId = editIntent.getIntExtra("postId", -1);
        groupId = editIntent.getIntExtra("groupId", -1);

        if (isEdit && postId != -1) {
            loadPostData(groupId, postId); // 서버에서 기존 리뷰 데이터 가져오기
        }
        Log.d("EditPostResponse", "groupId=" + groupId + ", postId=" + postId + ", isEdit=true");



        // 권한 체크
        checkPermissions();
        setupImagePicker();;

        /// RecognizerIntent 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName()); // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어 설정
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString()); // 기기의 기본 언어로 설정


        // btnRecord 클릭 리스너에서 micON 이미지 뷰를 표시
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSummarize.setVisibility(View.GONE);
                initSpeechRecognizer();
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(GroupWriteActivity.this); // 새 SpeechRecognizer 를 만드는 팩토리 메서드
                speechRecognizer.setRecognitionListener(listener); // 리스너 설정
                // micON 레이아웃 보이기
                if (micImageView != null) {
                    micImageView.setVisibility(View.VISIBLE);
                }

                // Lottie 애니메이션 시작
                if (lottieMicBars != null) {
                    lottieMicBars.setVisibility(View.VISIBLE);
                    lottieMicBars.playAnimation();
                }
                etInput.setHint("");  // hint를 빈 문자열로 설정
                speechRecognizer.startListening(intent); // 듣기 시작
            }
        });

        checkPermissions();


        // --- 게시글 업로드 / 수정 버튼 ---
        btnUpload.setOnClickListener(v -> {

            String title = etInputTitle.getText().toString().trim();
            String content = etInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (content.isEmpty()) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            token = prefs.getString("jwt", null);
            int userId = prefs.getInt("userId", -1);

            if (token == null || userId == -1) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);
            Call<ApiResponse<GroupReviewResponse>> call;

            if (isEdit) {
                UpdatePostRequest updateRequest = new UpdatePostRequest(title, content);
                call = groupApi.updatePost("Bearer " + token, groupId, postId, userId, updateRequest);
            } else {
                CreatePostRequest createRequest = new CreatePostRequest(title, content);
                call = groupApi.createPost("Bearer " + token, groupId, createRequest);
            }

            call.enqueue(new Callback<ApiResponse<GroupReviewResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<GroupReviewResponse>> call, Response<ApiResponse<GroupReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        GroupReviewResponse postData = response.body().getData();
                        int createdPostId = postData.getId();

                        if (selectedImageUri != null) {
                            // FeedWriteActivity 방식처럼 이미지 업로드
                            uploadPostImage(groupApi, token, createdPostId, selectedImageUri, postData);
                        } else {
                            // 이미지 없으면 바로 결과 처리
                            handlePostResult(postData);
                        }

                    } else {
                        Toast.makeText(GroupWriteActivity.this,
                                isEdit ? "게시글 수정 실패" : "게시글 작성 실패", Toast.LENGTH_SHORT).show();
                        Log.e("GroupWriteActivity", "Response code: " + response.code() +
                                ", body: " + new Gson().toJson(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<GroupReviewResponse>> call, Throwable t) {
                    Toast.makeText(GroupWriteActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("GroupWriteActivity", "onFailure", t);
                }
            });
        });
    }


    private void setupEditTextListeners() {
        if (etInput == null) return;

        etInput.setOnClickListener(v -> {
            if (isKeyboardVisible) {
                hideKeyboard();
            } else {
                etInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etInput, InputMethodManager.SHOW_IMPLICIT);
            }
            isKeyboardVisible = !isKeyboardVisible;
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 5) {
                    btnSummarize.setVisibility(View.VISIBLE);
                } else {
                    btnSummarize.setVisibility(View.GONE);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ActivityResultLauncher 설정
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            // ✅ 화면에 바로 표시
                            imageView.setImageURI(selectedUri);
                            // ✅ 서버 업로드용으로 저장
                            selectedImageUri = selectedUri;
                        }
                    }
                });

        btnAddFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }



    private void uploadPostImage(GroupApi groupApi, String token, int postId, Uri imageUri, GroupReviewResponse postData) {
        try {
            // 1️⃣ URI를 캐시 파일로 복사
            File file = new File(getCacheDir(), "temp_group_" + System.currentTimeMillis() + ".jpg");
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) throw new IOException("InputStream is null");
            try (OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
            inputStream.close();

            // 2️⃣ MultipartBody 생성
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("files", file.getName(), requestFile);

            // 3️⃣ 서버 업로드
            Call<ApiResponse<List<String>>> call = groupApi.uploadPostImages("Bearer " + token, groupId, postId, body);
            call.enqueue(new Callback<ApiResponse<List<String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<String> uploadedUrls = response.body().getData();
                        if (uploadedUrls != null) {
                            postData.setImageUrls(uploadedUrls); // ✅ 최신 이미지 리스트 반영
                        }
                        Toast.makeText(GroupWriteActivity.this, "게시글 & 이미지 업로드 완료!", Toast.LENGTH_SHORT).show();
                    }
                    handlePostResult(postData);
                }

                @Override
                public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                    handlePostResult(postData);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "이미지 처리 중 오류 발생 (게시글은 업로드됨)", Toast.LENGTH_SHORT).show();
            handlePostResult(postData);
        }
    }




    // --- 업로드 / 수정 후 처리 공통 ---
        private void handlePostResult(GroupReviewResponse postData) {
            if (isEdit) {
                GroupFeedResponse.Post updatedPost = new GroupFeedResponse.Post();
                updatedPost.setPostType(postData.getPostType());
                updatedPost.setTitle(postData.getTitle());
                updatedPost.setContent(postData.getContent());
                updatedPost.setReviewImageUrls(postData.getImageUrls());
                updatedPost.setReviewId(postData.getId());
                updatedPost.setCreatedAt(postData.getCreatedAt());

                Intent resultIntent = new Intent(GroupWriteActivity.this, GroupFeedActivity.class);
                resultIntent.putExtra("refreshPost", true); // 새로고침 신호 추가
                resultIntent.putExtra("updatedPost", updatedPost);
                resultIntent.putExtra("postId", postId);
                resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); // resultIntent에 플래그 추가
                startActivity(resultIntent);
                finish();

            } else {
                // 생성: GroupFeedActivity에 refresh 신호 보내기
                Intent intent = new Intent(GroupWriteActivity.this, GroupFeedActivity.class);
                intent.putExtra("refreshPost", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
        private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            showSpeechRecognitionUI();
            // 말하기 시작할 준비가되면 호출
        }

        @Override
        public void onBeginningOfSpeech() {
            // 말하기 시작했을 때 호출
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // 입력받는 소리의 크기를 알려줌
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // 말을 시작하고 인식이 된 단어를 buffer에 담음
        }

        @Override
        public void onEndOfSpeech() {
            // 말하기를 중지하면 호출
        }

        @Override
        public void onError(int error) {
            Log.e("SpeechRecognizer", "오류 코드: " + error); // 오류 코드 확인
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 녹음 오류";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 오류";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "권한 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 오류";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트워크 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "음성을 인식하지 못했습니다. 다시 시도해 주세요.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "음성 인식기가 사용 중입니다. 잠시 후 다시 시도해 주세요.";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버 오류 발생";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "입력 시간이 초과되었습니다. 다시 시도해 주세요.";
                    break;
                default:
                    message = "알 수 없는 오류 발생 (코드: " + error + ")";
                    break;
            }

            Log.e("SpeechRecognizer", "에러 메시지: " + message); // 에러 메시지 출력 확인
            Toast.makeText(getApplicationContext(), "에러 발생 : " + message, Toast.LENGTH_SHORT).show();
            hideSpeechRecognitionUI();
            etInput.setHint("독서 후 느낌을 공유해 보세요!");  // 기본 hint로 설정
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                etInput.setText(matches.get(0)); // 인식된 첫 번째 텍스트를 etInput에 설정
                etInput.setSelection(etInput.getText().length()); // 🔥 커서를 맨 뒤로 이동
                // 음성 인식이 완료되면 요약하기 버튼을 보이게 설정
                btnSummarize.setVisibility(View.VISIBLE);
            }
            hideSpeechRecognitionUI();  // 음성 인식 후 UI 숨김
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // 부분 인식 결과를 사용할 수 있을 때 호출
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // 향후 이벤트를 추가하기 위해 예약
        }
    };

    //음성인식 초기화 한 번만
    private void initSpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(listener);
    }



    private void startSpeechRecognition() {
        micImageView.setVisibility(View.VISIBLE);
        etInput.setHint("");
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한 허용 완료", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "권한을 허용해야 음성 인식이 가능합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showSpeechRecognitionUI() {
        // 음성 인식 중 이미지 보이기
        if (micImageView != null) {
            micImageView.setVisibility(View.VISIBLE);  // micON 이미지를 보이도록 설정
        }
        if (lottieMicBars != null) {
            lottieMicBars.setVisibility(View.VISIBLE);
            lottieMicBars.playAnimation();  // Lottie 애니메이션 시작
        }
    }

    // 음성 인식 종료 후 UI 숨김
    private void hideSpeechRecognitionUI() {
        if (micImageView != null) {
            micImageView.setVisibility(View.GONE);  // micON 이미지 뷰 숨김
        }
        if (lottieMicBars != null) {
            lottieMicBars.cancelAnimation();       // Lottie 애니메이션 중지
            lottieMicBars.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }


    // 기존 그룹 게시글 불러오기 (수정용)
    private void loadPostData(int groupId, int postId) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("GroupWrite", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

        // 단일 게시글 조회
        groupApi.getPost("Bearer " + token, groupId, postId, userId)
                .enqueue(new Callback<ApiResponse<GroupReviewResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GroupReviewResponse>> call,
                                           Response<ApiResponse<GroupReviewResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            GroupReviewResponse postData = response.body().getData();
                            bindDataToViews(postData);
                        } else {
                            Toast.makeText(GroupWriteActivity.this, "게시글 로드 실패", Toast.LENGTH_SHORT).show();
                            Log.e("GroupWrite", "게시글 로드 실패: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<GroupReviewResponse>> call, Throwable t) {
                        Toast.makeText(GroupWriteActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                        Log.e("GroupWrite", "게시글 API 실패: " + t.getMessage());
                    }
                });
    }

    // GroupReviewResponse → 뷰 바인딩
    private void bindDataToViews(GroupReviewResponse feed) {
        // 바로 데이터 세팅
        if (etInputTitle != null) etInputTitle.setText(feed.getTitle());
        if (etInput != null) etInput.setText(feed.getContent());
        // ✅ 기존 이미지 표시 (마지막 이미지 사용)
        if (feed.getImageUrls() != null && !feed.getImageUrls().isEmpty()) {
            List<String> images = feed.getImageUrls();
            String latestImage = images.get(images.size() - 1); // 마지막 이미지 선택
            Glide.with(this)
                    .load(latestImage)
                    .into(imageView);

            // ✅ 기존 이미지를 화면에 보여주지만 업로드할 Uri는 아직 null (중요)
            selectedImageUri = null;
        }
    }

}
