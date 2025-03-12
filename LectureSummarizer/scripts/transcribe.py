import os
import sys
import whisper
import codecs

# Ensure output is encoded in UTF-8 (fixes UnicodeEncodeError)
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.buffer)
sys.stderr = codecs.getwriter("utf-8")(sys.stderr.buffer)

def transcribe_audio(audio_file):
    print(f"Checking file: {audio_file}")  # Remove emojis to avoid encoding issues

    if not os.path.exists(audio_file):
        print("❌ Error: File not found.")
        return ""

    print("🔄 Loading Whisper model... (this may take a moment)")
    model = whisper.load_model("tiny")

    print(f"🎙️ Transcribing: {audio_file}")
    result = model.transcribe(audio_file)
    transcript = result["text"].strip()

    if not transcript:
        print("⚠️ Warning: No speech detected.")
        return ""

    print("✅ Transcription complete!")
    return transcript

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("❌ Error: No audio file provided.")
        sys.exit(1)

    audio_file = sys.argv[1]
    print(transcribe_audio(audio_file))
