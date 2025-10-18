#!/bin/bash

# ��ȯ�� �ֻ��� ��� ���� (�ʿ信 ���� �ٲټ���)
BASE_DIR="./src/main/java"

# �ѱ��� ���Ե� .java ���� ����Ʈ ���
files=$(grep -rl --include="*.java" '[��-�R]' "$BASE_DIR")

if [ -z "$files" ]; then
  echo "�ѱ��� ���Ե� .java ������ �����ϴ�."
  exit 0
fi

echo "�� $(echo "$files" | wc -l)���� �ѱ� ���� ������ UTF-8�� ��ȯ�մϴ�..."

for file in $files; do
  echo "��ȯ ��: $file"
  # iconv ��ȯ. ���� �� ���� ����
  if iconv -f cp949 -t utf-8 "$file" -o "${file}.utf8"; then
    mv "${file}.utf8" "$file"
  else
    echo "��ȯ ����: $file (���� ���ڵ��� cp949�� �ƴ� �� �ֽ��ϴ�)"
    rm -f "${file}.utf8"
  fi
done

echo "��ȯ �۾� �Ϸ�."
