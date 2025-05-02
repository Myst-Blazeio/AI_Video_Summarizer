#!/usr/bin/env python3

import sys
from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api.formatters import TextFormatter
from youtube_transcript_api._errors import TranscriptsDisabled, NoTranscriptFound, VideoUnavailable

def fetch_transcript(video_id: str, languages: list = ['en']) -> str:
    try:
        ytt_api = YouTubeTranscriptApi()
        transcript_list = ytt_api.list(video_id)

        # Try to find manually created transcript first
        try:
            transcript = transcript_list.find_manually_created_transcript(languages)
        except NoTranscriptFound:
            # Fallback to generated transcript
            transcript = transcript_list.find_generated_transcript(languages)

        fetched = transcript.fetch()
        formatter = TextFormatter()
        formatted_transcript = formatter.format_transcript(fetched)
        return formatted_transcript

    except (NoTranscriptFound, TranscriptsDisabled) as e:
        return "❌ No transcript available for this video."
    except VideoUnavailable:
        return "❌ The video is unavailable."
    except Exception as e:
        return f"❌ An error occurred: {e}"

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python yt__api.py <YouTubeVideoID>")
        sys.exit(1)

    video_id = sys.argv[1]
    result = fetch_transcript(video_id)
    print(result)
