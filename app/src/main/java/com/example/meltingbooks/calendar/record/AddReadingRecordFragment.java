package com.example.meltingbooks.calendar.record;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.BookListHelper.BookItem;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookApi;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.book.BookCreateRequest;
import com.example.meltingbooks.network.book.BookResponse;
import com.example.meltingbooks.network.growth.GrowthApi;
import com.example.meltingbooks.network.growth.GrowthController;
import com.example.meltingbooks.network.log.LogApi;
import com.example.meltingbooks.network.log.LogController;
import com.example.meltingbooks.network.log.ReadingLogRequest;
import com.example.meltingbooks.network.log.ReadingLogResponse;
import com.example.meltingbooks.search.SearchBookAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AddReadingRecordFragment extends Fragment {

    private LocalDate selectedDate = LocalDate.now();
    private View rootView;
    private View feedBookSearch; //책 검색 및 선택 레이아웃(숨김.보임)
    // 🔹 책 관련 변수 매핑
    private SearchBookAdapter bookAdapter;
    private List<Book> filteredBookList;
    private BookController bookController;
    private RecyclerView rvSearchResults; //책 검색
    private LinearLayout bookInfoSelected; //선택한 책 표시
    private ImageView bookCover, searchBook; //책 이미지
    private TextView bookInfoTitle, bookInfoAuthor, bookInfoPublisher, bookInfoCategory; //제목 저자 출판사 카테고리

    private EditText editPage, editHours, editMinutes, etBookTitle;
    private TextView btnSave, btnDelete;

    // 선택한 책 bookId
    private int selectedBookId = -1;
    private boolean isBookSearchInitialized = false;
    private int currentLogId = -1; // 수정/삭제 시 필요

    //읽은 시간대 집계
    private String currentReadAtString = "";
    private EditText editStartHour;
    private EditText editStartMinute;


    private String token;
    private int userId;

    private LogController logController;

    private GrowthController growthController;

    public AddReadingRecordFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_reading_record, container, false);

        editPage = rootView.findViewById(R.id.edit_page);
        editMinutes = rootView.findViewById(R.id.edit_minute);
        editHours= rootView.findViewById(R.id.edit_hour);
        btnSave = rootView.findViewById(R.id.btn_save_log);
        btnDelete = rootView.findViewById(R.id.btn_delete_log);

        // --- AM/PM Spinner 초기화 ---
        Spinner spinnerAmPm = rootView.findViewById(R.id.spinner_am_pm);

        // 커스텀 ArrayAdapter로 글자색 적용
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.am_pm)
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.BLACK); // 선택된 아이템 글자색
                view.setPadding(0, 4, 0, 4); // 좌우 공백 0, 상하 4dp
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.BLACK); // 드롭다운 아이템 글자색
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAmPm.setAdapter(adapter);
        spinnerAmPm.setSelection(0); // 기본값: 오전

        spinnerAmPm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateReadAtTimeFromInput(); // 시간 재계산
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        editStartHour = rootView.findViewById(R.id.edit_start_hour);
        editStartMinute = rootView.findViewById(R.id.edit_start_minute);

        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        LogApi logApi = ApiClient.getClient(token).create(LogApi.class);
        logController = new LogController(logApi);

        GrowthApi growthApi = ApiClient.getClient(token).create(GrowthApi.class);
        growthController = new GrowthController(growthApi, token);


        // 저장 버튼
        btnSave.setOnClickListener(v -> saveOrUpdateLog());

        // 삭제 버튼
        btnDelete.setOnClickListener(v -> deleteLog());

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // 초기 날짜 표시
        updateWeekDates();

        // 이전 주 버튼
        ImageView btnPrev = view.findViewById(R.id.btn_prev_week);
        btnPrev.setOnClickListener(v -> {
            selectedDate = selectedDate.minusWeeks(1);
            updateWeekDates();
            clearInputs();
            loadLogForSelectedDate(); // 이전 주 첫 진입 시 자동 로드
        });

        // 다음 주 버튼
        ImageView btnNext = view.findViewById(R.id.btn_next_week);
        btnNext.setOnClickListener(v -> {
            selectedDate = selectedDate.plusWeeks(1);
            updateWeekDates();
            clearInputs();
           // loadLogForSelectedDate(); // 다음 주 첫 진입 시 자동 로드
        });

        initializeBookSearchViews();

        CheckBox checkboxFinished = view.findViewById(R.id.checkboxFinished);
        boolean isFinished = checkboxFinished.isChecked(); // true면 완독, false면 미완독

    }

    private void updateWeekDates() {
        LinearLayout container = rootView.findViewById(R.id.week_date_container);
        container.removeAllViews();

        LocalDate sunday = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() % 7);

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            // 1️⃣ 셀: weight로 폭 분배, padding 제거
            FrameLayout cell = new FrameLayout(getContext());
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            cell.setLayoutParams(cellParams);

            // 2️⃣ 고정 크기 TextView (원)
            TextView textView = new TextView(getContext());
            int circleSizeInDp = 35;
            int circleSizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, circleSizeInDp, getResources().getDisplayMetrics()
            );

            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                    circleSizeInPx, circleSizeInPx
            );
            textParams.gravity = Gravity.CENTER;
            textView.setLayoutParams(textParams);

            textView.setText(String.valueOf(date.getDayOfMonth()));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setGravity(Gravity.CENTER);

            // 선택된 날짜
            if (date.equals(selectedDate)) {
                textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_date));
                textView.setTextColor(Color.WHITE);
            } else {
                textView.setTextColor(Color.BLACK);
            }

            textView.setOnClickListener(v -> {
                selectedDate = date;
                updateWeekDates();
                clearInputs();
                loadLogForSelectedDate();
            });

            cell.addView(textView);
            container.addView(cell);
        }

        loadLogForSelectedDate();
    }

    //책 검색과 선택 및 별점
    private void initializeBookSearchViews() {
        // 1. Fragment의 rootView에서 뷰 찾기
        feedBookSearch = rootView.findViewById(R.id.feedBookSearch);


        // 2. feedBookSearch가 null이 아닌지 확인합니다.
        if (feedBookSearch != null) {
            // 3. 이제 안전하게 하위 뷰들을 찾습니다.
            // 책 검색 관련 뷰 매핑
            LinearLayout bookSearchInfoContainer = feedBookSearch.findViewById(R.id.bookSearchInfoContainer);
            //EditText etBookTitle = feedBookSearch.findViewById(R.id.etBookTitle);
            //ImageView searchBook = feedBookSearch.findViewById(R.id.searchBook);
            //rvSearchResults = feedBookSearch.findViewById(R.id.rvSearchResults);
            etBookTitle = feedBookSearch.findViewById(R.id.etBookTitle);
            searchBook = feedBookSearch.findViewById(R.id.searchBook);
            rvSearchResults = feedBookSearch.findViewById(R.id.rvSearchResults);
            bookInfoSelected = feedBookSearch.findViewById(R.id.bookInfoSelected);

            // bookInfoSelected도 null 체크를 하는 것이 안전합니다.
            if (bookInfoSelected != null) {
                bookInfoSelected.setVisibility(View.GONE);

                bookInfoTitle = bookInfoSelected.findViewById(R.id.bookInfoTitle);
                bookInfoAuthor = bookInfoSelected.findViewById(R.id.bookInfoAuthor);
                bookInfoPublisher = bookInfoSelected.findViewById(R.id.bookInfoPublisher);
                bookCover = bookInfoSelected.findViewById(R.id.bookCover);
                bookInfoCategory = bookInfoSelected.findViewById(R.id.bookInfoCategory);
            }

            // RecyclerView 및 어댑터 초기화 로직
            bookController = new BookController(requireContext());
            filteredBookList = new ArrayList<>();
            bookAdapter = new SearchBookAdapter(requireContext(), filteredBookList);
            rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext())); // 수정
            rvSearchResults.setAdapter(bookAdapter);


            // 책 검색 버튼 클릭 리스너
            searchBook.setOnClickListener(v -> {
                if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.GONE);
                String query = etBookTitle.getText().toString().trim();
                rvSearchResults.setVisibility(View.VISIBLE);

                if (!query.isEmpty()) {
                    bookController.searchBooks(query, new Callback<BookResponse>() {
                        @Override
                        public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                            filteredBookList.clear();
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                filteredBookList.addAll(response.body().getData());
                            } else {
                                Log.e("BookSearch", "검색 실패: " + response.code() + " / " + response.message());
                            }
                            bookAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<BookResponse> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show();
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

                // 서버에 Book 생성 요청
                createBookOnServer(book);
            });
        }
    }
    // 책 생성
    private void createBookOnServer(Book searchResult) {
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
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

        // BookApi 사용
        BookApi bookApi = ApiClient.getClient(token).create(BookApi.class);
        bookApi.createBook("Bearer " + token, request).enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    selectedBookId = response.body().getBookId();
                } else {
                    Toast.makeText(requireContext(), "책 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 특정 날짜의 기록 불러오기 */
    private void loadLogForSelectedDate() {
        if (token == null || userId == -1) return;

        String from = selectedDate.toString();
        String to = selectedDate.toString();

        logController.getLogsByPeriod(token, userId, from, to,
                new Callback<ApiResponse<List<ReadingLogResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReadingLogResponse>>> call,
                                           Response<ApiResponse<List<ReadingLogResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReadingLogResponse> logs = response.body().getData();
                            if (logs != null && !logs.isEmpty()) {
                                ReadingLogResponse log = logs.get(0);
                                currentLogId = log.getId();
                                fillInputsWithLog(log);
                            } else {
                                Toast.makeText(requireContext(),
                                        "해당 날짜 기록이 없습니다. 새로 입력하세요.", Toast.LENGTH_SHORT).show();
                                clearInputs();
                                currentLogId = -1;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReadingLogResponse>>> call, Throwable t) {
                        Toast.makeText(requireContext(), "서버 오류", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** 입력 값 → 서버 저장 or 수정 */
    private void saveOrUpdateLog() {
        if (selectedBookId == -1) {
            Toast.makeText(requireContext(), "책을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int pages = 0;
        int hours = 0;
        int minutes = 0;

        try {
            String pageStr = editPage.getText().toString();
            String hourStr = editHours.getText().toString();
            String minuteStr = editMinutes.getText().toString();

            pages = pageStr.isEmpty() ? 0 : Integer.parseInt(pageStr);
            hours = hourStr.isEmpty() ? 0 : Integer.parseInt(hourStr);
            minutes = minuteStr.isEmpty() ? 0 : Integer.parseInt(minuteStr);

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "숫자를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalMinutes = hours * 60 + minutes; // API용 분 환산

        String readAt = selectedDate.atStartOfDay().toString();

        CheckBox checkboxFinished = rootView.findViewById(R.id.checkboxFinished);
        boolean isFinished = checkboxFinished.isChecked(); // 사용자가 체크한 상태 확인

        updateReadAtTimeFromInput(); // AM/PM → 24시간 변환 후 currentReadAtString 사용
        ReadingLogRequest request = new ReadingLogRequest(pages, totalMinutes, currentReadAtString, isFinished );

        if (currentLogId == -1) {
            // 새로 생성
            logController.createLog(token, userId, selectedBookId, request,
                    new Callback<ApiResponse<ReadingLogResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<ReadingLogResponse>> call,
                                               Response<ApiResponse<ReadingLogResponse>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "기록 저장 완료", Toast.LENGTH_SHORT).show();
                                loadLogForSelectedDate();
                                // 기록 저장 후 항상 먼저 로그 로드
                                loadLogForSelectedDate();

                                //완독이면 경험치 지급
                                if (isFinished) {
                                    giveCompletionExp();
                                }
                            } else {
                                Toast.makeText(requireContext(), "기록 저장 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<ReadingLogResponse>> call, Throwable t) {
                            Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // 수정
            logController.updateLog(token, userId, currentLogId, request,
                    new Callback<ApiResponse<ReadingLogResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<ReadingLogResponse>> call,
                                               Response<ApiResponse<ReadingLogResponse>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "기록 수정 완료", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "기록 수정 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<ReadingLogResponse>> call, Throwable t) {
                            Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /** 삭제 */
    private void deleteLog() {
        if (currentLogId == -1) {
            Toast.makeText(requireContext(), "삭제할 기록이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        logController.deleteLog(token, userId, currentLogId, new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    currentLogId = -1;
                    loadLogForSelectedDate(); // 선택한 날짜 갱신
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private List<BookListHelper.BookItem> bookItems = new ArrayList<>();

    private void fillInputsWithLog(ReadingLogResponse log) {
        // 시간
        LocalTime time = LocalTime.parse(log.getReadAt().substring(11)); // "HH:mm:ss"

        int hour = time.getHour();
        int minute = time.getMinute();

        // AM/PM 변환
        boolean isPm = hour >= 12;
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;

        editStartHour.setText(String.valueOf(displayHour));
        editStartMinute.setText(String.valueOf(minute));

        Spinner spinnerAmPm = rootView.findViewById(R.id.spinner_am_pm);
        spinnerAmPm.setSelection(isPm ? 1 : 0);

        // 페이지/시간 세팅
        editPage.setText(String.valueOf(log.getPagesRead()));
        int totalMinutes = log.getMinutesRead();
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        editHours.setText(String.valueOf(hours));
        editMinutes.setText(String.valueOf(minutes));

        // 선택된 책 ID
        selectedBookId = log.getBookId();

        // ✅ 검색 영역 숨기고 책 정보 영역만 보여주기
        if (etBookTitle != null) etBookTitle.setVisibility(View.GONE);
        if (searchBook != null) searchBook.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);

        if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.VISIBLE);

        // ✅ 서버에서 책 상세 정보 가져오기
        BookApi bookApi = ApiClient.getClient(token).create(BookApi.class);
        BookController tempController = new BookController(getContext());
        tempController.getBookDetail(selectedBookId, new retrofit2.Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Book book = response.body();
                    bookInfoTitle.setText(book.getTitle());
                    bookInfoAuthor.setText(book.getAuthor());
                    bookInfoPublisher.setText(book.getPublisher());
                    bookInfoCategory.setText(book.getCategoryName());
                    Glide.with(requireContext()).load(book.getCover()).into(bookCover);
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Log.e("AddReadingRecord", "책 상세 조회 실패", t);
            }
        });

        // 체크박스 표시 + 수정 불가 처리
        CheckBox checkboxFinished = rootView.findViewById(R.id.checkboxFinished);
        checkboxFinished.setChecked(log.isFinished());
        checkboxFinished.setEnabled(false); // UI에서 수정 불가


    }

    private void clearInputs() {
        editPage.setText("");
        editHours.setText("");
        editMinutes.setText("");
        editStartHour.setText("");
        editStartMinute.setText("");

        // ✅ AM/PM Spinner 초기화 (기본값: 오전)
        Spinner spinnerAmPm = rootView.findViewById(R.id.spinner_am_pm);
        if (spinnerAmPm != null) {
            spinnerAmPm.setSelection(0); // 0: 오전
        }

        // 서버로 전송할 최종 시간 문자열 초기화
        currentReadAtString = "";

        selectedBookId = -1;

        // ✅ 검색 영역 다시 보이게
        if (etBookTitle != null) etBookTitle.setVisibility(View.VISIBLE);
        if (searchBook != null) searchBook.setVisibility(View.VISIBLE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);

        // ✅ 책 정보 영역은 숨기기
        if (bookInfoSelected != null) bookInfoSelected.setVisibility(View.GONE);

        CheckBox checkboxFinished = rootView.findViewById(R.id.checkboxFinished);
        if (checkboxFinished != null) {
            checkboxFinished.setChecked(false);
            checkboxFinished.setEnabled(true); // 새 기록 입력 가능
        }
    }

    private void updateReadAtTimeFromInput() {
        String hourStr = editStartHour.getText().toString();
        String minuteStr = editStartMinute.getText().toString();

        int hour = hourStr.isEmpty() ? 0 : Integer.parseInt(hourStr);
        int minute = minuteStr.isEmpty() ? 0 : Integer.parseInt(minuteStr);
        int second = 0; // 여기서 미리 선언

        Spinner spinnerAmPm = rootView.findViewById(R.id.spinner_am_pm);
        boolean isPm = spinnerAmPm.getSelectedItemPosition() == 1;

        // 12시간 → 24시간 변환
        if (hour == 12) hour = isPm ? 12 : 0;
        else hour = isPm ? hour + 12 : hour;

        // 00:00 처리 → 00:00:01
        if (hour == 0 && minute == 0) second = 1;

        // 분 범위 초과 방지
        if (minute >= 60) { hour += minute / 60; minute %= 60; }

        // 24:00 처리 → 23:59:59
        if (hour >= 24) {
            hour = 23;
            minute = 59;
            second = 59;
            Toast.makeText(requireContext(), "시간은 12시 59분까지 입력 가능합니다.", Toast.LENGTH_SHORT).show();}

        // 최종 LocalDateTime 생성
        LocalDateTime readAt = selectedDate.atTime(hour, minute, second);
        currentReadAtString = readAt.toString(); // 서버 전송용
    }

    private void giveCompletionExp() {

        growthController.giveExp(userId, "COMPLETE_BOOK", new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    Toast.makeText(requireContext(), "경험치 지급 완료: " + response.body(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "경험치 지급 실패", Toast.LENGTH_SHORT).show();
                    //Log.e("ExpAPI", "Code: " + response.code() + ", Message: " + response.message());
                    //Log.d("EXP_DEBUG", "userId=" + userId + ", eventType=" + "COMPLETE_BOOK");
                    //Log.d("EXP_DEBUG", "token=" + token);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();

            }
        });

    }

}