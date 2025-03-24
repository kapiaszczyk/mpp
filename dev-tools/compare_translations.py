"""
This script purpose is to compare the translation files of the different languages 
and check if they are consistent with each other or a given referemce file.

To scan the whole project for translation keys, run script `dev-tools/extract_translation_keys.py` first.

Args:
- `--reference-file` (optional): The reference file to compare the translation files with.
    If not provided, the script will compare the translation files with each other.
    The reference file should be a json file containing the translation keys.
"""

MPP_CLIENT_PATH = "../mpp-client"
TRANSLATIONS_DIR_PATH = "src/assets/i18n"

import json
import os
import argparse

# Load the extracted translation keys from the json file from the provided path
def load_translation_keys(file_path):
    with open(file_path, 'r', encoding="utf8") as file:
        return json.load(file)
    
# Get all the translation files in the mpp-client
def get_translation_files():
    translation_files = []
    for root, dirs, files in os.walk(os.path.join(MPP_CLIENT_PATH, TRANSLATIONS_DIR_PATH)):
        for file in files:
            if file.endswith(".json"):
                # Trim the file of the empty spaces and etc
                translation_files.append(os.path.join(root, file))
    return translation_files

# Compare the translation keys with the extracted translation keys
def compare_translation_keys(translation_keys, reference_keys):
    missing_keys = set(reference_keys) - set(translation_keys)
    extra_keys = set(translation_keys) - set(reference_keys)
    return missing_keys, extra_keys

def main(reference_file):
    # Load the extracted translation keys from the json file
    extracted_translation_keys = load_translation_keys(reference_file)
    
    # Get all the translation files in the mpp-client
    translation_files = get_translation_files()
    
    # Compare the translation keys with the extracted translation keys
    for file in translation_files:
        print(f"Comparing {file} with {reference_file}")
        with open(file, 'r', encoding="utf8") as f:
            translation_keys = json.load(f)
        missing_keys, extra_keys = compare_translation_keys(translation_keys, extracted_translation_keys)
        if missing_keys:
            print(f"Missing keys in {file}")
            pretty_print_missing(missing_keys)
        if extra_keys:
            print(f"Extra keys in {file}")
            pretty_print_extra(extra_keys)
        if not missing_keys and not extra_keys:
            print("The translation file is consistent with the reference file.")

def pretty_print_missing(keys):
    for key in keys:
        print("- " + key)

def pretty_print_extra(keys):
    for key in keys:
        print("+ " + key)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Compare the translation files of the different languages.')
    parser.add_argument('--reference-file', help='The reference file to compare the translation files with.')
    args = parser.parse_args()
    main(args.reference_file)