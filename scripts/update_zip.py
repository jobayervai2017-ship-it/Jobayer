#!/usr/bin/env python3
import os
import zipfile
import shutil

def update_zip_bundles():
    apk_paths = [
        'app/build/outputs/apk/debug/app-debug.apk',
        '.build-outputs/app-debug.apk'
    ]
    apk_path = None
    for p in apk_paths:
        if os.path.exists(p) and os.path.getsize(p) > 1024:
            apk_path = p
            break

    if not apk_path:
        print("Error: No valid APK found to bundle.")
        return False

    print(f"Bundling APK: {apk_path} ({os.path.getsize(apk_path)} bytes)")

    sample_image_sync = '''[
  {
    "id": "scene_1",
    "imagePath": "sample_cinematic_1.jpg",
    "durationSeconds": 3.0,
    "transition": "CROSSFADE",
    "filterEffect": "CINEMATIC_WARM",
    "framingMode": "CROP_TO_FILL"
  },
  {
    "id": "scene_2",
    "imagePath": "sample_cinematic_2.jpg",
    "durationSeconds": 3.0,
    "transition": "ZOOM_IN",
    "filterEffect": "CYBERPUNK",
    "framingMode": "CROP_TO_FILL"
  },
  {
    "id": "scene_3",
    "imagePath": "sample_cinematic_3.jpg",
    "durationSeconds": 3.0,
    "transition": "SLIDE_LEFT",
    "filterEffect": "NORMAL",
    "framingMode": "CROP_TO_FILL"
  },
  {
    "id": "scene_4",
    "imagePath": "sample_cinematic_4.jpg",
    "durationSeconds": 3.0,
    "transition": "DIP_TO_BLACK",
    "filterEffect": "TEAL_ORANGE",
    "framingMode": "CROP_TO_FILL"
  }
]'''

    sample_text_sync = '''[
  {
    "id": "sub_1",
    "text": "STOP SCROLLING!",
    "startSeconds": 0.2,
    "endSeconds": 2.8,
    "animationPreset": "POP_UP",
    "textEffect": "STROKE_3D",
    "fontName": "Anton",
    "fontSize": 44,
    "textColorHex": "#FFE600",
    "outlineColorHex": "#000000",
    "posX": 0.5,
    "posY": 0.76
  },
  {
    "id": "sub_2",
    "text": "HERE IS THE SECRET...",
    "startSeconds": 3.0,
    "endSeconds": 5.8,
    "animationPreset": "BOUNCE",
    "textEffect": "STROKE_3D",
    "fontName": "Impact",
    "fontSize": 42,
    "textColorHex": "#00F0FF",
    "outlineColorHex": "#000000",
    "posX": 0.5,
    "posY": 0.78
  },
  {
    "id": "sub_3",
    "text": "DUAL-JSON ARCHITECTURE",
    "startSeconds": 6.0,
    "endSeconds": 8.8,
    "animationPreset": "WORD_GLOW",
    "textEffect": "GLOW",
    "fontName": "Bebas Neue",
    "fontSize": 40,
    "textColorHex": "#00FFE0",
    "outlineColorHex": "#FF007F",
    "posX": 0.5,
    "posY": 0.78
  },
  {
    "id": "sub_4",
    "text": "FOLLOW FOR MORE!",
    "startSeconds": 9.0,
    "endSeconds": 11.8,
    "animationPreset": "POP_UP",
    "textEffect": "STROKE_3D",
    "fontName": "Anton",
    "fontSize": 46,
    "textColorHex": "#FFE600",
    "outlineColorHex": "#000000",
    "posX": 0.5,
    "posY": 0.76
  }
]'''

    readme_content = '''=====================================================
SyncCut: Faceless Video Editor & Subtitle Studio
Samsung Galaxy A53 5G Optimized (Exynos MediaCodec)
=====================================================

এই জিপ ফাইলটিতে রয়েছে সম্পূর্ণ ইনস্টলেবল APK এবং ডেমো কনফিগারেশন।

কিভাবে APK ইনস্টল করবেন (How to Install):
-----------------------------------------------------
১. এই জিপ ফাইলটি আনজিপ (Extract) করুন।
২. 'FacelessVideoEditor_SamsungA53.apk' (বা 'SyncCut_SamsungA53.apk') ফাইলটিতে ট্যাপ করুন।
৩. যদি আপনার স্যামসাং বা অ্যান্ড্রয়েড ফোন 'Install Unknown Apps' বা 'অজানা উৎস থেকে ইনস্টল' করার অনুমতি চায়:
   - Settings (সেটিংস) এ ক্লিক করুন।
   - 'Allow from this source' (এই উৎস থেকে অনুমোদন দিন) অপশনটি চালু (Enable) করুন।
৪. এরপর 'Install' (ইনস্টল) বাটনে চাপ দিন।
৫. ইনস্টলেশন সম্পন্ন হলে অ্যাপটি ওপেন করুন!

মূল সুবিধাসমূহ (Key Features):
-----------------------------------------------------
- 10x Exynos Hardware Acceleration (MediaCodec H.264 MP4 export)
- গ্যালাক্সি A53 এবং স্ট্যান্ডার্ড স্যামসাং গ্যালারিতে সরাসরি Movies/FacelessEditor/ ফোল্ডারে ভিডিও সেভ হয়
- ডুয়াল-JSON সিঙ্ক আর্কিটেকচার (image_sync.json + text_sync.json)
- Hormozi, MrBeast, Neon Cyber ইত্যাদি ভাইরাল সাবটাইটেল স্টাইল
- অফলাইন ও সুরক্ষিত পার্মানেন্ট মিডিয়া স্টোরেজ

Included Files:
1. FacelessVideoEditor_SamsungA53.apk (Installable APK)
2. SyncCut_SamsungA53.apk (Mirrored APK)
3. image_sync.json (Visual scenes sync specification)
4. text_sync.json (Caption timeline & animation presets)
5. README_INSTALL_GUIDE.txt
'''

    # Copy APK directly to root so users can download it directly from file explorer
    shutil.copyfile(apk_path, 'FacelessVideoEditor_SamsungA53.apk')
    shutil.copyfile(apk_path, 'SyncCut_SamsungA53.apk')
    print(f"Copied standalone APKs to root: FacelessVideoEditor_SamsungA53.apk ({os.path.getsize('FacelessVideoEditor_SamsungA53.apk')} bytes)")

    zip_files = ['FacelessVideoEditor_SamsungA53.zip', 'SyncCut_SamsungA53.zip']
    for zname in zip_files:
        if os.path.exists(zname):
            os.remove(zname)
        with zipfile.ZipFile(zname, 'w', compression=zipfile.ZIP_DEFLATED) as z:
            z.write(apk_path, 'FacelessVideoEditor_SamsungA53.apk')
            z.writestr('image_sync.json', sample_image_sync)
            z.writestr('text_sync.json', sample_text_sync)
            z.writestr('README_INSTALL_GUIDE.txt', readme_content)
        print(f"Successfully updated: {zname} ({os.path.getsize(zname)} bytes)")

    return True

if __name__ == '__main__':
    update_zip_bundles()
