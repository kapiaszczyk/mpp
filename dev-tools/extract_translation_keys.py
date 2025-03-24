"""
This script is used for extracting all the translation keys from the project.
It will scan the whole project for translation keys and save them in a file `extracted_translation_keys.json`.

To compare the translation files, run script `dev-tools/compare_translations.py` after this script.
"""

MPP_CLIENT_PATH = "../mpp-client/src/app"
TRANSLATION_KEYS_FILE = "extracted_translation_keys.json"

import os
import json
import argparse
import re

import logging
logging.basicConfig(level=logging.INFO)

# Extract all the translation keys from the ts file
def extract_translation_keys(file_path):
    translation_keys = []
    
    # Match translation keys in pipes (e.g., 'key' | translate)
    # Example: 'mainMenu.sharedAlbums' | translate
    pipe_pattern = r"'([\w\d\.\-]+)'\s*\|\s*translate"
    
    # Match translation keys passed as function arguments (e.g., sendSuccessMessage('key'))
    # Example: sendSuccessMessage('mainMenu.sharedAlbums')
    function_pattern = r"send\w+\((?:'|\")([\w\d\.\-]+)(?:'|\")\)"

    # Match keys inside translate.instant('key') or translationService.instant('key')
    # Example: translate.instant('mainMenu.sharedAlbums')
    instant_pattern = r"instant\((?:'|\")([\w\d\.\-]+)(?:'|\")\)"

    # Match translation keys in object properties (e.g., label: 'mainMenu.key')
    # Example: { label: 'mainMenu.sharedAlbums', route: '/shared' },
    # FIXME: Ideally, those cases will be refactored to use pipes or functions
    # Sadly it also picks up some false positives like the following:
    # languages = [
    #     { code: 'en', label: 'English' },
    #     { code: 'pl', label: 'Polski' },
    # ];
    object_property_pattern = r"label:\s*(?:'|\")([\w\d\.\-]+)(?:'|\")"

    with open(file_path, 'r', encoding='utf-8') as file:
        for line in file:
            line = line.strip()

            # Extract keys from all patterns
            pipe_matches = re.findall(pipe_pattern, line)
            function_matches = re.findall(function_pattern, line)
            instant_matches = re.findall(instant_pattern, line)
            object_property_matches = re.findall(object_property_pattern, line)

            # Ignore keys with capital letters (e.g., 'Main menu')
            # This is a fix for the false positives in object_property_pattern
            object_property_matches = [match for match in object_property_matches if match.islower()]

            translation_keys.extend(pipe_matches + function_matches + instant_matches + object_property_matches)

    # FIXME: Hardcoded keys that are not extracted by the patterns
    hardcoded_keys = [
        "mainMenu.sharedAlbums",
        "mainMenu.tagView",
        "mainMenu.mainGallery",
        "mainMenu.adminView",
        "mainMenu.logout"
    ]

    translation_keys.extend(hardcoded_keys)

    # Remove duplicates
    translation_keys = list(set(translation_keys))

    return translation_keys

# Get all the ts files in the mpp-client
def get_ts_files():
    ts_files = []
    for root, dirs, files in os.walk(MPP_CLIENT_PATH):
        for file in files:
            if (file.endswith(".ts") and "spec" not in file) or file.endswith(".html"):
                ts_files.append(os.path.join(root, file))

                logging.debug(f"Found file which might contain translations: {file}")
    return ts_files


def main():
    translation_keys = []
    ts_files = get_ts_files()
    for file in ts_files:
        translation_keys.extend(extract_translation_keys(file))

    # Sort and remove duplicates
    translation_keys = sorted(list(set(translation_keys)))

    with open(TRANSLATION_KEYS_FILE, 'w') as file:
        json.dump(translation_keys, file, indent=4)
        print(f"Translation keys extracted from {len(ts_files)} files and saved in {TRANSLATION_KEYS_FILE}")
        print(f"Total translation keys extracted: {len(translation_keys)}")

if __name__ == "__main__":
    main()
