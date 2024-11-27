from textblob import TextBlob
import sys


def analysis_text(text):
    text_for_analysis = TextBlob(text)
    result = text_for_analysis.sentiment
    print(result)
    return result


if __name__ == "__main__":
    param1 = sys.argv[1]
    if (len(sys.argv) > 2) or (type(param1) is not str):
        print("Invalid args count, format: python script.py <text>")
    else:
        analysis_text(param1)
