#!/bin/bash

# ������Ʈ ��Ʈ���� �����ϼ���

# 1. ���ڵ��� UTF-8�� �ƴ� .java ���ϸ� ã��
echo "UTF-8 �ƴ� Java ���� ��ȯ ����..."

find ./src/main/java -name "*.java" | while read -r file; do
    encoding=$(file -bi "$file" | sed -n 's/.*charset=\([^ ]*\).*/\1/p')
    
    if [[ "$encoding" != "utf-8" ]]; then
        echo "��ȯ ��: $file (���ڵ�: $encoding)"

        # �ӽ� ���� ���� �� ��ȯ
        iconv -f "$encoding" -t utf-8 "$file" -o "${file}.utf8" 2>/dev/null
        
        if [ $? -eq 0 ]; then
            mv "${file}.utf8" "$file"
            echo "��ȯ �Ϸ�: $file"
        else
            echo "��ȯ ����: $file (���ڵ�: $encoding) ? ��ȯ�� �ǳʶ�"
            rm -f "${file}.utf8"
        fi
    else
        echo "UTF-8 ���ڵ�, ��ȯ �ʿ� ����: $file"
    fi
done

echo "��ȯ �۾� ����."
