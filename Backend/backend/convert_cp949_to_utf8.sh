#!/bin/bash

# º¯È¯ÇÒ ÃÖ»óÀ§ °æ·Î ÁöÁ¤ (ÇÊ¿ä¿¡ µû¶ó ¹Ù²Ù¼¼¿ä)
BASE_DIR="./src/main/java"

# ÇÑ±ÛÀÌ Æ÷ÇÔµÈ .java ÆÄÀÏ ¸®½ºÆ® ¾ò±â
files=$(grep -rl --include="*.java" '[°¡-ÆR]' "$BASE_DIR")

if [ -z "$files" ]; then
  echo "ÇÑ±ÛÀÌ Æ÷ÇÔµÈ .java ÆÄÀÏÀÌ ¾ø½À´Ï´Ù."
  exit 0
fi

echo "ÃÑ $(echo "$files" | wc -l)°³ÀÇ ÇÑ±Û Æ÷ÇÔ ÆÄÀÏÀ» UTF-8·Î º¯È¯ÇÕ´Ï´Ù..."

for file in $files; do
  echo "º¯È¯ Áß: $file"
  # iconv º¯È¯. ½ÇÆĞ ½Ã ¿øº» º¸Á¸
  if iconv -f cp949 -t utf-8 "$file" -o "${file}.utf8"; then
    mv "${file}.utf8" "$file"
  else
    echo "º¯È¯ ½ÇÆĞ: $file (ÆÄÀÏ ÀÎÄÚµùÀÌ cp949°¡ ¾Æ´Ò ¼ö ÀÖ½À´Ï´Ù)"
    rm -f "${file}.utf8"
  fi
done

echo "º¯È¯ ÀÛ¾÷ ¿Ï·á."
