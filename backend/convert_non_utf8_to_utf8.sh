#!/bin/bash

# 프로젝트 루트에서 실행하세요

# 1. 인코딩이 UTF-8이 아닌 .java 파일만 찾기
echo "UTF-8 아닌 Java 파일 변환 시작..."

find ./src/main/java -name "*.java" | while read -r file; do
    encoding=$(file -bi "$file" | sed -n 's/.*charset=\([^ ]*\).*/\1/p')
    
    if [[ "$encoding" != "utf-8" ]]; then
        echo "변환 중: $file (인코딩: $encoding)"

        # 임시 파일 생성 및 변환
        iconv -f "$encoding" -t utf-8 "$file" -o "${file}.utf8" 2>/dev/null
        
        if [ $? -eq 0 ]; then
            mv "${file}.utf8" "$file"
            echo "변환 완료: $file"
        else
            echo "변환 실패: $file (인코딩: $encoding) ? 변환을 건너뜀"
            rm -f "${file}.utf8"
        fi
    else
        echo "UTF-8 인코딩, 변환 필요 없음: $file"
    fi
done

echo "변환 작업 종료."
