from docopt import docopt
import re
import os

WORD = 0
POS = 1
LEMMA = 2
ORIGINAL = 3
id_pattern = re.compile(".* id=\"(\d+)\" .*")
period_pattern = re.compile(".* period=\"(\d+)\" .*")


def main():
    args = docopt("""
    Usage:
        transformrsc.py <rsc.vrt> <targetpath>
    """)
    source = args["<rsc.vrt>"]
    target = args["<targetpath>"]
    if not os.path.exists(target):
        os.makedirs(target)
    row = LEMMA

    periods = {x: open(os.path.join(target, x), "w")
               for x in ["1650", "1700", "1750", "1800", "1850"]}

    for text, textid, period in iterate(source, row):
        periods[period].write(text)

    for key, value in periods.items():
        value.close()


def iterate(source, row):
    text, textid, period = "", "", ""
    for line in open(source, "r"):
        if line.startswith("<"):
            if line.startswith("<text"):
                if text is not "" and textid is not "" and period is not "":
                    yield " ".join(text) + "\n", textid, period
                elif text is not "" or textid is not "" or period is not "":
                    raise Exception("Error when parsing textid " + textid)
                text = []
                textid, period = extract_text_information(line)
        else:
            words = line.split("\t")

            # cardinal number
            if "@" in words[row]:
                word = words[WORD]
            else:
                word = words[row]

            #mangled &amp;
            if word == ";" and text[-1] == "&amp":
                text.pop()
            else:
                text.append(word)


def extract_text_information(line):
    idmatch, periodmatch = id_pattern.match(line), period_pattern.match(line)
    if not (idmatch and periodmatch):
        raise Exception("Error when parsing:" + line)
    return idmatch.group(1), periodmatch.group(1)


def finish_text(text, textid, period):
    return

if __name__ == "__main__":
    main()
