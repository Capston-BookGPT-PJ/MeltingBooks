package com.example.meltingbooks.feed;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.meltingbooks.BuildConfig;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookApi;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.book.BookCreateRequest;
import com.example.meltingbooks.network.book.BookResponse;
import com.example.meltingbooks.network.feed.FeedResponse;
import com.example.meltingbooks.network.feed.ReviewRequest;
import com.example.meltingbooks.network.feed.ReviewResponse;
import com.example.meltingbooks.network.feed.ReviewUpdateRequest;
import com.example.meltingbooks.network.growth.GrowthApi;
import com.example.meltingbooks.network.growth.GrowthController;
import com.example.meltingbooks.search.SearchBookAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.airbnb.lottie.LottieAnimationView;

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


public class FeedWriteActivity extends AppCompatActivity {
    private String apiKey;  // apiKey는 이제 onCreate()에서 초기화
    private Request request;  // request는 callAPI() 메서드 내에서 생성
    private static final int REQUEST_PERMISSION_CODE = 1001;
    private ImageButton btnRecord, btnAddFile, btnUpload;
    private EditText etInput;
    private Button btnSummarize;
    private boolean isKeyboardVisible = false;
    private ImageView imageView;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private SpeechRecognizer speechRecognizer;
    final int PERMISSION = 1;
    private Intent intent;
    private Intent speechRecognizerIntent;

    private LinearLayout micImageView;
    private LinearLayout summarizingImageView;

    // ChatGPT API client setup
    private OkHttpClient client;
    private Uri selectedImageUri;

    // 해시태그 관련 변수
    private EditText etHashtag;
    private ImageButton btnHashtag;
    private LinearLayout hashtagLayout;

    // 책 관련 변수
    private ImageButton btnBook;
    private View feedBookSearch; //책 검색 및 선택 레이아웃(숨김.보임)
    //책 검색
    private LinearLayout bookSearchInfoContainer;
    private EditText etBookTitle;
    private ImageView searchBook;
    // 🔹 책 관련 변수 매핑
    private SearchBookAdapter bookAdapter;
    private List<Book> filteredBookList;
    private BookController bookController;
    private RecyclerView rvSearchResults; //책 검색
    private LinearLayout bookInfoSelected; //선택한 책 표시
    private ImageView bookCover; //책 이미지
    private TextView bookInfoTitle, bookInfoAuthor, bookInfoPublisher, bookInfoCategory; //제목 저자 출판사 카테고리

    // 선택한 책 bookId와 별점(전달용)
    private Integer selectedBookId = -1;
    private int selectedBookRating = 0;
    private boolean isBookSearchInitialized = false;

    // 별점용 LinearLayout
    private LinearLayout ratingBar;
    private int numStars = 5; // 별 개수

    //게시글 수정용 변수
    private boolean isEdit = false;
    private int postId = -1;
    private ReviewResponse currentFeed; // 수정할 게시글 데이터

    private GrowthController growthController;
    private LottieAnimationView lottieMicBars, lottieChatGPTBars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        //위 상단바색상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);  // 아이콘 색상 어둡게!
        }

        // ChatGPT API 키 초기화
        apiKey = BuildConfig.OPENAI_API_KEY;

        ///안드로이드 6.0버전 이상인지 체크해서 퍼미션
        if(Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        //게시글 수정 모드
        Intent feedIntent = getIntent();
        isEdit = feedIntent.getBooleanExtra("isEdit", false);
        postId = feedIntent.getIntExtra("postId", -1);

        if (isEdit && postId != -1) {
            loadReviewData(postId); // 서버에서 기존 리뷰 데이터 가져오기
        }


        // Firebase Storage 초기화
        storageReference = FirebaseStorage.getInstance().getReference("audio");

        // UI 컴포넌트 초기화
        btnUpload = findViewById(R.id.btnUpload);

        // 하단 버튼(4가지)
        btnRecord = findViewById(R.id.btnRecord); // 음성 녹음 버튼
        btnAddFile = findViewById(R.id.btnAddFile); // 이미지 첨부 버튼
        btnHashtag = findViewById(R.id.btnHashtag); // 해시태그 입력 버튼
        btnBook = findViewById(R.id.btnBook); // 책 검색 버튼

        // content
        imageView = findViewById(R.id.imageView); // 청부된 이미지
        etInput = findViewById(R.id.etInput); // 내용

        // 추가 표시 버튼 및 이미지
        btnSummarize = findViewById(R.id.btnSummarize); // 요약하기 버튼
        micImageView = findViewById(R.id.micON); // 음성 녹음 중 이미지
        summarizingImageView = findViewById(R.id.summarizing); // 요약 중 이미지

        //애니메이션
        lottieMicBars = findViewById(R.id.lottieMicBars);
        lottieChatGPTBars = findViewById(R.id.lottieChatGPTBars);


        // 책 관련
        ratingBar = findViewById(R.id.ratingContainer); // 별점바 초기화
        feedBookSearch = findViewById(R.id.feedBookSearch); // 책 정보 레이아웃 초기화

        // 책 검색 UI 초기화 후 한번만 별점 세팅
        if (!isBookSearchInitialized) {
            setupStars(selectedBookRating); // 처음 UI 표시, 선택된 별점 없으면 0
            isBookSearchInitialized = true;
        }
        // 해시태그 관련
        etHashtag = findViewById(R.id.etHashtag);
        hashtagLayout = findViewById(R.id.hashtagLayout);

        // Initialize OkHttpClient for ChatGPT API
        client = new OkHttpClient();

        // 음성 인식 권한 확인
        checkPermissions();

        // editText 기능
        etInput.setOnClickListener(v -> {
            if (isKeyboardVisible) {
                // 키보드가 보이면 숨김
                hideKeyboard();
            } else {
                // 키보드가 숨겨져 있으면 보임
                etInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etInput, InputMethodManager.SHOW_IMPLICIT);
            }
            isKeyboardVisible = !isKeyboardVisible; // 키보드 상태 반전
        });

        // 첨부 파일 추가
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData(); // ✅ 기존 이미지 교체됨
                        imageView.setImageURI(selectedImageUri);       // ✅ 화면에서도 교체됨
                    }
                });

        btnAddFile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imagePickerLauncher.launch(intent);
        });

        checkPermissions();

        // EditText에 입력 감지하는 TextWatcher 추가
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력된 텍스트 길이가 5자 이상이면 버튼 활성화
                if (s.length() >= 5) {
                    btnSummarize.setVisibility(View.VISIBLE);  // 입력 5자 이상 → 버튼 보이기
                } else {
                    btnSummarize.setVisibility(View.GONE);  // 5자 미만 → 버튼 숨기기
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 요약하기 버튼 클릭 리스너
        btnSummarize.setOnClickListener(v -> {
            //요약하기 버튼 숨기기
            btnSummarize.setVisibility(View.GONE);
            // 요약 중 이미지를 보이게
            summarizingImageView.setVisibility(View.VISIBLE);
            // Lottie 애니메이션 초기화 후 재생
            if (lottieChatGPTBars != null) {
                lottieChatGPTBars.setVisibility(View.VISIBLE);
                lottieChatGPTBars.cancelAnimation(); // 이전 상태 초기화
                lottieChatGPTBars.setProgress(0f);   // 시작 위치 초기화
                lottieChatGPTBars.playAnimation();   // 재생
            }

            // 텍스트를 요약하는 로직
            String inputText = etInput.getText().toString();
            callAPI(inputText);  // ChatGPT API 호출
        });

        /// RecognizerIntent 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName()); // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR"); // 언어 설정
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString()); // 기기의 기본 언어로 설정

        // btnRecord 클릭 리스너에서 micON 이미지 뷰를 표시 + Lottie 재생
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSummarize.setVisibility(View.GONE);
                initSpeechRecognizer();
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(FeedWriteActivity.this);
                speechRecognizer.setRecognitionListener(listener);

                // micON 레이아웃 보이기
                if (micImageView != null) {
                    micImageView.setVisibility(View.VISIBLE);
                }

                // Lottie 애니메이션 시작
                if (lottieMicBars != null) {
                    lottieMicBars.setVisibility(View.VISIBLE);
                    lottieMicBars.playAnimation();
                }

                etInput.setHint("");
                speechRecognizer.startListening(intent);
            }
        });
        checkPermissions();


        // 1. 책 버튼 클릭시 책 검색 및 별점 레이아웃 보이기
        btnBook.setOnClickListener(v -> {
            // 책 검색 레이아웃을 보이거나 숨기는 로직
            if (feedBookSearch.getVisibility() == View.GONE) {
                feedBookSearch.setVisibility(View.VISIBLE);
                // ✅ 책 선택 전이므로 숨겨둠
                ratingBar.setVisibility(View.GONE);
                // 뷰 초기화 및 리스너 등록 메서드 호출
                initializeBookSearchViews();
            } else {
                feedBookSearch.setVisibility(View.GONE);
                ratingBar.setVisibility(View.GONE);
            }
        });


        // 2. 해시태그 버튼 클릭 시 레이아웃 보이기
        btnHashtag.setOnClickListener(v -> {
            hashtagLayout.setVisibility(View.VISIBLE);
            etHashtag.requestFocus();

            // 키보드 자동 오픈
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etHashtag, InputMethodManager.SHOW_IMPLICIT);

            // ✅ 기존 텍스트 유지 + 뒤에 공백과 '#' 추가
            String currentText = etHashtag.getText().toString();
            if (!currentText.endsWith(" ") && !currentText.isEmpty()) {
                currentText += " ";
            }
            etHashtag.setText(currentText + "#");
            etHashtag.setSelection(etHashtag.getText().length()); // 커서 맨 뒤로
        });

        // 해시태그 '#' 추가
        etHashtag.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                String currentText = etHashtag.getText().toString();
                // 이미 끝에 공백 없으면 공백 추가
                if (!currentText.endsWith(" ")) {
                    currentText += " ";
                }
                // 새 # 추가
                etHashtag.setText(currentText + "#");
                // 커서를 마지막으로 이동
                etHashtag.setSelection(etHashtag.getText().length());
                return true; // 이벤트 소비
            }
            return false;
        });

        btnUpload.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            String token = prefs.getString("jwt", null);
            int userId = prefs.getInt("userId", -1);

            if (token == null || userId == -1) {
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String content = etInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 해시태그 배열에 값 넣기
            List<String> hashtags = new ArrayList<>();
            String hashtagInput = etHashtag.getText().toString().trim();
            if (!hashtagInput.isEmpty()) {
                String[] parts = hashtagInput.split("#"); //# 기준 구분
                for (String part : parts) {
                    part = part.trim();
                    if (!part.isEmpty()) {
                        // 연속된 # 제거 후 하나만 붙이기
                        part = part.replaceAll("^#+", ""); // 시작 부분의 # 제거
                        hashtags.add("#" + part); // 다시 # 붙이기
                    }
                }

            }

            ApiService apiService = ApiClient.getClient(token).create(ApiService.class);


            //게시슬 수정 기능 추가
            Call<ApiResponse<ReviewResponse>> call; // 밖에서 선언

            if (isEdit) {
                // 수정 모드: 모든 필드 포함
                Integer safeBookId = (selectedBookId != null && selectedBookId != -1) ? selectedBookId : null;
                ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
                        content,
                        safeBookId, // 선택한 책 ID
                        selectedBookRating != 0 ? selectedBookRating : null,  // 별점
                        hashtags.isEmpty() ? null : hashtags
                );

                call = apiService.updateReview(
                        "Bearer " + token,
                        postId,
                        userId,  // 쿼리 파라미터
                        updateRequest
                );


            } else {
                Integer safeBookId = (selectedBookId != null && selectedBookId != -1) ? selectedBookId : null;
                // 생성 모드: bookId, rating, hashtags 조건부 전달
                ReviewRequest createRequest = new ReviewRequest(
                        safeBookId,
                        content,
                        selectedBookRating != 0 ? selectedBookRating : null,
                        hashtags.isEmpty() ? null : hashtags
                );

                call = apiService.createReview(
                        "Bearer " + token,
                        userId,
                        createRequest
                );
            }

            call.enqueue(new Callback<ApiResponse<ReviewResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ReviewResponse>> call, Response<ApiResponse<ReviewResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        ReviewResponse review = response.body().getData();
                        int reviewId = review.getReviewId();

                        if (selectedImageUri != null) {
                            uploadReviewImage(apiService, token, reviewId, selectedImageUri, review);
                        } else {
                            Toast.makeText(FeedWriteActivity.this, isEdit ? "게시글 수정 완료!" : "리뷰 작성 완료!", Toast.LENGTH_SHORT).show();

                            //리뷰 저장 성공 후 경험치 지급
                            GrowthApi growthApi = ApiClient.getClient(token).create(GrowthApi.class);
                            growthController = new GrowthController(growthApi, token);

                            giveCompletionExp();

                            if (isEdit) {
                                // 수정: 런처로 갱신
                                FeedResponse updatedFeed = new FeedResponse();
                                updatedFeed.setHashtags(review.getHashtags());
                                updatedFeed.setBookId(review.getBookId());
                                updatedFeed.setContent(review.getContent());
                                updatedFeed.setReviewImageUrls(review.getReviewImageUrls());
                                updatedFeed.setUserId(review.getUserId());
                                updatedFeed.setReviewId(review.getReviewId());
                                updatedFeed.setRating(review.getRating());
                                updatedFeed.setCreatedAt(review.getCreatedAt());

                                Intent resultIntent = new Intent(FeedWriteActivity.this, FeedActivity.class);
                                resultIntent.putExtra("refreshFeed", true); // 새로고침 신호 추가
                                resultIntent.putExtra("updatedFeed", updatedFeed);
                                resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); // resultIntent에 플래그 추가
                                startActivity(resultIntent);
                                finish();
                            } else {
                                // 생성: FeedActivity에 refresh 신호 보내기
                                Intent intent = new Intent(FeedWriteActivity.this, FeedActivity.class);
                                intent.putExtra("refreshFeed", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }

                        }
                    } else {
                        // 실패 처리
                        Toast.makeText(FeedWriteActivity.this, isEdit ? "게시글 수정 실패" : "리뷰 작성 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ReviewResponse>> call, Throwable t) {
                    Toast.makeText(FeedWriteActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private void setupStars(int currentRating) {
        ratingBar.removeAllViews(); // 기존 별 초기화
        for (int i = 1; i <= numStars; i++) {
            final int starIndex = i;
            ImageView star = new ImageView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(40),
                    dpToPx(40)
            );
            params.setMargins(12, 0, 12, 0); // 별 간 간격
            star.setLayoutParams(params);
            star.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // 현재 선택한 별점 기준으로 이미지 설정
            if (i <= currentRating) {
                star.setImageResource(R.drawable.star_filled);
            } else {
                star.setImageResource(R.drawable.star_empty);
            }

            // ⭐ 클릭 리스너
            star.setOnClickListener(v -> {
                selectedBookRating = starIndex; // 점수 저장
                updateStars(selectedBookRating); // UI 갱신
            });

            ratingBar.addView(star);
        }
    }

    private void updateStars(int currentRating) {
        int childCount = ratingBar.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView star = (ImageView) ratingBar.getChildAt(i);
            if (i < currentRating) {
                star.setImageResource(R.drawable.star_filled);
            } else {
                star.setImageResource(R.drawable.star_empty);
            }
        }
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
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

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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


    // ChatGPT API 호출
    private void callAPI(String question) {
        // 요약 중 이미지를 보이게
        summarizingImageView.setVisibility(View.VISIBLE);
        // Lottie 애니메이션 재생
        lottieChatGPTBars.playAnimation();

        JSONObject object = new JSONObject();
        try {
            object.put("model", "gpt-3.5-turbo");
            JSONArray messagesArray = new JSONArray();

            // 시스템 역할 추가
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant that summarizes text.");
            messagesArray.put(systemMessage);

            // 사용자 입력 추가
            JSONObject messageObj = new JSONObject();
            messageObj.put("role", "user");
            messageObj.put("content",
                    "다음 텍스트를 요약하되:\\n\n" +
                            "1. 1인칭 느낌 유지, '사용자'나 '읽은 사람' 등의 단어 금지\\n\n" +
                            "2. 핵심 내용과 주인공 경험 중심 유지\\n\n" +
                            "3. 추가 추론 금지, 불필요한 재서술 최소화\\n\n" +
                            "4. 의미 보존 전제하에 문장 구조 일부 재구성 가능\\n\n" +
                            "5. 압축 기준 (글자 수 기준, ±3자 오차만 허용):\\n\n" +
                            "   - 1~2문장 원문: 40~50%로 압축\\n\n" +
                            "   - 3~5문장 원문: 30~40%로 압축\\n\n" +
                            "   - 6문장 이상 원문: 20~30%로 압축\\n\n" +
                            "   ※ 불필요한 부사, 수식어, 반복 표현은 삭제하고 문장을 합쳐 압축할 것\\n\n" +
                            "6. 요약 단계 (반드시 아래 순서로 수행할 것):\\n\n" +
                            "   1단계. 원문에서 주제, 핵심 사건, 주인공의 경험 중심 의미만 추출\\n\n" +
                            "   2단계. 불필요한 부사, 수식어, 반복 표현 제거\\n\n" +
                            "   3단계. 문장을 합치거나 재구성해 자연스럽게 줄이기\\n\n" +
                            "   4단계. 지정된 압축률에 따라 목표 글자 수 계산\\n\n" +
                            "   5단계. 요약문을 작성한 뒤 글자 수를 직접 계산하여 아래 형식으로 중간 결과를 **내부적으로만 계산**\\n\n" +
                            "\\n\n" +
                            "[원문 글자 수]: XXX자\\n\n" +
                            "[요약 목표 글자 수]: 약 XX자 (XX%)\\n\n" +
                            "[실제 요약문]: (여기에 요약문)\\n\n" +
                            "[실제 요약문 글자 수]: XXX자\\n\n" +
                            "\\n\n" +
                            "7단계. **최종 출력 단계**:\\n\n" +
                            "   위 형식(중간 계산용 표기)은 모두 무시하고, 오직 요약문 본문 내용만 출력할 것.\\n\n" +
                            "   '(여기에 요약문)', '(실제 요약문)' 등의 예시 문구는 절대 포함하지 않는다.\\n\n" +
                            "   [원문 글자 수], [요약 목표 글자 수] 등 라벨과 숫자 정보도 포함하지 않는다.\\n\n" +
                            "\\n\n" +
                            "※ 최종 출력 결과는 완성된 요약문 문장 1개만으로 구성된다.\\n\n" +
                            "⚠️ 출력 시 주의: 모든 안내문, 괄호, 라벨, 숫자, 기호를 제거하고 오직 요약문 본문만 출력한다.\\n\n" +
                            "   예시) 『흩어진 마음』은 ADHD의 원인을 아동기의 정서적 상처와 환경적 요인으로 설명하며, 관계와 환경의 영향을 강조한다.\\n\n" +
                            "\\n\n" +
                            "---\\n\n" +
                            "예시:\\n\n" +
                            "\\n\n" +
                            "   1~2문장 원문:\\n\n" +
                            "   원문 (36자): 주인공이 새로운 도전을 통해 성장하는 이야기를 읽고 흥미로웠다.\\n\n" +
                            "   요약 (22자, 약 60% 압축): 주인공의 도전을 통한 성장 이야기가 흥미롭게 느껴졌다.\\n\n" +
                            "\\n\n" +
                            "   3~5문장 원문:\\n\n" +
                            "   원문 (104자): 주인공이 어려운 상황에서도 포기하지 않고 문제를 해결하려고 노력했다. 그 과정을 보면서 용기를 얻었다. 마지막에 주인공이 큰 성취를 이루는 장면은 매우 인상 깊었다. 전반적인 스토리 전개는 예측 가능했지만, 주제 전달이 명확했다.\\n\n" +
                            "   요약 (70자, 약 67% 압축): 주인공이 어려움을 극복하고 문제를 해결하는 과정과 마지막 성취 장면이 인상적이며, 메시지가 분명하게 전달된다.\\n\n" +
                            "\\n\n" +
                            "   3~5문장 원문 (실제 책 감상평 예시):\\n\n" +
                            "   원문 (120자): 『흩어진 마음』은 ADHD와 관련된 심리적, 사회적 요인을 다루고 있습니다. 가보르 마테는 아동기의 정서적 상처가 성인기의 정신 건강에 미치는 영향을 강조합니다. 이 책을 통해 ADHD를 단순한 뇌의 문제로만 보지 않고, 환경과 관계의 영향을 고려해야 한다는 점을 깨달았습니다. 저자의 주장은 때때로 논란의 여지가 있지만, 새로운 시각을 제공해 주었습니다.\\n\n" +
                            "   요약 (84자): 『흩어진 마음』은 ADHD의 원인을 아동기의 정서적 상처와 환경적 요인으로 설명한다. 저자는 ADHD를 단순한 뇌 문제로만 보지 않고, 관계와 환경의 영향을 강조하며 새로운 시각을 제시한다.\\n\n" +
                            "\\n\n" +
                            "   6문장 이상 원문:\\n\n" +
                            "   원문 (150자): 이 책은 인간의 행복에 대해 깊이 고민하게 만든 작품이에요. 특히 주인공이 행복을 찾기 위해 겪는 다양한 갈등과 모험이 정말 공감됐어요. 마지막에 주인공이 자신의 행복이 다른 사람들과의 관계 속에서 발견된다는 걸 깨닫는 장면은 정말 인상 깊었어요. 저도 이 책을 읽고 나서 제 주변 사람들과의 관계를 다시 생각해 보게 됐어요. 전반적인 스토리 전개가 예측 가능했지만, 주제 전달은 명확했어요. 결말이 따뜻하게 마무리되어 만족스러웠습니다.\\n\n" +
                            "   요약 (94자): 행복의 본질을 탐구하며, 주인공이 타인과의 관계 속에서 행복을 찾는 과정을 다룬 책. 읽고 나서 주변 사람들과의 관계를 돌아보게 되었고, 메시지가 분명하며 결말이 따뜻하게 마무리된다.\\n\n" +
                            "\\n\n" +
                            "---\\n\n" +
                            "다음 텍스트도 같은 방식으로 요약:\\n"

                            + question);  // 명확한 요청 추가
            messagesArray.put(messageObj);
            object.put("messages", messagesArray);
            object.put("temperature", 0.7); // 다양성을 조절하는 옵션
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(object.toString(), MediaType.get("application/json; charset=utf-8"));
        request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)  // apiKey를 사용
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() { // 반드시 okhttp3.Callback 사용
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                // 요청 실패 처리
                runOnUiThread(() -> {
                    summarizingImageView.setVisibility(View.GONE);  // 요약 중 이미지 숨기기

                    // 🔹 Lottie 애니메이션 중지
                    if (lottieChatGPTBars != null) {
                        lottieChatGPTBars.cancelAnimation();
                        lottieChatGPTBars.setVisibility(View.GONE);
                    }
                    Toast.makeText(FeedWriteActivity.this, "요약 실패", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", responseBody);  // 응답 로깅


                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        String summarizedText = choices.getJSONObject(0).getJSONObject("message").getString("content");

                        runOnUiThread(() -> {
                            etInput.setText(summarizedText);
                            summarizingImageView.setVisibility(View.GONE);
                            // 🔹 Lottie 애니메이션 중지
                            if (lottieChatGPTBars != null) {
                                lottieChatGPTBars.cancelAnimation();
                                lottieChatGPTBars.setVisibility(View.GONE);
                            }
                            btnSummarize.setVisibility(View.VISIBLE);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        summarizingImageView.setVisibility(View.GONE);
                        // 🔹 Lottie 애니메이션 중지
                        if (lottieChatGPTBars != null) {
                            lottieChatGPTBars.cancelAnimation();
                            lottieChatGPTBars.setVisibility(View.GONE);
                        }
                        Toast.makeText(FeedWriteActivity.this, "API 호출 오류: " + responseBody, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    private void uploadReviewImage(ApiService apiService, String token, int reviewId, Uri imageUri, ReviewResponse review) {
        try {
            File file = new File(getCacheDir(), "temp_review.jpg");
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
            MultipartBody.Part body = MultipartBody.Part.createFormData("files", file.getName(), requestFile);

            Call<ApiResponse<List<String>>> call = apiService.uploadReviewImage("Bearer " + token, reviewId, body);
            call.enqueue(new Callback<ApiResponse<List<String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<String> uploadedImageUrls = response.body().getData();
                        if (uploadedImageUrls != null) {
                            review.setReviewImageUrls(uploadedImageUrls);
                        }

                        // ✅ ReviewResponse → FeedResponse 변환
                        FeedResponse updatedFeed = new FeedResponse();
                        updatedFeed.setHashtags(review.getHashtags());
                        updatedFeed.setBookId(review.getBookId());
                        updatedFeed.setContent(review.getContent());
                        updatedFeed.setReviewImageUrls(review.getReviewImageUrls());
                        updatedFeed.setUserId(review.getUserId());
                        updatedFeed.setReviewId(review.getReviewId());
                        updatedFeed.setRating(review.getRating());
                        updatedFeed.setCreatedAt(review.getCreatedAt());

                        // FeedActivity에 전달
                        Intent resultIntent = new Intent(FeedWriteActivity.this, FeedActivity.class);
                        resultIntent.putExtra("refreshFeed", true);
                        resultIntent.putExtra("updatedFeed", updatedFeed);
                        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(resultIntent);
                        finish();

                    } else {
                        Toast.makeText(FeedWriteActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                    Toast.makeText(FeedWriteActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //책 검색과 선택 및 별점
    private void initializeBookSearchViews() {
        // 1. 최상위 뷰인 feedBookSearch를 찾습니다.
        feedBookSearch = findViewById(R.id.feedBookSearch);

        // 2. feedBookSearch가 null이 아닌지 확인합니다.
        if (feedBookSearch != null) {
            // 3. 이제 안전하게 하위 뷰들을 찾습니다.
            bookSearchInfoContainer = feedBookSearch.findViewById(R.id.bookSearchInfoContainer);
            etBookTitle = feedBookSearch.findViewById(R.id.etBookTitle);
            searchBook = feedBookSearch.findViewById(R.id.searchBook);
            rvSearchResults = feedBookSearch.findViewById(R.id.rvSearchResults);
            bookInfoSelected = feedBookSearch.findViewById(R.id.bookInfoSelected);



            // bookInfoSelected도 null 체크를 하는 것이 안전합니다.
            if (bookInfoSelected != null) {
                bookInfoTitle = bookInfoSelected.findViewById(R.id.bookInfoTitle);
                bookInfoAuthor = bookInfoSelected.findViewById(R.id.bookInfoAuthor);
                bookInfoPublisher = bookInfoSelected.findViewById(R.id.bookInfoPublisher);
                bookCover = bookInfoSelected.findViewById(R.id.bookCover);
                bookInfoCategory = bookInfoSelected.findViewById(R.id.bookInfoCategory);
            }

            // RecyclerView 및 어댑터 초기화 로직
            bookController = new BookController(this);
            filteredBookList = new ArrayList<>();
            bookAdapter = new SearchBookAdapter(this, filteredBookList);
            rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
            rvSearchResults.setAdapter(bookAdapter);

            // 초기에는 안 보이게 숨김
            rvSearchResults.setVisibility(View.GONE);
            bookInfoSelected.setVisibility(View.GONE);



            // 책 검색 버튼 클릭 리스너
            searchBook.setOnClickListener(v -> {
                if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.GONE);
                String query = etBookTitle.getText().toString().trim();
                rvSearchResults.setVisibility(View.VISIBLE);
                // ✅ 검색 중이므로 별점 숨김
                ratingBar.setVisibility(View.GONE);

                if (!query.isEmpty()) {
                    bookController.searchBooks(query, new Callback<BookResponse>() {
                        @Override
                        public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {

                            List<Book> books = response.body().getData();
                            filteredBookList.clear();
                            if (response.isSuccessful() && response.body() != null) {
                                filteredBookList.addAll(books);
                            }
                            bookAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<BookResponse> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(FeedWriteActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            // RecyclerView 아이템 클릭
            bookAdapter.setOnItemClickListener(book -> {
                bookInfoTitle.setText(book.getTitle());
                bookInfoAuthor.setText(book.getAuthor());
                bookInfoPublisher.setText(book.getPublisher());
                bookInfoCategory.setText(book.getCategoryName());
                Glide.with(this).load(book.getCover()).into(bookCover);

                rvSearchResults.setVisibility(View.GONE);
                bookInfoSelected.setVisibility(View.VISIBLE);
                // ✅ 책 선택했으니 이제 별점 보여도 됨!
                ratingBar.setVisibility(View.VISIBLE);

                // 2️⃣ 서버에 Book 생성 요청
                createBookOnServer(book);
            });
        }
    }
    // 책 생성
    private void createBookOnServer(Book searchResult) {
        String token = getSharedPreferences("auth", MODE_PRIVATE).getString("jwt", null);
        if (token == null) return;

        BookCreateRequest request = new BookCreateRequest(
                searchResult.getTitle(),
                searchResult.getAuthor(),
                searchResult.getPublisher(),
                searchResult.getPubDate(),
                searchResult.getIsbn(),
                searchResult.getIsbn13(),
                searchResult.getCover(),
                searchResult.getLink(),
                searchResult.getCategoryName(),
                searchResult.getItemPage()
        );


        //Book 객체에 저장(bookId 저장->selectedBookId)
        // BookApi 사용
        BookApi bookApi = ApiClient.getClient(token).create(BookApi.class);
        bookApi.createBook("Bearer " + token, request).enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedBookId = response.body().getBookId();
                } else {
                    Toast.makeText(FeedWriteActivity.this, "책 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Toast.makeText(FeedWriteActivity.this, "서버 통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //게시글 수정: 기존 게시글 불러오기
    private void loadReviewData(int postId) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("Feed", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        apiService.getReviewDetail("Bearer " + token, postId, userId)
                .enqueue(new Callback<ApiResponse<FeedResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FeedResponse>> call, Response<ApiResponse<FeedResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            FeedResponse feed = response.body().getData();
                            bindDataToViews(feed); // bindDataToViews도 FeedResponse 타입으로 변경
                        } else {
                            Toast.makeText(FeedWriteActivity.this, "게시글 로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FeedResponse>> call, Throwable t) {
                        Toast.makeText(FeedWriteActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bindDataToViews(FeedResponse feed) {
        etInput.setText(feed.getContent());
        etInput.setSelection(etInput.getText().length());

        if (feed.getHashtags() != null) {
            etHashtag.setText(String.join(" ", feed.getHashtags()));
        }

        // 선택한 책 ID 등
        selectedBookId = feed.getBookId();

        // ✅ 기존 이미지 표시 (마지막 이미지 사용)
        if (feed.getReviewImageUrls() != null && !feed.getReviewImageUrls().isEmpty()) {
            List<String> images = feed.getReviewImageUrls();
            String latestImage = images.get(images.size() - 1); // 마지막 이미지 선택
            Glide.with(this)
                    .load(latestImage)
                    .into(imageView);

            // ✅ 기존 이미지를 화면에 보여주지만 업로드할 Uri는 아직 null (중요)
            selectedImageUri = null;
        }
    }

    private void giveCompletionExp() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        growthController.giveExp(userId, "WRITE_REVIEW", new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    Toast.makeText(FeedWriteActivity.this, "경험치 지급 완료: " + response.body(), Toast.LENGTH_SHORT).show();
                    Log.d("EXP_DEBUG", "userId=" + userId + ", eventType=" + "WRITE_REVIEW"+"경험치 지급 완료");
                } else {
                    Toast.makeText(FeedWriteActivity.this, "경험치 지급 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

                Toast.makeText(FeedWriteActivity.this, "서버 통신 실패", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
